package com.tgautoresponder.app.data

data class ContactConfig(
    val name: String,
    val customMessage: String = "",
    val isEnabled: Boolean = true,
    val lastMessageTime: Long = System.currentTimeMillis()
)
