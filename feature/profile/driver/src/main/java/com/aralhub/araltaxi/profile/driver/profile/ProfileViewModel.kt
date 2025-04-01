package com.aralhub.araltaxi.profile.driver.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.domain.driver.DriverProfileUseCase
import com.aralhub.araltaxi.core.domain.driver.DriverProfileWithVehicleUseCase
import com.aralhub.araltaxi.core.domain.driver.DriverUploadProfileImageUseCase
import com.aralhub.araltaxi.core.domain.driver.GetDriverCardUseCase
import com.aralhub.araltaxi.profile.driver.model.toProfileItemList
import com.aralhub.indrive.core.data.model.driver.DriverProfile
import com.aralhub.indrive.core.data.model.driver.DriverProfileWithVehicle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.ui.model.profile.ProfileItem
import com.aralhub.ui.model.profile.ProfileItemCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.io.File

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val driverProfileUseCase: DriverProfileUseCase,
    private val driverCardUseCase: GetDriverCardUseCase,
    private val driverUploadProfileImageUseCase: DriverUploadProfileImageUseCase
) : ViewModel() {

    private var _profileUiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileUiState = _profileUiState.asStateFlow()
    fun getDriverProfile() = viewModelScope.launch {
        _profileUiState.emit(ProfileUiState.Loading)
        driverProfileUseCase().let { result ->
            when (result) {
                is Result.Success -> {
                    _profileUiState.emit(ProfileUiState.Success(result.data.avatar.toString(),
                        result.data.toProfileItemList()))
                }
                is Result.Error -> {
                    _profileUiState.emit(ProfileUiState.Error(result.message))
                }
            }
        }
    }

    private var _cardUiState = MutableStateFlow<CardUiState>(CardUiState.Loading)
    private val cardUiState = _cardUiState.asStateFlow()
    fun getDriverCard() = viewModelScope.launch {
        _cardUiState.emit(CardUiState.Loading)
        driverCardUseCase().let { result ->
            when (result) {
                is Result.Success -> {
                    _cardUiState.emit(CardUiState.Success(listOf(ProfileItem(result.data.cardNumber, result.data.nameOnCard, ProfileItemCategory.CARD))))
                }
                is Result.Error -> {
                    _cardUiState.emit(CardUiState.Error(result.message))
                }
            }
        }
    }

    val combinedUiState = combine(profileUiState, cardUiState) { profileUiState, cardUiState ->
        val profileItems = (profileUiState as? ProfileUiState.Success)?.profileItems ?: emptyList()
        val cardItems = (cardUiState as? CardUiState.Success)?.profileItems ?: emptyList()
        profileItems + cardItems
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uploadImageUiState = MutableSharedFlow<UploadImageUiState>()
    val uploadImageUiState = _uploadImageUiState.asSharedFlow()

    fun uploadImage(file: File) = viewModelScope.launch {
        _uploadImageUiState.emit(UploadImageUiState.Loading)
        _uploadImageUiState.emit(driverUploadProfileImageUseCase(file).let {
            when (it) {
                is Result.Error -> UploadImageUiState.Error(it.message)
                is Result.Success -> UploadImageUiState.Success
            }
        })
    }
}


sealed interface CardUiState {
    data object Loading : CardUiState
    data class Success(val profileItems: List<ProfileItem>) : CardUiState
    data class Error(val message: String) : CardUiState
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val avatar: String, val profileItems: List<ProfileItem>) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

sealed interface UploadImageUiState {
    data object Success : UploadImageUiState
    data class Error(val message: String) : UploadImageUiState
    data object Loading : UploadImageUiState
}