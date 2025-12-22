package com.bay.chatapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bay.chatapp.ChatApp

@Database(
    entities = [
        UserEntity::class,
        ContactEntity::class,
        ChatEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChatAppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile private var INSTANCE: ChatAppDatabase? = null
        fun get(): ChatAppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(ChatApp.instance).also { INSTANCE = it }
            }
        }
        private fun build(context: Context): ChatAppDatabase {
            return Room.databaseBuilder(context, ChatAppDatabase::class.java, "chatapp.db").build()
        }
    }
}
