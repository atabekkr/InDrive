package com.aralhub.araltaxi.driver.orders.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.common.utils.convertIsoToMillis
import com.aralhub.araltaxi.core.domain.driver.offer.CreateOfferUseCase
import com.aralhub.indrive.core.data.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferViewModel @Inject constructor(
    private val createOfferUseCase: CreateOfferUseCase
) : ViewModel() {

    private var _createOfferUiState =
        MutableStateFlow<CreateOfferUiState>(CreateOfferUiState.Loading)
    val createOfferUiState = _createOfferUiState.asStateFlow()
    fun createOffer(rideUUID: String, amount: Int) {
        viewModelScope.launch {
            createOfferUseCase(rideUUID, amount).let { result ->
                when (result) {
                    is Result.Error -> _createOfferUiState.value =
                        (CreateOfferUiState.Error(result.message))

                    is Result.Success -> {
                        startExpirationChecker(result.data)
                        _createOfferUiState.value =
                            (CreateOfferUiState.Success(result.data))
                    }
                }
            }
        }
    }

    var dismissOfferWaitingTimeBottomSheet = MutableSharedFlow<Unit>()
    private fun startExpirationChecker(expiresAt: String) = viewModelScope.launch {
        while (true) {
            delay(1000L) // Check every second, adjust as needed
            val currentTime = System.currentTimeMillis()
            val expiresAtMillis = convertIsoToMillis(expiresAt)
            val dismissOrderLoadingDialog = expiresAtMillis <= currentTime
            if (dismissOrderLoadingDialog) {
                dismissOfferWaitingTimeBottomSheet.emit(Unit)
                return@launch
            }
        }
    }
}

sealed interface CreateOfferUiState {
    data object Loading : CreateOfferUiState
    data class Success(val expiresAt: String?) : CreateOfferUiState
    data class Error(val message: String) : CreateOfferUiState
}