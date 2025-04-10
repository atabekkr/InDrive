package com.aralhub.araltaxi.history.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.aralhub.araltaxi.core.domain.ridehistory.GetRideHistoryUseCase
import com.aralhub.ui.model.RideHistoryUI
import com.aralhub.ui.model.asUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideHistoryViewModel @Inject constructor(
    private val useCase: GetRideHistoryUseCase
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
}