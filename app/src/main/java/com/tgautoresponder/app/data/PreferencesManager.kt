package com.tgautoresponder.app.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tg_autoresponder_prefs", Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ENABLED, value).apply()

    var replyText: String
        get() = prefs.getString(KEY_REPLY_TEXT, "Привет! Я сейчас занят, отвечу позже 👋") ?: ""
        set(value) = prefs.edit().putString(KEY_REPLY_TEXT, value).apply()

    var cooldownMinutes: Int
        get() = prefs.getInt(KEY_COOLDOWN, 10)
        set(value) = prefs.edit().putInt(KEY_COOLDOWN, value).apply()

    var replyToGroups: Boolean
        get() = prefs.getBoolean(KEY_REPLY_GROUPS, false)
        set(value) = prefs.edit().putBoolean(KEY_REPLY_GROUPS, value).apply()

    fun addLog(entry: String) {
        val existing = getLogs().toMutableList()
        existing.add(0, entry)
        if (existing.size > 20) {
            existing.removeAt(existing.size - 1)
        }
        prefs.edit().putStringSet(KEY_LOGS, existing.toSet()).apply()
    }

    fun getLogs(): List<String> {
        return prefs.getStringSet(KEY_LOGS, emptySet())?.toList() ?: emptyList()
    }

    companion object {
        private const val KEY_IS_ENABLED = "is_enabled"
        private const val KEY_REPLY_TEXT = "reply_text"
        private const val KEY_COOLDOWN = "cooldown_minutes"
        private const val KEY_REPLY_GROUPS = "reply_groups"
        private const val KEY_LOGS = "reply_logs"
    }
}
