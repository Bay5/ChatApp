package com.bay.chatapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val username: String,
    val displayName: String,
    val email: String,
    val photoUrl: String
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val userA: String,
    val userB: String,
    val requestedBy: String,
    val contactStatus: String,
    val updatedAt: Long
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val userA: String,
    val userB: String,
    val lastMessage: String,
    val lastFromUid: String,
    val lastTimestamp: Long
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val fromUid: String,
    val toUid: String,
    val text: String,
    val timestamp: Long,
    val messageStatus: String
)
