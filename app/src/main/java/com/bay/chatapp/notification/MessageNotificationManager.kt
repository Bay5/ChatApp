package com.bay.chatapp.notification

import android.content.Context
import androidx.core.text.BidiFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

object MessageNotificationManager {
    private var listener: ListenerRegistration? = null
    private val lastNotified = mutableMapOf<String, Long>()

    fun start(context: Context) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid ?: return

        listener?.remove()
        listener = db.collection("chats")
            .where(Filter.or(Filter.equalTo("userA", uid), Filter.equalTo("userB", uid)))
            .addSnapshotListener { snap, _ ->
                val docs = snap?.documents.orEmpty()
                docs.forEach { doc ->
                    val otherUid = run {
                        val a = doc.getString("userA") ?: ""
                        val b = doc.getString("userB") ?: ""
                        if (uid == a) b else a
                    }
                    val lastMessage = doc.getString("lastMessage") ?: ""
                    val lastTimestamp = doc.getLong("lastTimestamp") ?: 0L
                    val lastFromUid = doc.getString("lastFromUid") ?: ""

                    val cid = listOf(uid, otherUid).sorted().joinToString("_")
                    val prev = lastNotified[cid] ?: 0L

                    if (otherUid.isBlank()) return@forEach
                    if (lastFromUid.isBlank() || lastFromUid == uid) return@forEach
                    if (lastTimestamp <= prev) return@forEach

                    db.collection("chats")
                        .document(cid)
                        .collection("messages")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(5)
                        .get()
                        .addOnSuccessListener { msnap ->
                            val batch = db.batch()
                            var updates = 0

                            val lines = msnap.documents
                                .mapNotNull { doc ->
                                    val txt = doc.getString("text")
                                    val from = doc.getString("fromUid")
                                    val status = doc.getString("messageStatus")
                                    
                                    if (from != uid && status == "sent") {
                                        batch.update(doc.reference, "messageStatus", "received")
                                        updates++
                                    }
                                    Triple(txt, from, status)
                                }
                                .filter { (_, from, status) -> from != uid && (status == "sent" || status == "received") }
                                .map { (text, _, _) ->
                                    val clean = (text ?: "").trim()
                                    ellipsize(clean, 60)
                                }
                                .take(3)
                                .reversed()

                            if (updates > 0) {
                                batch.commit()
                            }

                            db.collection("users")
                                .document(otherUid)
                                .get()
                                .addOnSuccessListener { userDoc ->
                                    val displayName = userDoc.getString("displayName") ?: ""
                                    val photo = userDoc.getString("photoUrl") ?: ""

                                    val nid = cid.hashCode()
                                    NotificationHelper.notifyMessages(
                                        context = context,
                                        otherUid = otherUid,
                                        otherDisplayName = displayName,
                                        otherPhotoUrl = photo,
                                        lines = lines,
                                        notificationId = nid
                                    )
                                    lastNotified[cid] = lastTimestamp
                                }
                        }
                }
            }
    }

    fun stop() {
        listener?.remove()
        listener = null
        lastNotified.clear()
    }

    private fun ellipsize(text: String, max: Int): String {
        val safe = BidiFormatter.getInstance().unicodeWrap(text)
        return if (safe.length <= max) safe else safe.take(max - 1) + "…"
    }
}
