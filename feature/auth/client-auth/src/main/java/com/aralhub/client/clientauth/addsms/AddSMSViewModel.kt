package com.aralhub.client.clientauth.addsms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.domain.client.ClientVerifyPhoneUseCase
import com.aralhub.indrive.core.data.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSMSViewModel @Inject constructor(private val useCase: ClientVerifyPhoneUseCase) :
    ViewModel() {
    private val _addSMSUiState = MutableSharedFlow<AddSMSUiState>()
    val addSMSUiState = _addSMSUiState.asSharedFlow()

    fun verifyPhone(phone: String, code: String) = viewModelScope.launch {
        _addSMSUiState.emit(AddSMSUiState.Loading)
        _addSMSUiState.emit(useCase(phone = phone, code = code).let {
            when (it) {
                is Result.Error -> AddSMSUiState.Error(message = it.message)
                is Result.Success -> AddSMSUiState.Success(isSignedUp = it.data)
            }
        })
    }
}

sealed interface AddSMSUiState {
    data class Success(val isSignedUp: Boolean) : AddSMSUiState
    data class Error(val message: String) : AddSMSUiState
    data object Loading : AddSMSUiState
}