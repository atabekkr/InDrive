package com.aralhub.araltaxi.history.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.aralhub.araltaxi.core.domain.driver.GetRideHistoryDetailsUseCase
import com.aralhub.araltaxi.core.domain.ridehistory.GetRideHistoryUseCase
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.ui.model.RideHistoryUI
import com.aralhub.ui.model.asUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideHistoryViewModel @Inject constructor(
    private val useCase: GetRideHistoryUseCase,
    private val rideHistoryDetailsUseCase: GetRideHistoryDetailsUseCase
) : ViewModel() {

    private val _rideHistoryFlow = MutableStateFlow<PagingData<RideHistoryUI>>(PagingData.empty())
    val rideHistoryFlow: StateFlow<PagingData<RideHistoryUI>> = _rideHistoryFlow

    init {
        getRideHistory()
    }

    private fun getRideHistory() {
        viewModelScope.launch {
            useCase()
                .map { it.map { item -> item.asUI() } }
                .cachedIn(viewModelScope)
                .collectLatest {
                    _rideHistoryFlow.value = it
                }
        }
    }

    private val _rideHistoryDetailsResult = MutableSharedFlow<RideDetailsUiState>()
    val rideHistoryDetailsResult: Flow<RideDetailsUiState> = _rideHistoryDetailsResult
    fun getRideHistoryDetails(rideId: Int) {
        viewModelScope.launch {
            _rideHistoryDetailsResult.emit(RideDetailsUiState.Loading)
            rideHistoryDetailsUseCase(rideId).let { result ->
                when (result) {
                    is Result.Error -> {
                        _rideHistoryDetailsResult.emit(RideDetailsUiState.Error(result.message))
                    }

                    is Result.Success -> {
                        _rideHistoryDetailsResult.emit(
                            RideDetailsUiState.Success(result.data.asUI())
                        )
                    }
                }
            }
        }
    }
}

sealed interface RideDetailsUiState {
    data object Idle : RideDetailsUiState
    data object Loading : RideDetailsUiState
    data class Success(val data: RideHistoryUI) : RideDetailsUiState
    data class Error(val message: String) : RideDetailsUiState
}