package com.bay.chatapp.data.entity

data class Contact(
    val id: String = "",
    val userA: String = "",
    val userB: String = "",
    val requestedBy: String = "",
    val contactStatus: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
