package com.bay.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bay.chatapp.model.ChatMessage
import com.bay.chatapp.model.ChatRepository
import com.google.firebase.firestore.ListenerRegistration

class ChatViewModel(
    private val repo: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var listener: ListenerRegistration? = null
    private var currentOtherUid: String? = null

    fun startChat(otherUid: String) {
        if (currentOtherUid == otherUid && listener != null) return

        listener?.remove()
        currentOtherUid = otherUid
        _loading.value = true

        listener = repo.listenForMessages(otherUid) { list, err ->
            _loading.value = false
            if (err != null) {
                _error.value = err
            } else {
                _messages.value = list
            }
        }
    }

    fun sendMessage(text: String) {
        val otherUid = currentOtherUid ?: return
        if (text.isBlank()) return

        repo.sendMessage(otherUid, text.trim()) { ok, err ->
            if (!ok && err != null) {
                _error.value = err
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
