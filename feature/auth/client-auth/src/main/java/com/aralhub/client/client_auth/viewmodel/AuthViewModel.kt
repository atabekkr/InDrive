package com.aralhub.client.client_auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.indrive.core.data.repository.SampleRepository
import com.aralhub.network.WrappedResult
import com.aralhub.network.model.NetworkAuthRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: SampleRepository,
) : ViewModel() {

    private val _authState = MutableSharedFlow<LoginUiState>()
    val authState = _authState.asSharedFlow()

    fun auth(phone: String) {
        viewModelScope.launch {
            _authState.emit(repository.auth(NetworkAuthRequest(phone)).let {
                when (it) {
                    is WrappedResult.Error -> LoginUiState.Error(it.message)
                    WrappedResult.Loading -> LoginUiState.Loading
                    is WrappedResult.Success -> LoginUiState.Success
                }
            })
        }
    }

    sealed interface LoginUiState {
        data object Success : LoginUiState
        data class Error(val message: String) : LoginUiState
        data object Loading : LoginUiState
    }
}