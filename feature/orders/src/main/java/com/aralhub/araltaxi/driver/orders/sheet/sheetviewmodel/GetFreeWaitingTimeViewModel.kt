package com.aralhub.araltaxi.driver.orders.sheet.sheetviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.domain.driver.GetWaitTimeUseCase
import com.aralhub.indrive.core.data.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GetFreeWaitingTimeViewModel @Inject constructor(
    private val useCase: GetWaitTimeUseCase
) : ViewModel() {

    private val _getWaitingTime = MutableSharedFlow<Result<Double>>()
    val getWaitingTime: Flow<Result<Double>> = _getWaitingTime

    fun getWaitTime(rideId: Int) {
        viewModelScope.launch {
            _getWaitingTime.emit(useCase(rideId))
        }
    }

}