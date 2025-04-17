package com.aralhub.araltaxi.ride

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.domain.client.ClientCancelRideWithReasonUseCase
import com.aralhub.araltaxi.core.domain.client.ClientCancelRideWithoutReasonUseCase
import com.aralhub.araltaxi.core.domain.client.ClientGetActiveRideUseCase
import com.aralhub.araltaxi.core.domain.client.CreatePassengerReviewUseCase
import com.aralhub.araltaxi.core.domain.client.DisconnectClientActiveRideUseCase
import com.aralhub.araltaxi.core.domain.client.GetClientRideStatusUseCase
import com.aralhub.araltaxi.core.domain.client.GetDriverCardUseCase
import com.aralhub.araltaxi.core.domain.client.GetStandardPriceUseCase
import com.aralhub.araltaxi.core.domain.client.GetWaitAmountUseCase
import com.aralhub.indrive.core.data.model.driver.DriverCard
import com.aralhub.indrive.core.data.model.review.PassengerReview
import com.aralhub.indrive.core.data.model.review.Review
import com.aralhub.indrive.core.data.model.ride.ActiveRide
import com.aralhub.indrive.core.data.model.ride.RideStatus
import com.aralhub.indrive.core.data.model.ride.StandardPrice
import com.aralhub.indrive.core.data.model.ride.WaitAmount
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.indrive.core.data.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideViewModel @Inject constructor(
    private val getActiveRideUseCase: ClientGetActiveRideUseCase,
    private val cancelRideWithoutReasonUseCase: ClientCancelRideWithoutReasonUseCase,
    private val cancelRideWithReasonUseCase: ClientCancelRideWithReasonUseCase,
    private val getClientRideStatusUseCase: GetClientRideStatusUseCase,
    private val disconnectClientActiveRideUseCase: DisconnectClientActiveRideUseCase,
    private val getWaitAmountUseCase: GetWaitAmountUseCase,
    private val getStandardPriceUseCase: GetStandardPriceUseCase,
    private val getDriverCardUseCase: GetDriverCardUseCase,
    private val createPassengerReviewUseCase: CreatePassengerReviewUseCase
) : ViewModel() {

    private val _rideStateUiState = MutableStateFlow<RideStateUiState>(RideStateUiState.Idle)
    val rideStateUiState: StateFlow<RideStateUiState> = _rideStateUiState.asStateFlow()

    init {
        getClientRideState()
        getActiveRide()
    }

    private var _activeRideState = MutableStateFlow<ActiveRideUiState>(ActiveRideUiState.Loading)
    val activeRideState = _activeRideState.asStateFlow()
    private fun getActiveRide() = viewModelScope.launch {
        getActiveRideUseCase().let {
            when (it) {
                is Result.Error -> {
                    _activeRideState.emit(ActiveRideUiState.Error(it.message))
                }

                is Result.Success -> {
                    _activeRideState.emit(ActiveRideUiState.Success(it.data))
                }
            }
        }
    }

    private val _cancelRideState = MutableSharedFlow<CancelRideUiState>()
    val cancelRideState = _cancelRideState.asSharedFlow()

    private val _cancelRideWithReasonState = MutableSharedFlow<CancelRideUiState>()
    val cancelRideWithReasonState = _cancelRideWithReasonState.asSharedFlow()

    fun cancelRide(rideId: Int) = viewModelScope.launch {
        cancelRideWithoutReasonUseCase(rideId).let {
            when (it) {
                is Result.Error -> {
                    _cancelRideState.emit(CancelRideUiState.Error(it.message))
                }

                is Result.Success -> {
                    disconnectClientActiveRideUseCase()
                    _cancelRideState.emit(CancelRideUiState.Success)
                }
            }
        }
    }

    fun cancelRideWithReason(rideId: Int, reasonId: Int) = viewModelScope.launch {
        cancelRideWithReasonUseCase(rideId, reasonId).let {
            when (it) {
                is Result.Error -> {
                    _cancelRideWithReasonState.emit(CancelRideUiState.Error(it.message))
                }

                is Result.Success -> {
                    _cancelRideWithReasonState.emit(CancelRideUiState.Success)
                }
            }
        }
    }

    private fun getClientRideState() = viewModelScope.launch {
        getClientRideStatusUseCase().collectLatest {
            _rideStateUiState.emit(RideStateUiState.Success(it))
        }
    }

    fun setRideStateIdle() = viewModelScope.launch {
        _rideStateUiState.emit(RideStateUiState.Idle)
    }

    private var _getWaitAmountUiState = MutableSharedFlow<GetWaitAmountUiState>()
    val getWaitAmountUiState = _getWaitAmountUiState.asSharedFlow()
    fun getWaitAmount(rideId: Int) = viewModelScope.launch {
        _getWaitAmountUiState.emit(
            getWaitAmountUseCase(rideId).fold(
                onSuccess = {
                    GetWaitAmountUiState.Success(it)
                },
                onError = {
                    GetWaitAmountUiState.Error(it)
                }
            )
        )
    }

    private var _getStandardPriceUiState = MutableSharedFlow<GetStandardPriceUiState>()
    val getStandardPriceUiState = _getStandardPriceUiState.asSharedFlow()
    fun getStandardPrice() = viewModelScope.launch {
        _getStandardPriceUiState.emit(
            getStandardPriceUseCase().let {
                when (it) {
                    is Result.Error -> GetStandardPriceUiState.Error(it.message)
                    is Result.Success -> {
                        Log.i("RideViewModel", "${it.data}")
                        GetStandardPriceUiState.Success(it.data)
                    }
                }
            }
        )
    }

    private val _getDriverCardUiState =
        MutableStateFlow<GetDriverCardUiState>(GetDriverCardUiState.Loading)
    val getDriverCardUiState = _getDriverCardUiState.asStateFlow()
    fun getDriverCard(driverId: Int) = viewModelScope.launch {
        _getDriverCardUiState.emit(
            getDriverCardUseCase(driverId).let {
                when (it) {
                    is Result.Error -> GetDriverCardUiState.Error(it.message)
                    is Result.Success -> GetDriverCardUiState.Success(it.data)
                }
            }
        )
    }

    private val _createReviewUiState =
        MutableStateFlow<CreateReviewUiState>(CreateReviewUiState.Loading)
    val createReviewUiState = _createReviewUiState.asStateFlow()
    fun createReview(review: PassengerReview) = viewModelScope.launch {
        _createReviewUiState.emit(
            createPassengerReviewUseCase(review).let {
                when (it) {
                    is Result.Error -> CreateReviewUiState.Error(it.message)
                    is Result.Success -> CreateReviewUiState.Success(it.data)
                }
            }
        )

    }

    override fun onCleared() {
        super.onCleared()
        setRideStateIdle()
    }

}

sealed interface GetWaitAmountUiState {
    data object Loading : GetWaitAmountUiState
    data class Success(val waitAmount: WaitAmount) : GetWaitAmountUiState
    data class Error(val message: String) : GetWaitAmountUiState
}

sealed interface RideStateUiState {
    data object Idle : RideStateUiState
    data object Loading : RideStateUiState
    data class Success(val rideState: RideStatus) : RideStateUiState
    data class Error(val message: String) : RideStateUiState
}

sealed interface ActiveRideUiState {
    data object Loading : ActiveRideUiState
    data class Success(val activeRide: ActiveRide) : ActiveRideUiState
    data class Error(val message: String) : ActiveRideUiState
}

sealed interface CancelRideUiState {
    data object Loading : CancelRideUiState
    data object Success : CancelRideUiState
    data class Error(val message: String) : CancelRideUiState
}

sealed interface GetStandardPriceUiState {
    data object Loading : GetStandardPriceUiState
    data class Success(val standardPrice: StandardPrice) : GetStandardPriceUiState
    data class Error(val message: String) : GetStandardPriceUiState
}

sealed interface GetDriverCardUiState {
    data object Loading : GetDriverCardUiState
    data class Success(val driverCard: DriverCard) : GetDriverCardUiState
    data class Error(val message: String) : GetDriverCardUiState
}

sealed interface CreateReviewUiState {
    data object Loading : CreateReviewUiState
    data class Success(val review: Review) : CreateReviewUiState
    data class Error(val message: String) : CreateReviewUiState
}