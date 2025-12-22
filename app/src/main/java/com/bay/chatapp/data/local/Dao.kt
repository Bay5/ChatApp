package com.bay.chatapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserEntity>)
}

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(contact: ContactEntity)

    @Query("SELECT * FROM contacts WHERE contactStatus = 'ACCEPTED'")
    suspend fun getAccepted(): List<ContactEntity>
}

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(chat: ChatEntity)

    @Query("SELECT * FROM chats WHERE userA = :uid OR userB = :uid")
    suspend fun getChatsForUser(uid: String): List<ChatEntity>
}

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: MessageEntity)

    @Update
    suspend fun update(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getForChat(chatId: String): List<MessageEntity>

    @Query("UPDATE messages SET messageStatus = 'read' WHERE chatId = :chatId AND toUid = :currentUid AND messageStatus = 'sent'")
    suspend fun markRead(chatId: String, currentUid: String)

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId AND toUid = :currentUid AND messageStatus = 'sent'")
    suspend fun getUnreadCount(chatId: String, currentUid: String): Int
}
