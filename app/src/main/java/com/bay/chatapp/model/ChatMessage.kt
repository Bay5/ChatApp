package com.bay.chatapp.model

data class ChatMessage(
    val id: String = "",
    val fromUid: String = "",
    val toUid: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
