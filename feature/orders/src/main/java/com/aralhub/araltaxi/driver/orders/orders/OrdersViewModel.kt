package com.aralhub.araltaxi.driver.orders.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.common.utils.rejectOfferState
import com.aralhub.araltaxi.core.domain.driver.CancelRideUseCase
import com.aralhub.araltaxi.core.domain.driver.DriverLogoutUseCase
import com.aralhub.araltaxi.core.domain.driver.GetCancelCausesUseCase
import com.aralhub.araltaxi.core.domain.driver.GetExistingOrdersUseCase
import com.aralhub.araltaxi.core.domain.driver.UpdateRideStatusUseCase
import com.aralhub.araltaxi.driver.orders.model.SendDriverLocationUI
import com.aralhub.araltaxi.driver.orders.model.asDomain
import com.aralhub.araltaxi.driver.orders.model.asUI
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.indrive.core.data.util.WebSocketEvent
import com.aralhub.indrive.core.data.util.closeActiveOrdersWebSocket
import com.aralhub.indrive.core.data.util.webSocketEvent
import com.aralhub.ui.model.CancelItem
import com.aralhub.ui.model.OrderItem
import com.aralhub.ui.model.RideCompletedUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val driverLogoutUseCase: DriverLogoutUseCase,
    private val getExistingOrdersUseCase: GetExistingOrdersUseCase,
    private val updateRideStatusUseCase: UpdateRideStatusUseCase,
    private val cancelRideUseCase: CancelRideUseCase,
    private val getCancelCausesUseCase: GetCancelCausesUseCase,
) : ViewModel() {

    private val _ordersListState = MutableStateFlow<List<OrderItem>>(
        emptyList()
    )
    val ordersListState: StateFlow<List<OrderItem>> = _ordersListState.asStateFlow()

    private val _logoutUiState = MutableSharedFlow<LogoutUiState>()
    val logoutUiState = _logoutUiState.asSharedFlow()
    fun logout() = viewModelScope.launch {
        _logoutUiState.emit(LogoutUiState.Loading)
        driverLogoutUseCase().let { result ->
            when (result) {
                is Result.Success -> {
                    _logoutUiState.emit(LogoutUiState.Success)
                }

                is Result.Error -> {
                    _logoutUiState.emit(LogoutUiState.Error(result.message))
                }
            }
        }
    }

    private val existingOrdersState =
        MutableStateFlow<GetActiveOrdersUiState>(GetActiveOrdersUiState.Idle)

    fun getExistingOrders(
        sendDriverLocationUI: SendDriverLocationUI
    ) = viewModelScope.launch {
        getExistingOrdersUseCase(sendDriverLocationUI.asDomain()).let { result ->
            when (result) {
                is Result.Success -> {
                    val listOfOrders = result.data.map { it.asUI() }
                    existingOrdersState.value =
                        (GetActiveOrdersUiState.GetExistOrder(listOfOrders))
                    _ordersListState.value = listOfOrders
                }

                is Result.Error -> {
                    existingOrdersState.value = (GetActiveOrdersUiState.Error(result.message))
                }
            }
        }
    }

    init {
        startOrdersWebSocket()
    }

    private var ordersWebSocketJob: Job? = null
    private fun startOrdersWebSocket() {
        ordersWebSocketJob?.cancel()
        ordersWebSocketJob = viewModelScope.launch {
            webSocketEvent
                .collect {
                    Timber.d("startOrdersWebSocket: $it")
                    when (it) {
                        is WebSocketEvent.ActiveOffer -> {
                            addOrder(it.order.asUI())
                        }

                        is WebSocketEvent.OfferAccepted -> {
                            try {
                                webSocketOrdersState.value =
                                    GetActiveOrdersUiState.OfferAccepted(it.data.asUI())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        is WebSocketEvent.RideCancel -> {
                            removeOrder(it.rideId)
                        }

                        is WebSocketEvent.OfferReject -> {
                            rejectOfferState.emit(Unit)
                        }

                        is WebSocketEvent.RideFieldUpdated -> {
                            updateOrder(it.rideId, it.value)
                        }

                        is WebSocketEvent.Unknown -> {
                            webSocketOrdersState.value = GetActiveOrdersUiState.Error(it.error)
                        }

                        is WebSocketEvent.RideCancelledByPassenger -> {
                            webSocketOrdersState.value =
                                GetActiveOrdersUiState.RideCanceledByPassenger
                        }

                        is WebSocketEvent.ConnectionFailed -> {
                            webSocketOrdersState.value = GetActiveOrdersUiState.ConnectionFailed
                        }
                    }
                }
        }
    }

    private val webSocketOrdersState =
        MutableStateFlow<GetActiveOrdersUiState>(GetActiveOrdersUiState.Loading)
    val ordersState = merge(
        existingOrdersState,
        webSocketOrdersState
    ).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        GetActiveOrdersUiState.Loading
    )

    private var _rideCanceledResult = MutableSharedFlow<RideCancelUiState>()
    val rideCanceledResult = _rideCanceledResult.asSharedFlow()
    fun cancelRide(rideId: Int, cancelCauseId: Int) {
        viewModelScope.launch {
            _rideCanceledResult.emit(RideCancelUiState.Loading)
            cancelRideUseCase(rideId, cancelCauseId).let { result ->
                when (result) {
                    is Result.Error -> {
                        _rideCanceledResult.emit(RideCancelUiState.Error(result.message))
                    }

                    is Result.Success -> {
                        _rideCanceledResult.emit(RideCancelUiState.Success)
                    }
                }
            }
        }
    }

    private var _updateRideStatusResult = MutableSharedFlow<RideUpdateUiState>()
    val updateRideStatusResult = _updateRideStatusResult.asSharedFlow()
    fun updateRideStatus(rideId: Int, status: String) {
        viewModelScope.launch {
            updateRideStatusUseCase(rideId, status).let { result ->
                when (result) {
                    is Result.Error -> {
                        _updateRideStatusResult.emit(RideUpdateUiState.Error(result.message))
                    }

                    is Result.Success -> {
                        _updateRideStatusResult.emit(RideUpdateUiState.Success(result.data?.asUI()))
                    }
                }
            }
        }
    }

    private fun addOrder(order: OrderItem) {
        _ordersListState.value = _ordersListState.value.toMutableList().apply { add(order) }
    }

    private fun removeOrder(rideId: String) {
        _ordersListState.value = _ordersListState.value.filterNot { it.uuid == rideId }
    }

    private fun updateOrder(rideId: String, value: Int) {
        val previousPrice = _ordersListState.value.find { it.uuid == rideId }?.roadPrice
        _ordersListState.value = _ordersListState.value.map {
            if (it.uuid == rideId) {
                val isPriceIncreased = previousPrice?.let {
                    it.toInt() < value
                }
                it.copy(roadPrice = value.toString(),
                    isPriceIncreased = isPriceIncreased ?: false)
            } else {
                it
            }
        }
    }

    private var _getCancelCausesResult = MutableSharedFlow<CancelRideUiState>()
    val getCancelCausesResult = _getCancelCausesResult.asSharedFlow()
    fun getCancelCauses() {
        viewModelScope.launch {
            _getCancelCausesResult.emit(CancelRideUiState.Loading)
            getCancelCausesUseCase().let { result ->
                when (result) {
                    is Result.Error -> {
                        _getCancelCausesResult.emit(CancelRideUiState.Error(result.message))
                    }

                    is Result.Success -> {
                        _getCancelCausesResult.emit(CancelRideUiState.Success(result.data.map {
                            CancelItem(
                                id = it.id,
                                title = it.name
                            )
                        }))
                    }
                }
            }
        }
    }

    fun switchBackToOrdersSocket() {
        startOrdersWebSocket()
    }

    fun disconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            closeActiveOrdersWebSocket.emit(Unit)
        }
    }

    fun setIdleState() {
        webSocketOrdersState.value = GetActiveOrdersUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

}

sealed interface LogoutUiState {
    data object Loading : LogoutUiState
    data object Success : LogoutUiState
    data class Error(val message: String) : LogoutUiState
}

sealed interface GetActiveOrdersUiState {
    data object Loading : GetActiveOrdersUiState
    data class GetExistOrder(val data: List<OrderItem>) : GetActiveOrdersUiState
    data class OfferAccepted(val data: OrderItem) : GetActiveOrdersUiState
    data object RideCanceledByPassenger : GetActiveOrdersUiState
    data object ConnectionFailed : GetActiveOrdersUiState
    data object Idle : GetActiveOrdersUiState
    data class Error(val message: String) : GetActiveOrdersUiState
}

sealed interface RideCancelUiState {
    data object Loading : RideCancelUiState
    data object Success : RideCancelUiState
    data class Error(val message: String) : RideCancelUiState
}

sealed interface RideUpdateUiState {
    data class Success(val data: RideCompletedUI?) : RideUpdateUiState
    data class Error(val message: String) : RideUpdateUiState
}

sealed interface CancelRideUiState {
    data object Loading : CancelRideUiState
    data class Success(val data: List<CancelItem>) : CancelRideUiState
    data class Error(val message: String) : CancelRideUiState
}