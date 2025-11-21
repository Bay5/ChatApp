package com.bay.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bay.chatapp.model.AuthRepository

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String?) : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun registerWithEmail(username: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        repository.registerWithEmail(username, email, password) { ok, error ->
            _authState.value = if (ok) AuthState.Success else AuthState.Error(error)
        }
    }

    fun loginWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        repository.loginWithEmail(email, password) { ok, error ->
            _authState.value = if (ok) AuthState.Success else AuthState.Error(error)
        }
    }
}
