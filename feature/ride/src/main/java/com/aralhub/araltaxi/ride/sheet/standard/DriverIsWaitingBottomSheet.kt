package com.aralhub.araltaxi.ride.sheet.standard

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aralhub.araltaxi.client.ride.R
import com.aralhub.araltaxi.client.ride.databinding.BottomSheetDriverIsWaitingBinding
import com.aralhub.araltaxi.ride.ActiveRideUiState
import com.aralhub.araltaxi.ride.GetWaitAmountUiState
import com.aralhub.araltaxi.ride.RideViewModel
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideBottomSheetNavigation
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideNavigation
import com.aralhub.araltaxi.ride.sheet.modal.CancelTripFragment
import com.aralhub.araltaxi.ride.sheet.modal.WaitingTimeFragment
import com.aralhub.araltaxi.ride.utils.FragmentEx.sendPhoneNumberToDial
import com.aralhub.araltaxi.ride.utils.RideTimer
import com.aralhub.indrive.core.data.model.ride.ActiveRide
import com.aralhub.ui.utils.GlideEx.displayAvatar
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.StringUtils
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DriverIsWaitingBottomSheet : Fragment(R.layout.bottom_sheet_driver_is_waiting) {
    private val rideViewModel: RideViewModel by activityViewModels()

    @Inject
    lateinit var featureRideBottomSheetNavigation: FeatureRideBottomSheetNavigation

    @Inject
    lateinit var navigation: FeatureRideNavigation
    private val binding by viewBinding(BottomSheetDriverIsWaitingBinding::bind)
    private val cancelTripFragment = CancelTripFragment()
    private var currentRideId = 0
    private var rideTimer: RideTimer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        rideTimer = RideTimer()
    }

    private fun initListeners() {
        binding.layoutTime.setOnClickListener {
            showWaitingTimeBottomSheet()
        }
        binding.btnCancel.setOnClickListener {
            currentRideId.let { rideId ->
                CancelTripFragment().show(childFragmentManager, CancelTripFragment.TAG)
            }
        }
        observeState(rideViewModel.activeRideState) { activeRideState ->
            when (activeRideState) {
                is ActiveRideUiState.Error -> {
                    Log.i("RideBottomSheet", "initObservers: Error ${activeRideState.message}")
                }

                ActiveRideUiState.Loading -> {
                    Log.i("RideBottomSheet", "initObservers: Loading")
                }

                is ActiveRideUiState.Success -> {
                    currentRideId = activeRideState.activeRide.id
                    rideViewModel.getWaitAmount(activeRideState.activeRide.id)
                    displayActiveRide(activeRideState.activeRide)
                    Log.i("RideBottomSheet", "initObservers: Success ${activeRideState.activeRide}")
                }
            }
        }
        observeState(rideViewModel.getWaitAmountUiState) { getWaitAmountUiState ->
            when (getWaitAmountUiState) {
                is GetWaitAmountUiState.Error -> {}
                GetWaitAmountUiState.Loading -> {}
                is GetWaitAmountUiState.Success -> {
                    Log.i(
                        "RideBottomSheet",
                        "initObservers: Success ${getWaitAmountUiState.waitAmount}"
                    )
                    rideTimer?.let {
                        it.startTimer(
                            getWaitAmountUiState.waitAmount.waitStartTime,
                            getWaitAmountUiState.waitAmount.paidWaitingTime,
                            binding.tvTime
                        )
                    }
                }
            }
        }
    }

    private fun showWaitingTimeBottomSheet() {
        WaitingTimeFragment().show(childFragmentManager, WaitingTimeFragment.TAG)
    }

    private fun displayActiveRide(activeRide: ActiveRide) {
        binding.tvTitle.text = getString(com.aralhub.ui.R.string.label_driver_is_waiting)
        binding.tvDriverName.text = activeRide.driver.fullName
        displayAvatar("https://araltaxi.aralhub.uz/${activeRide.driver.photoUrl}", binding.ivDriver)
        Log.i("Vehicle", "${activeRide.driver.vehicleType}")
        Log.i("Vehicle", "${activeRide.driver.vehicleNumber}")
        binding.tvCarInfo.text = StringUtils.getBoldSpanString(
            fullText = "${activeRide.driver.vehicleType}, ${activeRide.driver.vehicleNumber}",
            boldText = activeRide.driver.vehicleNumber
        )

        binding.tvDriverRating.text =
            getString(com.aralhub.ui.R.string.label_driver_rating, activeRide.driver.rating)
        binding.btnCall.setOnClickListener {
            sendPhoneNumberToDial(activeRide.driver.phoneNumber)
        }
    }

}