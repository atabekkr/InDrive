package com.aralhub.araltaxi.request

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.ui.model.args.LocationType
import com.aralhub.ui.model.args.SelectedLocation
import com.aralhub.ui.model.args.SelectedLocations
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestViewModel2 @Inject constructor() : ViewModel() {

    private companion object {
        const val DEFAULT_ZOOM_LEVEL = 17
        val DEFAULT_LOCATION = SelectedLocation(
            locationType = LocationType.FROM,
            name = "Savıtsky kórkem óner múzeyi",
            latitude = 42.4651, // Example: Nukus, Karakalpakstan
            longitude = 59.6136
        )
    }

    private val searchManager: SearchManager = SearchFactory.getInstance()
        .createSearchManager(SearchManagerType.COMBINED)
    private val searchOptions = SearchOptions().apply {
        searchTypes = SearchType.GEO.value
        resultPageSize = 1
    }

    private val _currentLocationFlow = MutableStateFlow(DEFAULT_LOCATION)
    val currentLocationFlow = _currentLocationFlow.asStateFlow()

    private val _fromLocationFlow = MutableStateFlow<SelectedLocation?>(null)
    val fromLocationFlow = _fromLocationFlow.asStateFlow()

    private val _toLocationFlow = MutableStateFlow<SelectedLocation?>(null)
    val toLocationFlow = _toLocationFlow.asStateFlow()

    private val _navigateToCreateOrderFlow = MutableStateFlow<SelectedLocations?>(null)
    val navigateToCreateOrderFlow = _navigateToCreateOrderFlow.asStateFlow()

    init {
        observeNavigateToCreateOrderFlow()
    }

    fun setToLocation(selectedLocation: SelectedLocation) {
        _fromLocationFlow.value = currentLocationFlow.value
        _toLocationFlow.value = selectedLocation
    }

    fun clearToLocation() = viewModelScope.launch {
        _toLocationFlow.value = null
        _navigateToCreateOrderFlow.emit(null)
    }

    fun setFromLocation(selectedLocation: SelectedLocation) {
        _fromLocationFlow.value = selectedLocation
    }

    private fun observeNavigateToCreateOrderFlow() = viewModelScope.launch {
        combine(fromLocationFlow, toLocationFlow) { _, _ -> }.collect {
            if (fromLocationFlow.value != null && toLocationFlow.value != null) {
                _navigateToCreateOrderFlow.emit(
                    SelectedLocations(
                        fromLocationFlow.value!!,
                        toLocationFlow.value!!
                    )
                )
                _toLocationFlow.value = null
            }
        }
    }

    fun setCurrentLocation(latitude: Double, longitude: Double) {
        fetchCurrentLocationName(latitude, longitude)
    }

    private fun fetchCurrentLocationName(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val point = Point(latitude, longitude)
            searchManager.submit(point,
                DEFAULT_ZOOM_LEVEL,
                searchOptions,
                object : Session.SearchListener {
                    override fun onSearchResponse(response: Response) {
                        val name =
                            response.collection.children.firstNotNullOfOrNull { it.obj?.name }
                                ?: return
                        Log.i("RequestViewModel", "Current Location Name: $name")
                        _currentLocationFlow.value = SelectedLocation(
                            locationType = LocationType.FROM,
                            name = name,
                            latitude = point.latitude,
                            longitude = point.longitude
                        )
                    }

                    override fun onSearchError(error: com.yandex.runtime.Error) {}
                })
        }
    }

}

