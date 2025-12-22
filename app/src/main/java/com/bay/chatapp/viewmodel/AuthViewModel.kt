package com.bay.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bay.chatapp.data.Repository.AuthRepository

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object PasswordResetSent : AuthState()
    data class NeedsUsername(val email: String?) : AuthState()
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

    fun resetPassword(email: String) {
        _authState.value = AuthState.Loading
        repository.resetPassword(email) { ok, error ->
            _authState.value = if (ok) {
                AuthState.PasswordResetSent
            } else {
                AuthState.Error(error)
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        repository.loginWithGoogle(idToken) { success, needUsername, email, error ->
            _authState.value = when {
                !success -> AuthState.Error(error)
                needUsername -> AuthState.NeedsUsername(email)
                else -> AuthState.Success
            }
        }
    }

    fun setUsername(username: String) {
        _authState.value = AuthState.Loading
        repository.setUsernameForUser(username) { ok, error ->
            _authState.value = if (ok) AuthState.Success else AuthState.Error(error)
        }
    }

    fun setDisplayName(displayName: String) {
        _authState.value = AuthState.Loading
        repository.setDisplayNameForUser(displayName) { ok, error ->
            _authState.value = if (ok) AuthState.Success else AuthState.Error(error)
        }
    }
}
