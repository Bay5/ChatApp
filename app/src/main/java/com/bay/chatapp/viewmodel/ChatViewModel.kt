package com.bay.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bay.chatapp.data.entity.ChatMessage
import com.bay.chatapp.data.Repository.ChatRepository
import com.google.firebase.firestore.ListenerRegistration

class ChatViewModel(
    private val repo: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _sending = MutableLiveData(false)
    val sending: LiveData<Boolean> = _sending

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var otherUid: String? = null
    private var listener: ListenerRegistration? = null

    fun startChat(otherUid: String) {
        this.otherUid = otherUid

        // clean old listener if any
        listener?.remove()

        listener = repo.listenForMessages(
            otherUid = otherUid,
            onUpdate = { msgs -> _messages.postValue(msgs) },
            onError = { err -> _error.postValue(err) }
        )

        repo.markMessagesRead(otherUid) { ok, err ->
            if (!ok) _error.postValue(err)
        }
    }

    fun sendMessage(text: String) {
        val targetUid = otherUid ?: return
        _sending.value = true

        repo.sendTextMessage(targetUid, text) { ok, err ->
            _sending.postValue(false)
            if (!ok) {
                _error.postValue(err ?: "Failed to send")
            }
        }
    }

    fun markAllRead() {
        val targetUid = otherUid ?: return
        repo.markMessagesRead(targetUid) { ok, err ->
            if (!ok) _error.postValue(err)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
