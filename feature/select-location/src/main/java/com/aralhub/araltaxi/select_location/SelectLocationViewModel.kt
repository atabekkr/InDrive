package com.aralhub.araltaxi.select_location

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.select_location.utils.getPointsAt10MeterRadius
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import com.yandex.mapkit.search.Address
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session.SearchListener
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.runtime.Error
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectLocationViewModel @Inject constructor() : ViewModel() {

    private val searchManager =
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    private val point = MutableStateFlow(defaultPoint)
    private val searchState = MutableStateFlow<SearchState>(SearchState.Off)
    private val region = MutableStateFlow<VisibleRegion?>(null)

    val uiState: StateFlow<SelectLocationUiState> =
        combine(point, searchState) { point, searchState ->
            SelectLocationUiState(
                point = point,
                searchState = searchState
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, SelectLocationUiState(defaultPoint))

    fun submitLocation(point: Point, zoom: Int, visibleRegion: VisibleRegion) {
        this.point.value = point
        val locations = getPointsAt10MeterRadius(point.latitude, point.longitude)
        val p = locations.getOrNull(0)?.let { Point(it.lat, point.longitude) }
        searchManager.submit(point, zoom, searchOptions, searchListener)
    }

    private val _locationSelectedUiState = MutableSharedFlow<LocationSelectedUiState>()
    val locationSelectedUiState = _locationSelectedUiState.asSharedFlow()
    fun selectLocation(title: String, subtitle: String, point: Point) = viewModelScope.launch {
        _locationSelectedUiState.emit(LocationSelectedUiState.Success(title, subtitle, point))
    }


    fun setVisibleRegion(region: VisibleRegion) {
        VisibleRegion()
        this.region.value = region
    }

    private val searchListener = object : SearchListener {
        override fun onSearchResponse(response: Response) {
            val items = response.collection.children.mapNotNull {
                val addresses =
                    it.obj?.metadataContainer?.getItem(ToponymObjectMetadata::class.java)?.address?.components!!.filter { comp ->
                        comp.kinds.contains(Address.Component.Kind.HOUSE)
                    }
                Log.e("Address", "${addresses.lastOrNull()}")
                val point = it.obj?.geometry?.firstOrNull()?.point ?: return@mapNotNull null
                SearchResponseItem(point, it.obj)
            }
            val boundingBox = response.metadata.boundingBox ?: return
            searchState.value = SearchState.Success(
                items = items,
                zoomToItems = true,
                itemsBoundingBox = boundingBox
            )
        }

        override fun onSearchError(error: Error) {
            Log.i("Search Response", "Error")
            searchState.value = SearchState.Error
        }
    }

    companion object {
        private val searchOptions = SearchOptions().apply {
            searchTypes = SearchType.GEO.value
            resultPageSize = 5
            geometry = true
        }
        private val defaultPoint = Point(42.4651, 59.6136)

    }
}

sealed interface LocationSelectedUiState {
    data class Success(val title: String, val subtitle: String, val point: Point) :
        LocationSelectedUiState

    data object Error : LocationSelectedUiState
    data object Loading : LocationSelectedUiState
}