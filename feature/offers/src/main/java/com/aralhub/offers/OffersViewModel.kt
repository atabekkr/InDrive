package com.aralhub.offers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.common.utils.convertIsoToMillis
import com.aralhub.araltaxi.core.domain.client.ClientAcceptOfferUseCase
import com.aralhub.araltaxi.core.domain.client.ClientDeclineOfferUseCase
import com.aralhub.araltaxi.core.domain.client.ClientGetOffersUseCase
import com.aralhub.araltaxi.core.domain.client.ClientGetSearchRideUseCase
import com.aralhub.araltaxi.core.domain.client.CloseOffersWebSocketUseCase
import com.aralhub.indrive.core.data.model.offer.Offer
import com.aralhub.indrive.core.data.model.ride.ActiveRide
import com.aralhub.indrive.core.data.model.ride.SearchRide
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.indrive.core.data.result.fold
import com.aralhub.indrive.core.data.util.ClientWebSocketEvent
import com.aralhub.ui.model.OfferItem
import com.aralhub.ui.model.OfferItemDriver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OffersViewModel @Inject constructor(
    private val getOffersUseCase: ClientGetOffersUseCase,
    private val closeOffersWebSocketUseCase: CloseOffersWebSocketUseCase,
    private val acceptOfferUseCase: ClientAcceptOfferUseCase,
    private val declineOfferUseCase: ClientDeclineOfferUseCase,
    private val getClientGetSearchRideUseCase: ClientGetSearchRideUseCase
) : ViewModel() {

    private var _offersUiState = MutableStateFlow<OffersUiState>(OffersUiState.Loading)
    val offersUiState = _offersUiState.asSharedFlow()

    private var _searchRideUiState = MutableStateFlow<SearchRideUiState>(SearchRideUiState.Loading)
    val searchRideUiState = _searchRideUiState.asStateFlow()

    private var _autoTakeOfferUiState =
        MutableStateFlow<AutoTakeOfferUiState>(AutoTakeOfferUiState.Loading)
    val autoTakeOfferUiState = _autoTakeOfferUiState.asStateFlow()

    init {
        startExpirationChecker()
        getSearchRide()
    }

    fun closeOffersWebSocket() {
        viewModelScope.launch {
            closeOffersWebSocketUseCase()
        }
    }

    private fun getSearchRide() {
        viewModelScope.launch {
            _searchRideUiState.emit(getClientGetSearchRideUseCase().fold(
                onSuccess = {
                    SearchRideUiState.Success(it)
                },
                onError = {
                    SearchRideUiState.Error(it)
                }
            ))
        }
    }

    private fun startExpirationChecker() = viewModelScope.launch {
        while (true) {
            delay(1000L) // Check every second, adjust as needed
            val currentTime = System.currentTimeMillis()
            val hasExpiredOffers = offers.removeIf { offer ->
                val expiresAtMillis = convertIsoToMillis(offer.expiresAt)
                expiresAtMillis <= currentTime
            }
            if (hasExpiredOffers) {
                _offersUiState.emit(OffersUiState.Success(offers.map { offer -> offer.asOfferItem() }))
            }
        }
    }

    private var offers = mutableListOf<Offer>()
    private val declinedOfferIds = mutableListOf<String>()
    fun getOffers() = viewModelScope.launch {
        _offersUiState.emit(OffersUiState.Loading)
        getOffersUseCase().collect {
            when (it) {
                is ClientWebSocketEvent.DriverOffer -> {
                    val newOffer = it.offer
                    if (declinedOfferIds.contains(newOffer.offerId) || isOfferExpired(newOffer)) {
                        return@collect
                    } else if (!offers.any { offer -> offer.offerId == newOffer.offerId }) {
                        offers.add(newOffer)
                    }
                    offers.removeIf { offer ->
                        declinedOfferIds.contains(offer.offerId) || isOfferExpired(
                            offer
                        )
                    }
                    _offersUiState.emit(OffersUiState.Success(offers.map { offer -> offer.asOfferItem() }))
                }

                is ClientWebSocketEvent.Unknown -> {
                    _offersUiState.emit(OffersUiState.Error(it.error))
                }

                is ClientWebSocketEvent.OfferAccepted -> {
                    Log.i("OffersViewModel", "Offer accepted: ${it.ride}")
                    _autoTakeOfferUiState.emit(AutoTakeOfferUiState.Success(it.ride))
                }
            }
        }
    }

    private fun isOfferExpired(offer: Offer): Boolean {
        val expiresAtMillis = convertIsoToMillis(offer.expiresAt)
        return expiresAtMillis <= System.currentTimeMillis()
    }

    private var _acceptOfferUiState = MutableSharedFlow<AcceptOfferUiState>()
    val acceptOfferUiState = _acceptOfferUiState.asSharedFlow()
    fun acceptOffer(offerId: String) = viewModelScope.launch {
        acceptOfferUseCase.invoke(offerId).let {
            when (it) {
                is Result.Success -> {
                    _acceptOfferUiState.emit(AcceptOfferUiState.Success)
                }

                is Result.Error -> {
                    _acceptOfferUiState.emit(AcceptOfferUiState.Error(it.message))
                }
            }
        }
    }

    private var _declineOfferUiState = MutableSharedFlow<DeclineOfferUiState>()
    val declineOfferUiState = _declineOfferUiState.asSharedFlow()
    fun declineOffer(offerId: String, position: Int) = viewModelScope.launch {
        declineOfferUseCase.invoke(offerId).let {
            when (it) {
                is Result.Success -> {
                    declinedOfferIds.add(offerId)
                    offers.removeIf { offer -> offer.offerId == offerId }
                    _offersUiState.emit(OffersUiState.Success(offers.map { offer -> offer.asOfferItem() }))
                    _declineOfferUiState.emit(DeclineOfferUiState.Success(position))
                }

                is Result.Error -> {
                    _declineOfferUiState.emit(DeclineOfferUiState.Error(it.message))
                }
            }
        }
    }
}

private fun Offer.asOfferItem(): OfferItem {
    return OfferItem(
        id = offerId,
        driver = OfferItemDriver(
            id = driver.driverId,
            name = driver.fullName,
            carName = driver.vehicleType.kk,
            rating = driver.rating,
            avatar = "https://araltaxi.aralhub.uz/${driver.photoUrl}"
        ),
        offeredPrice = amount.toInt().toString(),
        timeToArrive = "0",
        expiresAt = expiresAt
    )
}

sealed interface OffersUiState {
    data object Loading : OffersUiState
    data class Success(val offers: List<OfferItem>) : OffersUiState
    data class Error(val message: String) : OffersUiState
}

sealed interface AutoTakeOfferUiState {
    data object Loading : AutoTakeOfferUiState
    data class Success(val activeRide: ActiveRide) : AutoTakeOfferUiState
    data class Error(val message: String) : AutoTakeOfferUiState
}

sealed interface AcceptOfferUiState {
    data object Loading : AcceptOfferUiState
    data object Success : AcceptOfferUiState
    data class Error(val message: String) : AcceptOfferUiState
}

sealed interface DeclineOfferUiState {
    data object Loading : DeclineOfferUiState
    data class Success(val position: Int) : DeclineOfferUiState
    data class Error(val message: String) : DeclineOfferUiState
}

sealed interface SearchRideUiState {
    data class Success(val searchRide: SearchRide) : SearchRideUiState
    data object Loading : SearchRideUiState
    data class Error(val message: String) : SearchRideUiState
}