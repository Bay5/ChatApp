package com.bay.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bay.chatapp.model.AppUser
import com.bay.chatapp.model.ChatItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Filter

class ChatListViewModel : ViewModel() {

    private val _chats = MutableLiveData<List<ChatItem>>(emptyList())
    val chats: LiveData<List<ChatItem>> = _chats

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var listener: ListenerRegistration? = null

    private val userCache = mutableMapOf<String, AppUser>()
    private var items: List<ChatItem> = emptyList()
    
    fun loadChats() {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            _chats.value = emptyList()
            _loading.value = false
            return
        }

        _loading.value = true

        fun mergeAndPublish() {
            val combined = items
            val neededUids = combined.map { it.otherUid }
                .filter { it.isNotBlank() && !userCache.containsKey(it) }
                .distinct()

            fun publish() {
                val final = combined.map { item ->
                    val other = userCache[item.otherUid]
                    item.copy(
                        otherUsername = other?.username ?: item.otherUsername,
                        otherDisplayName = other?.displayName ?: item.otherDisplayName,
                        otherPhotoUrl = other?.photoUrl ?: item.otherPhotoUrl
                    )
                }.sortedByDescending { it.lastTimestamp }

                _chats.value = final
                _loading.value = false
            }

            if (neededUids.isEmpty()) {
                publish()
            } else {
                val chunks = neededUids.chunked(10)
                var done = 0
                chunks.forEach { chunk ->
                    db.collection("users")
                        .whereIn("uid", chunk)
                        .get()
                        .addOnSuccessListener { snap ->
                            snap.documents.mapNotNull { it.toObject(AppUser::class.java) }
                                .forEach { userCache[it.uid] = it }
                            done++
                            if (done == chunks.size) publish()
                        }
                        .addOnFailureListener {
                            done++
                            if (done == chunks.size) publish()
                        }
                }
            }
        }

        // Real-time: single listener via OR filter on userA/userB
        listener?.remove()
        listener = db.collection("chats")
            .where(
                Filter.or(
                    Filter.equalTo("userA", currentUid),
                    Filter.equalTo("userB", currentUid)
                )
            )
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents.orEmpty().map { doc ->
                    val userA = doc.getString("userA") ?: ""
                    val userB = doc.getString("userB") ?: ""
                    val otherUid = if (currentUid == userA) userB else userA
                    val lastMessage = doc.getString("lastMessage") ?: ""
                    val lastTimestamp = doc.getLong("lastTimestamp") ?: 0L
                    ChatItem(
                        otherUid = otherUid,
                        lastMessage = lastMessage,
                        lastTimestamp = lastTimestamp
                    )
                }
                items = list
                mergeAndPublish()
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
