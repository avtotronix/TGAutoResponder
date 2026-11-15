package com.tgautoresponder.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.tgautoresponder.app.data.PreferencesManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class TelegramNotificationService : NotificationListenerService() {

    private val supportedPackages = setOf(
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "org.telegram.messenger.beta",
        "org.telegram.plus",
        "org.thunderdog.challegram",
        "nekox.messenger"
    )

    companion object {
        private const val TAG = "TGAutoReplyService"
        const val ACTION_CONTACTS_UPDATED = "com.tgautoresponder.app.CONTACTS_UPDATED"
        private val lastReplyTimeMap = ConcurrentHashMap<String, Long>()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener connected. Scanning existing notifications...")
        scanExistingNotifications()
    }

    fun scanExistingNotifications() {
        try {
            val activeNotifs = activeNotifications ?: return
            val prefs = PreferencesManager(this)
            var foundAny = false

            for (sbn in activeNotifs) {
                if (!supportedPackages.contains(sbn.packageName)) continue
                val sender = extractPersonalSender(sbn) ?: continue
                if (prefs.registerDiscoveredContact(sender)) {
                    foundAny = true
                }
            }

            if (foundAny) {
                notifyContactsUpdated()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning active notifications", e)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        if (!supportedPackages.contains(packageName)) return

        val senderName = extractPersonalSender(sbn) ?: return

        val prefs = PreferencesManager(this)

        // Always register / update the contact in discovered list
        val isNew = prefs.registerDiscoveredContact(senderName)
        if (isNew) {
            notifyContactsUpdated()
        }

        if (!prefs.isEnabled) return

        // Check if auto-reply is enabled for this specific contact
        val replyMessage = prefs.getReplyMessageFor(senderName)
        if (replyMessage == null) {
            Log.d(TAG, "Auto-reply is disabled for contact: $senderName")
            return
        }

        // Check cooldown
        val cooldownMs = prefs.cooldownMinutes * 60 * 1000L
        val now = System.currentTimeMillis()
        val lastReplied = lastReplyTimeMap[senderName] ?: 0L

        if (now - lastReplied < cooldownMs) {
            Log.d(TAG, "Skipping $senderName due to cooldown. Last replied: $lastReplied")
            return
        }

        // Search for Reply action with RemoteInput
        val (action, remoteInput) = findReplyAction(sbn.notification) ?: run {
            Log.d(TAG, "No remote reply action found for notification from $senderName")
            return
        }

        try {
            sendAutoReply(action.actionIntent, remoteInput, replyMessage)
            lastReplyTimeMap[senderName] = now

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val isCustom = prefs.getContacts().find { it.name.equals(senderName, ignoreCase = true) }?.customMessage?.isNotBlank() == true
            val tag = if (isCustom) " (персонально)" else ""
            prefs.addLog("✅ $senderName$tag в ${timeFormat.format(Date())}: $replyMessage")
            Log.d(TAG, "Successfully replied to $senderName")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending auto-reply", e)
        }
    }

    private fun extractPersonalSender(sbn: StatusBarNotification): String? {
        val notification = sbn.notification ?: return null
        val extras = notification.extras ?: return null

        // Filter out groups: Android flags group conversations in extras
        if (extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)) {
            return null
        }

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()

        if (title.isNullOrEmpty() || text.isNullOrEmpty()) return null

        // Skip internal/service notifications
        if (title.contains("Telegram", ignoreCase = true) && text.contains("running", ignoreCase = true)) {
            return null
        }

        // Check if this notification has a reply action (channels do NOT have reply actions!)
        if (findReplyAction(notification) == null) {
            return null
        }

        // Filter group-like title structures (e.g. "Group Name (3 messages)" or "Group: User")
        if (title.contains(":") && title.indexOf(":") < 15) {
            // Group notifications often prepend group title
            return null
        }

        return title
    }

    private fun findReplyAction(notification: Notification): Pair<Notification.Action, RemoteInput>? {
        val actions = notification.actions ?: return null

        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            for (remoteInput in remoteInputs) {
                val label = remoteInput.label?.toString()?.lowercase(Locale.getDefault()) ?: ""
                if (label.contains("reply") || label.contains("ответить") || remoteInputs.isNotEmpty()) {
                    return Pair(action, remoteInput)
                }
            }
        }
        return null
    }

    private fun sendAutoReply(
        pendingIntent: PendingIntent,
        remoteInput: RemoteInput,
        replyMessage: String
    ) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putCharSequence(remoteInput.resultKey, replyMessage)
        RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, bundle)
        pendingIntent.send(this, 0, intent)
    }

    private fun notifyContactsUpdated() {
        val intent = Intent(ACTION_CONTACTS_UPDATED)
        sendBroadcast(intent)
    }
}
