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
        private val lastReplyTimeMap = ConcurrentHashMap<String, Long>()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        if (!supportedPackages.contains(packageName)) return

        val prefs = PreferencesManager(this)
        if (!prefs.isEnabled) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()

        if (title.isNullOrEmpty() || text.isNullOrEmpty()) return

        // Skip internal/service notifications
        if (title.contains("Telegram", ignoreCase = true) && text.contains("running", ignoreCase = true)) return

        // Check cooldown
        val cooldownMs = prefs.cooldownMinutes * 60 * 1000L
        val now = System.currentTimeMillis()
        val lastReplied = lastReplyTimeMap[title] ?: 0L

        if (now - lastReplied < cooldownMs) {
            Log.d(TAG, "Skipping $title due to cooldown. Last replied: $lastReplied")
            return
        }

        // Search for Reply action with RemoteInput
        val (action, remoteInput) = findReplyAction(sbn.notification) ?: run {
            Log.d(TAG, "No remote reply action found for notification from $title")
            return
        }

        val replyMessage = prefs.replyText.ifBlank { "Привет! Сейчас не могу ответить, напишу позже." }

        try {
            sendAutoReply(action.actionIntent, remoteInput, replyMessage)
            lastReplyTimeMap[title] = now

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            prefs.addLog("✅ $title (${timeFormat.format(Date())}): $replyMessage")
            Log.d(TAG, "Successfully replied to $title")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending auto-reply", e)
        }
    }

    private fun findReplyAction(notification: Notification): Pair<Notification.Action, RemoteInput>? {
        val actions = notification.actions ?: return null

        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            for (remoteInput in remoteInputs) {
                val label = remoteInput.label?.toString()?.lowercase(Locale.getDefault()) ?: ""
                // Telegram reply action usually has "reply", "ответить", or contains standard remote input
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
}
