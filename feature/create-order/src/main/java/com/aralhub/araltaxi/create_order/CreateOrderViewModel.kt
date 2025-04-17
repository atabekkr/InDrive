package com.aralhub.araltaxi.create_order

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.domain.client.ClientCreateRideUseCase
import com.aralhub.araltaxi.core.domain.client.ClientGetRecommendedPriceUseCase
import com.aralhub.araltaxi.core.domain.payment.GetAllPaymentMethodsUseCase
import com.aralhub.araltaxi.core.domain.rideoption.GetRideOptionsUseCase
import com.aralhub.indrive.core.data.model.client.ClientRide
import com.aralhub.indrive.core.data.model.client.ClientRideRequest
import com.aralhub.indrive.core.data.model.client.ClientRideRequestLocations
import com.aralhub.indrive.core.data.model.client.ClientRideRequestLocationsItems
import com.aralhub.indrive.core.data.model.client.ClientRideRequestLocationsItemsCoordinates
import com.aralhub.indrive.core.data.model.client.ClientRideRequestRecommendedAmount
import com.aralhub.indrive.core.data.model.client.GeoPoint
import com.aralhub.indrive.core.data.model.client.RecommendedPrice
import com.aralhub.indrive.core.data.model.payment.PaymentMethod
import com.aralhub.indrive.core.data.model.payment.PaymentMethodType
import com.aralhub.indrive.core.data.model.ride.RecommendedAmount
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.ui.model.RideOptionItem
import com.aralhub.ui.model.args.SelectedLocations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val useCase: ClientCreateRideUseCase,
    private val getAllPaymentMethodsUseCase: GetAllPaymentMethodsUseCase,
    private val getRideOptionsUseCase: GetRideOptionsUseCase,
    private val getRecommendedPriceUseCase: ClientGetRecommendedPriceUseCase
) : ViewModel() {

    private val _paymentMethod = MutableStateFlow(PaymentMethodType.CASH)
    val paymentMethod = _paymentMethod.asStateFlow()
    fun setPaymentMethodType(paymentMethodType: PaymentMethodType) {
        _paymentMethod.value = paymentMethodType
    }

    private var _recommendedPriceUiState = MutableSharedFlow<RecommendedPriceUiState>()
    val recommendedPriceUiState = _recommendedPriceUiState.asSharedFlow()
    fun getRecommendedPrice(geopoints: List<GeoPoint>) = viewModelScope.launch {
        getRecommendedPriceUseCase.invoke(geopoints).let {
            _recommendedPriceUiState.emit(RecommendedPriceUiState.Loading)
            when (it) {
                is Result.Error -> {
                    _recommendedPriceUiState.emit(RecommendedPriceUiState.Error(it.message))
                }

                is Result.Success -> {
                    _recommendedPriceUiState.emit(RecommendedPriceUiState.Success(it.data))
                }
            }
        }
    }


    private var _sendOrderBottomSheetUiState = MutableSharedFlow<SendOrderBottomSheetUiState>()
    val sendOrderBottomSheetUiState = _sendOrderBottomSheetUiState.asSharedFlow()

    fun createRide(baseAmount: Number, recommendedAmount: RecommendedAmount, selectedLocations: SelectedLocations, comment: String, paymentId: Int, options: List<Int>) = viewModelScope.launch {
        _sendOrderBottomSheetUiState.emit(SendOrderBottomSheetUiState.Loading)
        useCase.invoke(
            ClientRideRequest(
                baseAmount = baseAmount,
                recommendedAmount = ClientRideRequestRecommendedAmount(
                    minAmount = recommendedAmount.minAmount,
                    maxAmount = recommendedAmount.maxAmount,
                    recommendedAmount = recommendedAmount.recommendedAmount
                ),
                locations = ClientRideRequestLocations(
                    points = listOf(
                        ClientRideRequestLocationsItems(
                            coordinates = ClientRideRequestLocationsItemsCoordinates(
                                longitude = selectedLocations.from.longitude,
                                latitude = selectedLocations.from.latitude
                            ),
                            name = selectedLocations.from.name
                        ), ClientRideRequestLocationsItems(
                            coordinates = ClientRideRequestLocationsItemsCoordinates(
                                longitude = selectedLocations.to.longitude,
                                latitude = selectedLocations.to.latitude
                            ),
                            name = selectedLocations.to.name
                        )
                    )
                ),
                comment = comment,
                autoTake = false,
                paymentId = paymentId,
                optionIds = options,
                cancelCauseId = null
            )
        ).let {
            when (it) {
                is Result.Error -> {
                    _sendOrderBottomSheetUiState.emit(SendOrderBottomSheetUiState.Error(it.message))
                    Log.i("SendOrderBottomSheetViewModel", "Error: ${it.message}")
                }

                is Result.Success -> {
                    _sendOrderBottomSheetUiState.emit(SendOrderBottomSheetUiState.Success(it.data))
                    Log.i("SendOrderBottomSheetViewModel", "Success: ${it.data}")
                }
            }
        }
    }

    private var _activePaymentMethodUiState = MutableSharedFlow<ActivePaymentMethodUiState>()
    val activePaymentMethodUiState = _activePaymentMethodUiState.asSharedFlow()
    fun getActivePaymentMethods() = viewModelScope.launch {
        _activePaymentMethodUiState.emit(ActivePaymentMethodUiState.Loading)
        getAllPaymentMethodsUseCase.invoke().let {
            when (it) {
                is Result.Error -> {
                    _activePaymentMethodUiState.emit(ActivePaymentMethodUiState.Error(it.message))
                }

                is Result.Success -> {
                    _activePaymentMethodUiState.emit(ActivePaymentMethodUiState.Success(it.data.filter { paymentMethod -> paymentMethod.isActive }))
                }
            }
        }
    }

    private var _rideOptionsUiState = MutableSharedFlow<RideOptionsUiState>()
    val rideOptionsUiState = _rideOptionsUiState.asSharedFlow()
    fun getRideOptions() = viewModelScope.launch {
        _rideOptionsUiState.emit(RideOptionsUiState.Loading)
        getRideOptionsUseCase.invoke().let {
            when (it) {
                is Result.Error -> {
                    _rideOptionsUiState.emit(RideOptionsUiState.Error(it.message))
                }

                is Result.Success -> {
                    _rideOptionsUiState.emit(RideOptionsUiState.Success(it.data.map { rideOption ->
                        RideOptionItem(
                            id = rideOption.id,
                            title = rideOption.name,
                            isEnabled = false
                        )
                    }))
                }
            }
        }
    }
}

sealed interface SendOrderBottomSheetUiState {
    data class Success(val clientRide: ClientRide) : SendOrderBottomSheetUiState
    data class Error(val message: String) : SendOrderBottomSheetUiState
    data object Loading : SendOrderBottomSheetUiState
}

sealed interface ActivePaymentMethodUiState {
    data class Success(val paymentMethods: List<PaymentMethod>) : ActivePaymentMethodUiState
    data class Error(val message: String) : ActivePaymentMethodUiState
    data object Loading : ActivePaymentMethodUiState
}

sealed interface RideOptionsUiState {
    data class Success(val rideOptions: List<RideOptionItem>) : RideOptionsUiState
    data class Error(val message: String) : RideOptionsUiState
    data object Loading : RideOptionsUiState
}

sealed interface RecommendedPriceUiState {
    data class Success(val recommendedPrice: RecommendedPrice) : RecommendedPriceUiState
    data class Error(val message: String) : RecommendedPriceUiState
    data object Loading : RecommendedPriceUiState
}

data class CurrentPrice(
    val price: Int,
    val minPrice: Int,
    val maxPrice: Int
)

sealed interface CurrentPriceUiState {
    data class Success(val currentPrice: CurrentPrice) : CurrentPriceUiState
    data class Error(val message: String) : CurrentPriceUiState
    data object Loading : CurrentPriceUiState
}