package com.bay.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bay.chatapp.model.ChatItem

class ChatListViewModel : ViewModel() {

    private val _chats = MutableLiveData<List<ChatItem>>(emptyList())
    val chats: LiveData<List<ChatItem>> = _chats

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    //  dummy
    fun loadChats() {
        _loading.value = true

        // TODO: replace with Firestore messages aggregation later
        val dummy = listOf(
            ChatItem(
                otherUid = "uid1",
                otherUsername = "john",
                otherDisplayName = "John helldiver",
                lastMessage = "Democracy",
                lastTimestamp = System.currentTimeMillis()
            ),
            ChatItem(
                otherUid = "uid2",
                otherUsername = "Widya",
                otherDisplayName = "Widya",
                lastMessage = "Reminder Proposal",
                lastTimestamp = System.currentTimeMillis() - 60000
            )
        )

        _chats.value = dummy
        _loading.value = false
    }
}
