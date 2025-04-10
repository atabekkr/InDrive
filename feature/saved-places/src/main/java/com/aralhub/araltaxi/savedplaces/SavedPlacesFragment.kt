package com.aralhub.araltaxi.savedplaces

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.araltaxi.saved_places.R
import com.aralhub.araltaxi.saved_places.databinding.FragmentSavedPlacesBinding
import com.aralhub.araltaxi.savedplaces.navigation.FeatureSavedPlaceNavigation
import com.aralhub.ui.adapter.AddressItemAdapter
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.ViewEx.hide
import com.aralhub.ui.utils.ViewEx.show
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SavedPlacesFragment : Fragment(R.layout.fragment_saved_places) {
    private val binding by viewBinding(FragmentSavedPlacesBinding::bind)
    private val viewModel by viewModels<SavedPlacesViewModel>()
    @Inject
    lateinit var errorHandler: ErrorHandler
    @Inject
    lateinit var navigation: FeatureSavedPlaceNavigation
    private val adapter = AddressItemAdapter()

    companion object {
        private const val LOCATION_OWNER_UNSPECIFIED = -1
        private const val SELECT_LOCATION_REQUEST_KEY = "location_key"
        private const val SELECT_LOCATION_KEY_LATITUDE = "latitude"
        private const val SELECT_LOCATION_KEY_LONGITUDE = "longitude"
        private const val SELECT_LOCATION_KEY_LOCATION_NAME = "locationName"
        private const val SELECT_LOCATION_KEY_LOCATION_ADDRESS = "locationAddress"
        private const val SELECT_LOCATION_KEY_LOCATION_OWNER = "owner"
        private const val NULL_STRING = "null" //locationName can be null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
        viewModel.getAllSavedAddresses()
    }

    private fun initViews() {
        binding.rvSavedPlaces.adapter = adapter
    }

    private fun initObservers() {
        observeState(viewModel.savedPlacesUiState) { savedPlacesUiState ->
            when (savedPlacesUiState) {
                is SavedPlacesUiState.Error -> errorHandler.showToast(savedPlacesUiState.message)
                SavedPlacesUiState.Loading -> {}
                is SavedPlacesUiState.Success -> {
                    if (savedPlacesUiState.addresses.isNotEmpty()) {
                        binding.tvNoSavedPlaces.hide()
                        adapter.submitList(savedPlacesUiState.addresses)
                    } else {
                        binding.tvNoSavedPlaces.show()
                    }
                }
            }
        }
    }

    private fun initListeners() {
        parentFragmentManager.clearFragmentResultListener(SELECT_LOCATION_REQUEST_KEY)
        parentFragmentManager.setFragmentResultListener(
            SELECT_LOCATION_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val latitude = bundle.getDouble(SELECT_LOCATION_KEY_LATITUDE)
            val longitude = bundle.getDouble(SELECT_LOCATION_KEY_LONGITUDE)
            val locationName = bundle.getString(SELECT_LOCATION_KEY_LOCATION_NAME) ?: NULL_STRING
            val locationAddress = bundle.getString(SELECT_LOCATION_KEY_LOCATION_ADDRESS) ?: NULL_STRING
            val locationOwner = bundle.getInt(SELECT_LOCATION_KEY_LOCATION_OWNER)
            if (locationOwner == LOCATION_OWNER_UNSPECIFIED){
                Log.i("SavedPlacesFragment", "Location owner is not specified $latitude  $longitude $locationName, $locationAddress")
                navigation.navigateToSaveAddressFromSavedPlaces(locationName, locationAddress, latitude, longitude)
            }
        }
        adapter.setOnItemClickListener {
            navigation.navigateToEditSavedPlace(it.id)
        }

        binding.tbSavedPlaces.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddPlace.setOnClickListener {
            navigation.navigateToSelectLocationFromSavedPlaces()
            /*viewModel.createAddress(
                address = CreateAddressRequest(
                    userId = 38,
                    name = "Work",
                    address = "Home Address",
                    latitude = 0.0,
                    longitude = 0.0,
                )
            )*/
        }
    }
}