package com.aralhub.araltaxi.request

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aralhub.araltaxi.core.domain.address.GetAllSavedAddressesUseCase
import com.aralhub.indrive.core.data.model.address.Address
import com.aralhub.indrive.core.data.result.fold
import com.aralhub.ui.model.LocationItem
import com.aralhub.ui.model.LocationItemClickOwner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedAddressesViewModel @Inject constructor(
    private val getAllSavedAddressesUseCase: GetAllSavedAddressesUseCase
) : ViewModel() {

    private val _savedPlacesUiState =
        MutableStateFlow<SavedPlacesUiState>(SavedPlacesUiState.Loading)
    val savedPlacesUiState = _savedPlacesUiState.asStateFlow()

    fun getAllSavedAddresses() {
        Log.i("SavedPlacesViewModel", "getAllSavedAddresses")
        viewModelScope.launch {
            _savedPlacesUiState.value = getAllSavedAddressesUseCase().fold(
                onSuccess = {
                    SavedPlacesUiState.Success(it.map { address -> address.toLocationItem() })
                },
                onError = SavedPlacesUiState::Error
            )
        }
    }
}

fun Address.toLocationItem() = LocationItem(
    id = this.id,
    title = this.name,
    subtitle = this.address,
    longitude = this.longitude.toDouble(),
    latitude = this.latitude.toDouble(),
    clickOwner = LocationItemClickOwner.FROM
)

sealed interface SavedPlacesUiState {
    data object Loading : SavedPlacesUiState
    data class Success(val addresses: List<LocationItem>) : SavedPlacesUiState
    data class Error(val message: String) : SavedPlacesUiState
}