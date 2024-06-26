package com.mdev.chatapp.ui.auth.viewmode

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdev.chatapp.domain.repository.local.UserRepository
import com.mdev.chatapp.domain.repository.remote.AuthRepository
import com.mdev.chatapp.domain.result.ApiResult
import com.mdev.chatapp.ui.auth.AuthState
import com.mdev.chatapp.ui.auth.AuthUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
): ViewModel(), AuthViewModelInterface{

    override var state by mutableStateOf(AuthState())
    private val uiEventChannel = Channel<ApiResult<Unit>>()
    val uiEvent = uiEventChannel.receiveAsFlow()

    val users = userRepository.getAllUser()

    init {
        authenticate()
    }

    override fun onEvent(event: AuthUiEvent) {
        when(event) {
            is AuthUiEvent.SignedInUsernameChanged -> {
                state = state.copy(signedInUsernameChanged = event.value)
            }
            is AuthUiEvent.DeleteUserChanged -> {
                state = state.copy(signedInUsernameChanged = event.value)
            }
            is AuthUiEvent.SignedIn -> {
                authenticateSignedUser()
            }
            is AuthUiEvent.Authenticated -> {
                authenticate()
            }
            is AuthUiEvent.UnAuthenticatedUserChanged -> {
                unauthenticatedUser()
            }
            else -> {
                // do nothing
            }
        }
    }


    private fun authenticate(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = authRepository.authenticate()
            uiEventChannel.send(result)
            state = state.copy(isLoading = false)
        }
    }
    private fun authenticateSignedUser(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = authRepository.authenticateSignedUser(state.signedInUsernameChanged)
            uiEventChannel.send(result)
            state = state.copy(isLoading = false)
        }
    }

    private fun unauthenticatedUser(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = authRepository.unAuthenticateUser(state.signedInUsernameChanged)
            userRepository.deleteUserById(state.signedInUsernameChanged)
            uiEventChannel.send(result)
            state = state.copy(isLoading = false)
        }
    }
}

