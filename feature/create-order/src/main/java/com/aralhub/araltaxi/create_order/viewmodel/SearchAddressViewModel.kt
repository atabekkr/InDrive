package com.aralhub.araltaxi.create_order.viewmodel

import androidx.lifecycle.ViewModel
import com.aralhub.ui.model.args.SelectedLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SearchAddressViewModel @Inject constructor() : ViewModel() {

    private val _fromLocationFlow = MutableStateFlow<SelectedLocation?>(null)
    val fromLocationFlow = _fromLocationFlow.asStateFlow()

    private val _toLocationFlow = MutableStateFlow<SelectedLocation?>(null)
    val toLocationFlow = _toLocationFlow.asStateFlow()

    fun setToLocation(selectedLocation: SelectedLocation) {
        _toLocationFlow.value = selectedLocation
    }

    fun setFromLocation(selectedLocation: SelectedLocation) {
        _fromLocationFlow.value = selectedLocation
    }

}