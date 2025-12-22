package com.bay.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bay.chatapp.data.entity.AppUser
import com.bay.chatapp.data.Repository.UserRepository

class UserSearchViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _users = MutableLiveData<List<AppUser>>(emptyList())
    val users: LiveData<List<AppUser>> = _users

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun search(query: String) {
        if (query.isBlank()) {
            _users.value = emptyList()
            _error.value = null
            return
        }

        _loading.value = true
        repository.searchUsersByUsername(query) { list, err ->
            _loading.value = false
            if (err != null) {
                _error.value = err
            } else {
                _error.value = null
                _users.value = list
            }
        }
    }
}
