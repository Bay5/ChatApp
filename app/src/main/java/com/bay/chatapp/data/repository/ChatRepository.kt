package com.bay.chatapp.data.repository

import com.bay.chatapp.data.entity.ChatMessage
import com.bay.chatapp.data.local.ChatAppDatabase
import com.bay.chatapp.data.local.ChatEntity
import com.bay.chatapp.data.local.MessageEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.google.firebase.firestore.SetOptions

class ChatRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val local = ChatAppDatabase.get()
    private val messageDao = local.messageDao()
    private val chatDao = local.chatDao()
    private val repoScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private fun chatId(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString("_")
    }

    fun sendTextMessage(
        toUid: String,
        text: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUid = auth.currentUser?.uid
            ?: return onResult(false, "Not logged in")

        if (text.isBlank()) {
            onResult(false, "Empty message")
            return
        }

        val cid = chatId(currentUid, toUid)
        val chatRef = db.collection("chats").document(cid)
        val msgRef = chatRef.collection("messages").document()

        val msg = ChatMessage(
            id = msgRef.id,
            fromUid = currentUid,
            toUid = toUid,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            messageStatus = "sent"
        )

        // we also upsert a minimal chat doc (metadata)
        val chatMeta = mapOf(
            "id" to cid,
            "userA" to listOf(currentUid, toUid).sorted()[0],
            "userB" to listOf(currentUid, toUid).sorted()[1],
            "lastMessage" to msg.text,
            "lastFromUid" to currentUid,
            "lastTimestamp" to msg.timestamp
        )

        db.runBatch { batch ->
            batch.set(chatRef, chatMeta, SetOptions.merge())
            batch.set(msgRef, msg)
        }.addOnSuccessListener {
            repoScope.launch {
                chatDao.upsert(
                    ChatEntity(
                        id = cid,
                        userA = chatMeta["userA"] as String,
                        userB = chatMeta["userB"] as String,
                        lastMessage = chatMeta["lastMessage"] as String,
                        lastFromUid = chatMeta["lastFromUid"] as String,
                        lastTimestamp = chatMeta["lastTimestamp"] as Long
                    )
                )
                messageDao.upsert(
                    MessageEntity(
                        id = msg.id,
                        chatId = cid,
                        fromUid = msg.fromUid,
                        toUid = msg.toUid,
                        text = msg.text,
                        timestamp = msg.timestamp,
                        messageStatus = msg.messageStatus
                    )
                )
            }
            onResult(true, null)
        }.addOnFailureListener { e ->
            onResult(false, e.message)
        }
    }

    fun markMessagesRead(otherUid: String, onResult: (Boolean, String?) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false, "Not logged in")
        val cid = chatId(currentUid, otherUid)
        val coll = db.collection("chats").document(cid).collection("messages")
        coll.whereEqualTo("toUid", currentUid)
            .whereIn("messageStatus", listOf("sent", "received"))
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    onResult(true, null)
                    return@addOnSuccessListener
                }
                db.runBatch { batch ->
                    snap.documents.forEach { doc ->
                        batch.update(doc.reference, "messageStatus", "read")
                    }
                }.addOnSuccessListener { onResult(true, null) }
                 .addOnFailureListener { e -> onResult(false, e.message) }
            }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    /**
     * Listen to all messages in this chat, ordered ascending by time.
     * Returns ListenerRegistration so you can remove it later.
     */
    fun listenForMessages(
        otherUid: String,
        onUpdate: (List<ChatMessage>) -> Unit,
        onError: (String?) -> Unit
    ): ListenerRegistration? {
        val currentUid = auth.currentUser?.uid ?: run {
            onError("Not logged in")
            return null
        }

        val cid = chatId(currentUid, otherUid)
        return db.collection("chats")
            .document(cid)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e.message)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                val msgs = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java) }
                repoScope.launch {
                    val entities = msgs.map {
                        MessageEntity(
                            id = it.id,
                            chatId = cid,
                            fromUid = it.fromUid,
                            toUid = it.toUid,
                            text = it.text,
                            timestamp = it.timestamp,
                            messageStatus = it.messageStatus
                        )
                    }
                    messageDao.upsertAll(entities)
                }
                onUpdate(msgs)
            }
    }
}
