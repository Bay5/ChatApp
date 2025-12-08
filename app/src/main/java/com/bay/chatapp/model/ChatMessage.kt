package com.bay.chatapp.model

data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val text: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
