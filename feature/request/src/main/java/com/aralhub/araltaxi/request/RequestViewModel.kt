package com.aralhub.araltaxi.request

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.domain.client.ClientGetActiveRideUseCase
import com.aralhub.araltaxi.core.domain.client.ClientGetSearchRideUseCase
import com.aralhub.araltaxi.core.domain.client.ClientLogOutUseCase
import com.aralhub.araltaxi.core.domain.client.ClientProfileUseCase
import com.aralhub.indrive.core.data.model.client.ClientProfile
import com.aralhub.indrive.core.data.model.ride.ActiveRide
import com.aralhub.indrive.core.data.model.ride.SearchRide
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.indrive.core.data.result.fold
import com.aralhub.ui.model.LocationItem
import com.aralhub.ui.model.LocationItemClickOwner
import com.aralhub.ui.model.args.LocationType
import com.aralhub.ui.model.args.SelectedLocation
import com.aralhub.ui.model.args.SelectedLocations
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.SuggestOptions
import com.yandex.mapkit.search.SuggestResponse
import com.yandex.mapkit.search.SuggestSession
import com.yandex.mapkit.search.SuggestType
import com.yandex.runtime.Error
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val clientProfileUseCase: ClientProfileUseCase,
    private val clientLogOutUseCase: ClientLogOutUseCase,
    private val clientGetActiveRideUseCase: ClientGetActiveRideUseCase,
    private val clientGetSearchRideUseCase: ClientGetSearchRideUseCase
) : ViewModel() {


    // Search configuration
    private val searchManager: SearchManager = SearchFactory.getInstance()
        .createSearchManager(SearchManagerType.COMBINED)
    private val suggestSession: SuggestSession = searchManager.createSuggestSession()
    private val searchOptions = SearchOptions().apply {
        searchTypes = SearchType.GEO.value
        resultPageSize = 55
    }
    private val suggestOptions = SuggestOptions().apply {
        suggestTypes = SuggestType.BIZ.value
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

    private val _locationEnabled = MutableStateFlow(false)
    val locationEnabled = _locationEnabled.asStateFlow()

    private val _fromLocationUiState = MutableSharedFlow<FromLocationUiState>()
    val fromLocationUiState = _fromLocationUiState.asSharedFlow()

    private val _selectedLocations = MutableStateFlow<SelectedLocations?>(null)
    val selectedLocations: StateFlow<SelectedLocations?> = _selectedLocations.asStateFlow()

    private val _profileUiState = MutableSharedFlow<ProfileUiState>()
    val profileUiState = _profileUiState.asSharedFlow()

    private val _activeRideUiState = MutableSharedFlow<ActiveRideUiState>()
    val activeRideUiState = _activeRideUiState.asSharedFlow()

    private val _searchRideUiState = MutableStateFlow<SearchRideUiState>(SearchRideUiState.Loading)
    val searchRideUiState = _searchRideUiState.asStateFlow()

    private val _logOutUiState = MutableSharedFlow<LogOutUiState>()
    val logOutUiState = _logOutUiState.asSharedFlow()

    // Location tracking
    private val _fromLocation = MutableStateFlow<SelectedLocation?>(null)
    private val _toLocation = MutableStateFlow<SelectedLocation?>(null)
    private var isFromLocationManuallySelected = false
    private var lastLocation = Point(0.0, 0.0)

    init {
        getProfile()
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

    // Location management
    fun updateLocationEnabled(enabled: Boolean) {
        _locationEnabled.value = enabled
    }

    fun updateLocation(location: SelectedLocation) {
        viewModelScope.launch {
            when (location.locationType) {
                LocationType.FROM -> {
                    _fromLocation.value = location
                    isFromLocationManuallySelected = true
                }

                LocationType.TO -> _toLocation.value = location
            }
            updateSelectedLocations()
        }
    }

    fun setFromLocation(latitude: Double, longitude: Double) {
        if (!isFromLocationManuallySelected) {
            lastLocation = Point(latitude, longitude)
            searchName(latitude, longitude)
        }
    }

    fun clearToLocation() {
        _selectedLocations.value = null
        _toLocation.value = null
        _fromLocation.value = null
    }

    private fun searchName(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _fromLocationUiState.emit(FromLocationUiState.Loading)
            searchManager.submit(
                Point(latitude, longitude),
                17,
                searchOptions,
                object : Session.SearchListener {
                    override fun onSearchResponse(response: Response) {
                        val geoObjects = response.collection.children.mapNotNull { it.obj }

                        val name = geoObjects.firstOrNull { it.name != null }?.name
                            ?: "Anıqlap bolmadı"
                        emitFromLocationSuccess(latitude, longitude, name)
                    }

                    override fun onSearchError(error: Error) {
                        emitFromLocationError("Error finding location name")
                    }
                })
        }
    }

    private fun updateSelectedLocations() {
        val from = _fromLocation.value
        val to = _toLocation.value
        if (from != null && to != null) {
            _selectedLocations.value = SelectedLocations(from, to)
        }
    }

    private fun emitFromLocationSuccess(latitude: Double, longitude: Double, name: String) {
        viewModelScope.launch {
            _fromLocationUiState.emit(
                FromLocationUiState.Success(
                    SelectedLocation(
                        locationType = LocationType.FROM,
                        name = name,
                        latitude = latitude,
                        longitude = longitude
                    )
                )
            )
        }
    }

    private fun emitFromLocationError(message: String) {
        viewModelScope.launch {
            _fromLocationUiState.emit(FromLocationUiState.Error(message))
        }
    }

    // API calls
    private fun getProfile() {
        viewModelScope.launch {
            _profileUiState.emit(ProfileUiState.Loading)
            when (val result = clientProfileUseCase()) {
                is Result.Success -> _profileUiState.emit(
                    ProfileUiState.Success(result.data.copy(profilePhoto = "${result.data.profilePhoto}"))
                )

                is Result.Error -> _profileUiState.emit(ProfileUiState.Error(result.message))
            }
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

sealed interface FromLocationUiState {
    data object Loading : FromLocationUiState
    data class Success(val location: SelectedLocation) : FromLocationUiState
    data class Error(val message: String) : FromLocationUiState
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profile: ClientProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
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
