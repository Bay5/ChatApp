package com.bay.chatapp.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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
            timestamp = System.currentTimeMillis()
        )

        // we also upsert a minimal chat doc (metadata)
        val chatMeta = mapOf(
            "id" to cid,
            "userA" to listOf(currentUid, toUid).sorted()[0],
            "userB" to listOf(currentUid, toUid).sorted()[1],
            "lastMessage" to msg.text,
            "lastTimestamp" to msg.timestamp
        )

        db.runBatch { batch ->
            batch.set(chatRef, chatMeta)
            batch.set(msgRef, msg)
        }.addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener { e ->
            onResult(false, e.message)
        }
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
                onUpdate(msgs)
            }
    }
}
