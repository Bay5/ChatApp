package com.bay.chatapp.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // stable chatId from 2 uids
    fun chatIdForUsers(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString("_")
    }

    fun listenForMessages(
        otherUid: String,
        onUpdate: (List<ChatMessage>, String?) -> Unit
    ): ListenerRegistration? {
        val currentUid = auth.currentUser?.uid ?: run {
            onUpdate(emptyList(), "Not logged in")
            return null
        }

        val chatId = chatIdForUsers(currentUid, otherUid)

        return db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    onUpdate(emptyList(), e.message)
                    return@addSnapshotListener
                }

                if (snap == null || snap.isEmpty) {
                    onUpdate(emptyList(), null)
                    return@addSnapshotListener
                }

                val msgs = snap.documents.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                }
                onUpdate(msgs, null)
            }
    }

    fun sendMessage(
        otherUid: String,
        text: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false, "Not logged in")
        val chatId = chatIdForUsers(currentUid, otherUid)

        val msg = ChatMessage(
            chatId = chatId,
            text = text,
            senderId = currentUid,
            receiverId = otherUid,
            timestamp = System.currentTimeMillis()
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(msg)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }
}
