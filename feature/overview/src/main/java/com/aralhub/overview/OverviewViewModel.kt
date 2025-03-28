package com.aralhub.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.domain.driver.DriverLogoutUseCase
import com.aralhub.araltaxi.core.domain.driver.DriverProfileUseCase
import com.aralhub.araltaxi.core.domain.driver.GetActiveRideByDriverUseCase
import com.aralhub.araltaxi.core.domain.driver.GetDriverBalanceUseCase
import com.aralhub.indrive.core.data.model.driver.DriverInfo
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.overview.mapper.asUI
import com.aralhub.ui.model.GetActiveRideByDriverUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val driverProfileUseCase: DriverProfileUseCase,
    private val driverBalanceUseCase: GetDriverBalanceUseCase,
    private val driverLogoutUseCase: DriverLogoutUseCase,
    private val getActiveRideByDriverUseCase: GetActiveRideByDriverUseCase
) : ViewModel() {

    init {
        getActiveRide()
    }

    private val _profileUiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileUiState = _profileUiState.asStateFlow()
    fun getProfile() = viewModelScope.launch {
        _profileUiState.emit(driverProfileUseCase().let {
            when (it) {
                is Result.Error -> ProfileUiState.Error(it.message)
                is Result.Success -> ProfileUiState.Success(it.data)
            }
        })
    }

    private val _balanceUiState = MutableStateFlow<DriverBalanceUiState>(DriverBalanceUiState.Loading)
    val balanceUiState = _balanceUiState.asSharedFlow()
    fun getBalance() = viewModelScope.launch {
        _balanceUiState.emit(driverBalanceUseCase().let {
            when (it) {
                is Result.Error -> DriverBalanceUiState.Error(it.message)
                is Result.Success -> DriverBalanceUiState.Success(
                    it.data.balance.toInt(),
                    it.data.dailyBalance.toInt()
                )
            }
        })
    }

    private val _logoutUiState = MutableSharedFlow<LogoutUiState>()
    val logoutUiState = _logoutUiState.asSharedFlow()
    fun logout() = viewModelScope.launch {
        _logoutUiState.emit(LogoutUiState.Loading)
        _logoutUiState.emit(driverLogoutUseCase().let {
            when (it) {
                is Result.Error -> LogoutUiState.Error(it.message)
                is Result.Success -> LogoutUiState.Success
            }
        })
    }

    private var _activeOrdersUiState = MutableSharedFlow<GetActiveRideByDriverUI?>()
    val activeOrdersUiState = _activeOrdersUiState.asSharedFlow()
    private fun getActiveRide() {
        viewModelScope.launch {
            getActiveRideByDriverUseCase().let { result ->
                when (result) {
                    is Result.Error -> {}
                    is Result.Success -> _activeOrdersUiState.emit(
                        (GetActiveRideByDriverUI(
                            order = result.data?.data?.asUI(),
                            status = result.data?.status
                        ))
                    )
                }
            }
        }
    }

}

sealed interface LogoutUiState {
    data object Success : LogoutUiState
    data class Error(val message: String) : LogoutUiState
    data object Loading : LogoutUiState
}

sealed interface DriverBalanceUiState {
    data class Success(val balance: Int, val dayBalance: Int) : DriverBalanceUiState
    data class Error(val message: String) : DriverBalanceUiState
    data object Loading : DriverBalanceUiState
}

sealed interface ProfileUiState {
    data class Success(val profile: DriverInfo) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data object Loading : ProfileUiState
}