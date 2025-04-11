package com.aralhub.araltaxi.request

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.domain.client.ClientGetActiveRideUseCase
import com.aralhub.araltaxi.core.domain.client.ClientGetSearchRideUseCase
import com.aralhub.araltaxi.core.domain.client.ClientLogOutUseCase
import com.aralhub.indrive.core.data.model.ride.ActiveRide
import com.aralhub.indrive.core.data.model.ride.SearchRide
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.indrive.core.data.result.fold
import com.aralhub.ui.model.LocationItem
import com.aralhub.ui.model.LocationItemClickOwner
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SuggestOptions
import com.yandex.mapkit.search.SuggestResponse
import com.yandex.mapkit.search.SuggestSession
import com.yandex.mapkit.search.SuggestType
import com.yandex.runtime.Error
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val clientLogOutUseCase: ClientLogOutUseCase,
    private val clientGetActiveRideUseCase: ClientGetActiveRideUseCase,
    private val clientGetSearchRideUseCase: ClientGetSearchRideUseCase
) : ViewModel() {

    // Search configuration
    private val searchManager: SearchManager = SearchFactory.getInstance()
        .createSearchManager(SearchManagerType.COMBINED)
    private val suggestSession: SuggestSession = searchManager.createSuggestSession()
    private val suggestOptions = SuggestOptions().apply {
        suggestTypes = SuggestType.GEO.value or SuggestType.BIZ.value
    }

    // Karakalpakstan region bounding box
    private companion object {
        const val MIN_LAT = 41.0
        const val MAX_LAT = 44.0
        const val MIN_LON = 56.0
        const val MAX_LON = 61.0
        val KARAKALPAKSTAN_BOUNDS = BoundingBox(
            Point(MIN_LAT, MIN_LON),
            Point(MAX_LAT, MAX_LON)
        )
    }

    // UI States
    private val _suggestionsUiState =
        MutableStateFlow<SuggestionsUiState>(SuggestionsUiState.Loading)
    val suggestionsUiState = _suggestionsUiState.asStateFlow()

    private val _activeRideUiState = MutableSharedFlow<ActiveRideUiState>()
    val activeRideUiState = _activeRideUiState.asSharedFlow()

    private val _searchRideUiState = MutableStateFlow<SearchRideUiState>(SearchRideUiState.Loading)
    val searchRideUiState = _searchRideUiState.asStateFlow()

    private val _logOutUiState = MutableSharedFlow<LogOutUiState>()
    val logOutUiState = _logOutUiState.asSharedFlow()

    init {
        getSearchRide()
        getActiveRide()
    }

    // Location suggestion handling
    fun suggestLocation(query: String, clickOwner: LocationItemClickOwner) {
        viewModelScope.launch {
            suggestSession.suggest(
                query,
                KARAKALPAKSTAN_BOUNDS,
                suggestOptions,
                object : SuggestSession.SuggestListener {
                    override fun onResponse(response: SuggestResponse) {
                        _suggestionsUiState.value = SuggestionsUiState.Loading
                        _suggestionsUiState.value = SuggestionsUiState.Success(
                            response.items
                                .filter {
                                    it.center?.let { pt -> pt.latitude < MAX_LAT || pt.longitude < MAX_LON }
                                        ?: false
                                }
                                .map { item ->
                                    LocationItem(
                                        id = 1,
                                        title = item.title.text ?: "",
                                        subtitle = item.subtitle?.text ?: "",
                                        longitude = item.center?.longitude ?: 0.0,
                                        latitude = item.center?.latitude ?: 0.0,
                                        clickOwner = clickOwner
                                    )
                                }
                        )
                    }

                    override fun onError(error: Error) {
                        _suggestionsUiState.value =
                            SuggestionsUiState.Error(error.isValid.toString())
                    }
                }
            )
        }
    }

    fun logOut() {
        viewModelScope.launch {
            _logOutUiState.emit(LogOutUiState.Loading)
            when (val result = clientLogOutUseCase()) {
                is Result.Success -> _logOutUiState.emit(LogOutUiState.Success)
                is Result.Error -> _logOutUiState.emit(LogOutUiState.Error(result.message))
            }
        }
    }

    private fun getSearchRide() {
        viewModelScope.launch {
            _searchRideUiState.emit(SearchRideUiState.Loading)
            _searchRideUiState.emit(
                clientGetSearchRideUseCase().fold(
                    onSuccess = { SearchRideUiState.Success(it) },
                    onError = { SearchRideUiState.Error(it) }
                )
            )
        }
    }

    private fun getActiveRide() {
        viewModelScope.launch {
            _activeRideUiState.emit(ActiveRideUiState.Loading)
            when (val result = clientGetActiveRideUseCase()) {
                is Result.Success -> {
                    Log.i("RequestViewModel", "getActiveRide: ${result.data}")
                    _activeRideUiState.emit(ActiveRideUiState.Success(result.data))
                }

                is Result.Error -> {
                    Log.i("RequestViewModel", "getActiveRide: ${result.message}")
                    _activeRideUiState.emit(ActiveRideUiState.Error(result.message))
                }
            }
        }
    }
}

// UI State Definitions
sealed interface SuggestionsUiState {
    data object Loading : SuggestionsUiState
    data class Success(val suggestions: List<LocationItem>) : SuggestionsUiState
    data class Error(val message: String) : SuggestionsUiState
}

sealed interface LogOutUiState {
    data object Loading : LogOutUiState
    data object Success : LogOutUiState
    data class Error(val message: String) : LogOutUiState
}

sealed interface SearchRideUiState {
    data object Loading : SearchRideUiState
    data class Success(val searchRide: SearchRide) : SearchRideUiState
    data class Error(val message: String) : SearchRideUiState
}

sealed interface ActiveRideUiState {
    data object Loading : ActiveRideUiState
    data class Success(val activeRide: ActiveRide) : ActiveRideUiState
    data class Error(val message: String) : ActiveRideUiState
}
