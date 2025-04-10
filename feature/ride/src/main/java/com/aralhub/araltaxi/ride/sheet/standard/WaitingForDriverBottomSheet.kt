package com.aralhub.araltaxi.ride.sheet.standard

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aralhub.araltaxi.client.ride.R
import com.aralhub.araltaxi.client.ride.databinding.BottomSheetWaitingForDriverBinding
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.araltaxi.ride.ActiveRideUiState
import com.aralhub.araltaxi.ride.CancelRideUiState
import com.aralhub.araltaxi.ride.RideStateUiState
import com.aralhub.araltaxi.ride.RideViewModel
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideBottomSheetNavigation
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideNavigation
import com.aralhub.araltaxi.ride.sheet.modal.CancelTripFragment
import com.aralhub.araltaxi.ride.utils.FragmentEx.sendPhoneNumberToDial
import com.aralhub.indrive.core.data.model.ride.ActiveRide
import com.aralhub.indrive.core.data.model.ride.RideStatus
import com.aralhub.ui.utils.GlideEx.displayAvatar
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.StringUtils
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WaitingForDriverBottomSheet : Fragment(R.layout.bottom_sheet_waiting_for_driver) {
    private val binding by viewBinding(BottomSheetWaitingForDriverBinding::bind)
    private val rideViewModel: RideViewModel by activityViewModels()
    @Inject lateinit var errorHandler: ErrorHandler
    @Inject lateinit var featureRideBottomSheetNavigation: FeatureRideBottomSheetNavigation
    @Inject lateinit var navigation: FeatureRideNavigation
    private var currentRideId = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initListeners()
    }

    private fun initListeners() {
        binding.btnCancel.setOnClickListener {
            currentRideId.let { rideId ->
                CancelTripFragment().show(childFragmentManager, CancelTripFragment.TAG)
            }
        }
    }

    private fun initObservers() {
        observeState(rideViewModel.rideStateUiState){ rideStateUiState ->
            when(rideStateUiState){
                is RideStateUiState.Error -> {
                    Log.i("Ride state", "WaitingForDriver: Error")
                }
                RideStateUiState.Loading -> {
                    Log.i("Ride state", "WaitingForDriver: Loading")
                }
                is RideStateUiState.Success -> {
                    Log.i("Ride State: Waiting For diver", "${rideStateUiState.rideState}")
                    when(rideStateUiState.rideState){
                        is RideStatus.DriverOnTheWay -> {
                            Log.i("Navigation", "driverOnTheWay")
                        }
                        is RideStatus.DriverWaitingClient -> {
                            Log.i("Navigation", "goToDriverIsWaiting")
                            featureRideBottomSheetNavigation.goToDriverIsWaiting()
                        }
                        is RideStatus.PaidWaiting -> {
                            Log.i("Navigation", "paidWaiting")
                        }
                        is RideStatus.PaidWaitingStarted -> {}
                        is RideStatus.RideCompleted -> {
                            featureRideBottomSheetNavigation.goToRideFinishedFromWaitingForDriver()
                        }
                        is RideStatus.RideStarted -> {
                            featureRideBottomSheetNavigation.goToRideFromWaitingForDriver()
                        }
                        is RideStatus.Unknown -> {}
                        is RideStatus.CanceledByDriver -> {
                            Log.i("WaitingForDriver", "initObservers: CanceledByDriver")
                        }
                    }
                }
            }
        }

        observeState(rideViewModel.cancelRideState){ cancelRideUiState ->
            when(cancelRideUiState){
                is CancelRideUiState.Error -> {
                    errorHandler.showToast(cancelRideUiState.message)
                    Log.i("RideBottomSheet", "cancelRideUiState: Error ${cancelRideUiState.message}")
                }
                CancelRideUiState.Loading -> {}
                CancelRideUiState.Success -> {
                    Log.i("RideBottomSheet", "cancelRideUiState: Success")
                }
            }
        }

        observeState(rideViewModel.activeRideState){ activeRideState ->
            when(activeRideState){
                is ActiveRideUiState.Error -> {
                    errorHandler.showToast(activeRideState.message)
                    Log.i("RideBottomSheet", "initObservers: Error ${activeRideState.message}")
                }
                ActiveRideUiState.Loading -> {
                    Log.i("RideBottomSheet", "initObservers: Loading")
                }
                is ActiveRideUiState.Success -> {
                    currentRideId = activeRideState.activeRide.id
                  displayActiveRide(activeRideState.activeRide)
                    Log.i("RideBottomSheet", "initObservers: Success ${activeRideState.activeRide}")
                }
            }
        }
    }

    private fun displayActiveRide(activeRide: ActiveRide) {
        binding.btnCall.setOnClickListener {}
        binding.tvDriverName.text = activeRide.driver.fullName
        displayAvatar("https://araltaxi.aralhub.uz/${activeRide.driver.photoUrl}", binding.ivDriver)
        Log.i("Vehicle", "${activeRide.driver.vehicleType}")
        Log.i("Vehicle", "${activeRide.driver.vehicleNumber}")
        binding.tvCarInfo.text = StringUtils.getBoldSpanString(
            fullText = "${activeRide.driver.vehicleType}, ${activeRide.driver.vehicleNumber}",
            boldText = activeRide.driver.vehicleNumber
        )
        binding.tvFromLocation.text = activeRide.locations.points[0].name
        binding.tvToLocation.text = activeRide.locations.points[activeRide.locations.points.size -1].name
        binding.tvDriverRating.text =  getString(com.aralhub.ui.R.string.label_driver_rating, activeRide.driver.rating)
        binding.btnCall.setOnClickListener {
            sendPhoneNumberToDial(activeRide.driver.phoneNumber)
        }
    }

}