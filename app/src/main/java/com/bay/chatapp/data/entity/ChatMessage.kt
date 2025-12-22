package com.bay.chatapp.data.entity

data class ChatMessage(
    val id: String = "",
    val fromUid: String = "",
    val toUid: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messageStatus: String = "sent"
)
