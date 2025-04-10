package com.aralhub.araltaxi.ride.sheet.standard

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aralhub.araltaxi.client.ride.R
import com.aralhub.araltaxi.client.ride.databinding.BottomSheetRideBinding
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.araltaxi.ride.ActiveRideUiState
import com.aralhub.araltaxi.ride.RideStateUiState
import com.aralhub.araltaxi.ride.RideViewModel
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideBottomSheetNavigation
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideNavigation
import com.aralhub.araltaxi.ride.sheet.modal.CancelTripFragment
import com.aralhub.araltaxi.ride.utils.FragmentEx.sendPhoneNumberToDial
import com.aralhub.indrive.core.data.model.payment.PaymentMethodType
import com.aralhub.indrive.core.data.model.ride.ActiveRide
import com.aralhub.indrive.core.data.model.ride.RideStatus
import com.aralhub.ui.utils.GlideEx.displayAvatar
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.StringUtils
import com.aralhub.ui.utils.ViewEx.hide
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RideBottomSheet : Fragment(R.layout.bottom_sheet_ride) {
    @Inject lateinit var featureRideBottomSheetNavigation: FeatureRideBottomSheetNavigation
    @Inject lateinit var navigation: FeatureRideNavigation
    private val rideViewModel: RideViewModel by activityViewModels()
    @Inject lateinit var errorHandler: ErrorHandler
    private val binding by viewBinding(BottomSheetRideBinding::bind)
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
        observeState(rideViewModel.activeRideState){ activeRideState ->
            when(activeRideState){
                is ActiveRideUiState.Error -> {
                    Log.i("RideBottomSheet", "initObservers: Error ${activeRideState.message}")
                }
                ActiveRideUiState.Loading -> {
                    Log.i("RideBottomSheet", "initObservers: Loading")
                }
                is ActiveRideUiState.Success -> {
                    currentRideId = activeRideState.activeRide.id
                    Log.i("RideBottomSheet", "initObservers: Success ${activeRideState.activeRide}")
                }
            }
        }
    }

    private fun initObservers() {
        observeState(rideViewModel.activeRideState){ activeRideState ->
            when(activeRideState){
                is ActiveRideUiState.Error -> {
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
        observeState(rideViewModel.rideStateUiState){ rideStateUiState ->
            when(rideStateUiState){
                is RideStateUiState.Error -> {
                    Log.i("RideFragment", "initObservers: Loading")
                }
                RideStateUiState.Loading -> {
                    Log.i("RideFragment", "initObservers: Loading")
                }
                is RideStateUiState.Success -> {
                    when(rideStateUiState.rideState){
                        is RideStatus.DriverOnTheWay -> {}
                        is RideStatus.DriverWaitingClient -> {}
                        is RideStatus.PaidWaiting ->{}
                        is RideStatus.PaidWaitingStarted ->{}
                        is RideStatus.RideCompleted -> {
                            featureRideBottomSheetNavigation.goToRideFinished()
                        }
                        is RideStatus.RideStarted -> {}
                        is RideStatus.Unknown -> {}
                        is RideStatus.CanceledByDriver -> {
                            Log.i("RideBottomSheet", "initObservers: CanceledByDriver")

                        }
                    }
                }
            }
        }
    }


    private fun displayActiveRide(activeRide: ActiveRide) {
        binding.tvTitle.hide()
        binding.btnCall.setOnClickListener {}
        binding.tvPrice.text = activeRide.amount.toString()
        binding.tvPaymentMethod.text = when(activeRide.paymentMethod.type){
            PaymentMethodType.CARD -> getString(com.aralhub.ui.R.string.label_online_payment)
            PaymentMethodType.CASH -> getString(com.aralhub.ui.R.string.label_cash)
        }
        binding.iconPaymentMethod.setBackgroundResource(
            when(activeRide.paymentMethod.type){
                PaymentMethodType.CARD -> com.aralhub.ui.R.drawable.ic_credit_card_3d
                PaymentMethodType.CASH -> com.aralhub.ui.R.drawable.ic_cash
            }
        )
        binding.tvDriverName.text = activeRide.driver.fullName
        displayAvatar("https://araltaxi.aralhub.uz/${activeRide.driver.photoUrl}", binding.ivDriver)
        Log.i("Vehicle", "${activeRide.driver.vehicleType}")
        Log.i("Vehicle", "${activeRide.driver.vehicleNumber}")
        binding.tvCarInfo.text = StringUtils.getBoldSpanString(
            fullText = "${activeRide.driver.vehicleType}, ${activeRide.driver.vehicleNumber}",
            boldText = activeRide.driver.vehicleNumber
        )
        binding.tvToLocation.text = activeRide.locations.points[activeRide.locations.points.size -1].name
        binding.tvDriverRating.text =  getString(com.aralhub.ui.R.string.label_driver_rating, activeRide.driver.rating)
        binding.btnCall.setOnClickListener {
            sendPhoneNumberToDial(activeRide.driver.phoneNumber)
        }
    }

}