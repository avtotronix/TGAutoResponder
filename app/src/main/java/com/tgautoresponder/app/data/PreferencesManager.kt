package com.tgautoresponder.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tg_autoresponder_prefs", Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ENABLED, value).apply()

    var onlyWhenOffline: Boolean
        get() = prefs.getBoolean(KEY_ONLY_WHEN_OFFLINE, true)
        set(value) = prefs.edit().putBoolean(KEY_ONLY_WHEN_OFFLINE, value).apply()

    var replyText: String
        get() = prefs.getString(KEY_REPLY_TEXT, "Привет! Я сейчас не в сети, отвечу позже 👋") ?: ""
        set(value) = prefs.edit().putString(KEY_REPLY_TEXT, value).apply()

    var cooldownMinutes: Int
        get() = prefs.getInt(KEY_COOLDOWN, 0)
        set(value) = prefs.edit().putInt(KEY_COOLDOWN, value).apply()

    fun addLog(entry: String) {
        val existing = getLogs().toMutableList()
        existing.add(0, entry)
        if (existing.size > 30) {
            existing.removeAt(existing.size - 1)
        }
        val array = JSONArray(existing)
        prefs.edit().putString(KEY_LOGS_JSON, array.toString()).apply()
    }

    fun getLogs(): List<String> {
        val json = prefs.getString(KEY_LOGS_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Contact management (Personal chats only) ---

    fun getContacts(): List<ContactConfig> {
        val json = prefs.getString(KEY_CONTACTS_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<ContactConfig>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ContactConfig(
                        name = obj.getString("name"),
                        customMessage = obj.optString("customMessage", ""),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        lastMessageTime = obj.optLong("lastMessageTime", 0L)
                    )
                )
            }
            list.sortedByDescending { it.lastMessageTime }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveContact(config: ContactConfig) {
        val contacts = getContacts().toMutableList()
        val index = contacts.indexOfFirst { it.name.equals(config.name, ignoreCase = true) }
        if (index != -1) {
            contacts[index] = config
        } else {
            contacts.add(0, config)
        }
        persistContacts(contacts)
    }

    fun deleteContact(name: String) {
        val contacts = getContacts().filterNot { it.name.equals(name, ignoreCase = true) }
        persistContacts(contacts)
    }

    fun toggleContactEnabled(name: String, enabled: Boolean) {
        val contacts = getContacts().map {
            if (it.name.equals(name, ignoreCase = true)) it.copy(isEnabled = enabled) else it
        }
        persistContacts(contacts)
    }

    fun registerDiscoveredContact(name: String): Boolean {
        val contacts = getContacts().toMutableList()
        val existingIndex = contacts.indexOfFirst { it.name.equals(name, ignoreCase = true) }
        return if (existingIndex == -1) {
            contacts.add(
                0,
                ContactConfig(
                    name = name,
                    customMessage = "",
                    isEnabled = true,
                    lastMessageTime = System.currentTimeMillis()
                )
            )
            persistContacts(contacts)
            true
        } else {
            val existing = contacts[existingIndex]
            contacts[existingIndex] = existing.copy(lastMessageTime = System.currentTimeMillis())
            persistContacts(contacts)
            false
        }
    }

    fun getReplyMessageFor(name: String): String? {
        val contacts = getContacts()
        val config = contacts.find { it.name.equals(name, ignoreCase = true) }

        // If explicitly disabled for this person, do not reply
        if (config != null && !config.isEnabled) {
            return null
        }

        // If this person has a custom message, return it
        if (config != null && config.customMessage.isNotBlank()) {
            return config.customMessage
        }

        // Otherwise return the global default message
        return replyText
    }

    private fun persistContacts(contacts: List<ContactConfig>) {
        val array = JSONArray()
        for (c in contacts) {
            val obj = JSONObject()
            obj.put("name", c.name)
            obj.put("customMessage", c.customMessage)
            obj.put("isEnabled", c.isEnabled)
            obj.put("lastMessageTime", c.lastMessageTime)
            array.put(obj)
        }
        prefs.edit().putString(KEY_CONTACTS_JSON, array.toString()).apply()
    }

    companion object {
        private const val KEY_IS_ENABLED = "is_enabled"
        private const val KEY_ONLY_WHEN_OFFLINE = "only_when_offline"
        private const val KEY_REPLY_TEXT = "reply_text"
        private const val KEY_COOLDOWN = "cooldown_minutes"
        private const val KEY_CONTACTS_JSON = "contacts_list_json"
        private const val KEY_LOGS_JSON = "logs_json"
    }
}
