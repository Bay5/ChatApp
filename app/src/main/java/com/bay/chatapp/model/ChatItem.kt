package com.bay.chatapp.model
// "Other" are dummy
data class ChatItem(
    val otherUid: String = "",
    val otherUsername: String = "",
    val otherDisplayName: String = "",
    val otherPhotoUrl: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val unreadCount: Int = 0
)
