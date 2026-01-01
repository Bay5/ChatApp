package com.bay.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bay.chatapp.data.entity.AppUser
import com.bay.chatapp.data.entity.Contact
import com.bay.chatapp.data.repository.ContactRepository
import com.bay.chatapp.data.repository.UserRepository

class ContactViewModel(
    private val repo: ContactRepository = ContactRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    private val _contactWithUser = MutableLiveData<Contact?>()
    val contactWithUser: LiveData<Contact?> = _contactWithUser

    // 🔹 Final list of full AppUser contacts
    private val _contactUsers = MutableLiveData<List<AppUser>>()
    val contactUsers: LiveData<List<AppUser>> = _contactUsers

    // 🔹 Incoming requests
    private val _incomingRequests = MutableLiveData<List<AppUser>>()
    val incomingRequests: LiveData<List<AppUser>> = _incomingRequests

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

    fun acceptRequestFrom(uid: String) {
        _loading.value = true
        repo.acceptContactRequest(uid) { ok, err ->
            _loading.value = false
            if (!ok) _error.value = err
            else loadIncomingRequests() // refresh list
        }
    }

    fun rejectRequestFrom(uid: String) {
        _loading.value = true
        repo.rejectContactRequest(uid) { ok, err ->
            _loading.value = false
            if (!ok) _error.value = err
            else loadIncomingRequests() // refresh list
        }
    }

    fun loadIncomingRequests() {
        _loading.value = true
        repo.getIncomingRequests { ids, err ->
            if (err != null) {
                _loading.value = false
                _error.value = err
                _incomingRequests.value = emptyList()
                return@getIncomingRequests
            }

            if (ids.isEmpty()) {
                _loading.value = false
                _incomingRequests.value = emptyList()
                return@getIncomingRequests
            }

            userRepo.getUsersByIds(ids) { users, err2 ->
                _loading.value = false
                if (err2 != null) _error.value = err2
                _incomingRequests.value = users
            }
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

    fun unfriend(otherUid: String) {
        _loading.value = true
        repo.unfriend(otherUid) { ok, err ->
            _loading.value = false
            if (!ok) {
                _error.value = err
            } else {
                loadContactUsers()
            }
        }
    }
}
