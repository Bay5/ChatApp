package com.bay.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bay.chatapp.model.Contact
import com.bay.chatapp.model.ContactRepository

class ContactViewModel(
    private val repo: ContactRepository = ContactRepository()
) : ViewModel() {

    private val _contactWithUser = MutableLiveData<Contact?>()
    val contactWithUser: LiveData<Contact?> = _contactWithUser

    private val _acceptedContacts = MutableLiveData<List<String>>()
    val acceptedContacts: LiveData<List<String>> = _acceptedContacts

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

    fun loadAcceptedContacts() {
        _loading.value = true
        repo.getAcceptedContacts { list, err ->
            _loading.value = false
            if (err != null) _error.value = err
            _acceptedContacts.value = list
        }
    }
}
