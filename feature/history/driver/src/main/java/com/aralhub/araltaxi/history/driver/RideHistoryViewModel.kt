package com.aralhub.araltaxi.history.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.domain.ridehistory.GetRideHistoryUseCase
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.ui.model.RideHistoryUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideHistoryViewModel @Inject constructor(
    private val useCase: GetRideHistoryUseCase
) : ViewModel() {

    val getRideHistory = MutableStateFlow<RideHistoryUiState>(RideHistoryUiState.Idle)

    init {
        getRideHistory()
    }

    private fun getRideHistory() {
        viewModelScope.launch {
            getRideHistory.value = RideHistoryUiState.Loading
            useCase().let { result ->
                when (result) {
                    is Result.Error -> getRideHistory.value =
                        RideHistoryUiState.Error(result.message)

                    is Result.Success -> getRideHistory.value =
                        RideHistoryUiState.Success(result.data.asUI())
                }
            }
        }
    }
}

sealed interface RideHistoryUiState {
    data object Idle : RideHistoryUiState
    data object Loading : RideHistoryUiState
    data class Success(val data: List<RideHistoryUI>) : RideHistoryUiState
    data class Error(val message: String) : RideHistoryUiState
}