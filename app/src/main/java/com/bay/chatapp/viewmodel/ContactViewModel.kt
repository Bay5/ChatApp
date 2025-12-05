package com.bay.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bay.chatapp.model.AppUser
import com.bay.chatapp.model.Contact
import com.bay.chatapp.model.ContactRepository
import com.bay.chatapp.model.UserRepository

class ContactViewModel(
    private val repo: ContactRepository = ContactRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    private val _contactWithUser = MutableLiveData<Contact?>()
    val contactWithUser: LiveData<Contact?> = _contactWithUser

    // 🔹 Final list of full AppUser contacts
    private val _contactUsers = MutableLiveData<List<AppUser>>()
    val contactUsers: LiveData<List<AppUser>> = _contactUsers

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun checkContact(otherUid: String) {
        _loading.value = true
        repo.getContactWith(otherUid) { contact, err ->
            _loading.value = false
            if (err != null) _error.value = err
            _contactWithUser.value = contact
        }
    }

    fun sendRequest(toUid: String) {
        _loading.value = true
        repo.sendContactRequest(toUid) { ok, err ->
            _loading.value = false
            if (!ok) _error.value = err
        }
    }

    fun accept(id: String) {
        repo.acceptContact(id) { ok, err ->
            if (!ok) _error.value = err
        }
    }

    fun reject(id: String) {
        repo.rejectContact(id) { ok, err ->
            if (!ok) _error.value = err
        }
    }

    fun loadContactUsers() {
        _loading.value = true
        repo.getAcceptedContacts { ids, err ->
            if (err != null) {
                _loading.value = false
                _error.value = err
                _contactUsers.value = emptyList()
                return@getAcceptedContacts
            }

            if (ids.isEmpty()) {
                _loading.value = false
                _contactUsers.value = emptyList()
                return@getAcceptedContacts
            }

            userRepo.getUsersByIds(ids) { users, err2 ->
                _loading.value = false
                if (err2 != null) _error.value = err2
                _contactUsers.value = users
            }
        }
    }
}
