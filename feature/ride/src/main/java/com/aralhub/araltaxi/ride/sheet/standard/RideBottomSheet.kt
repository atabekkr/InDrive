package com.aralhub.araltaxi.ride.sheet.standard

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.aralhub.araltaxi.client.ride.R
import com.aralhub.araltaxi.client.ride.databinding.BottomSheetRideBinding
import com.aralhub.araltaxi.ride.ActiveRideUiState
import com.aralhub.araltaxi.ride.RideViewModel
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideBottomSheetNavigation
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideNavigation
import com.aralhub.indrive.core.data.model.payment.PaymentMethodType
import com.aralhub.indrive.core.data.model.ride.ActiveRide
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.ui.utils.GlideEx.displayAvatar
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.StringUtils
import com.aralhub.ui.utils.ViewEx.hide
import com.aralhub.ui.utils.showSnackBar
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RideBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_ride) {

    @Inject
    lateinit var featureRideBottomSheetNavigation: FeatureRideBottomSheetNavigation

    @Inject
    lateinit var navigation: FeatureRideNavigation

    private val rideViewModel: RideViewModel by activityViewModels()

    @Inject
    lateinit var errorHandler: ErrorHandler

    private val binding by viewBinding(BottomSheetRideBinding::bind)
    private var currentRideId = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initListeners()
    }

    private fun initListeners() {
        observeState(rideViewModel.activeRideState) { activeRideState ->
            when (activeRideState) {
                is ActiveRideUiState.Error -> {
                    showSnackBar(activeRideState.message)
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
        observeState(rideViewModel.activeRideState) { activeRideState ->
            when (activeRideState) {
                is ActiveRideUiState.Error -> {
                    showSnackBar(activeRideState.message)
                }

                ActiveRideUiState.Loading -> {
                    Log.i("RideBottomSheet", "initObservers: Loading")
                }

                is ActiveRideUiState.Success -> {
                    currentRideId = activeRideState.activeRide.id
                    displayActiveRide(activeRideState.activeRide)
                }
            }
        }
    }


    private fun displayActiveRide(activeRide: ActiveRide) {
        binding.tvTitle.hide()
        binding.tvPrice.text = activeRide.amount.toString()
        binding.tvPaymentMethod.text = when (activeRide.paymentMethod.type) {
            PaymentMethodType.CARD -> getString(com.aralhub.ui.R.string.label_online_payment)
            PaymentMethodType.CASH -> getString(com.aralhub.ui.R.string.label_cash)
        }
        binding.iconPaymentMethod.setBackgroundResource(
            when (activeRide.paymentMethod.type) {
                PaymentMethodType.CARD -> com.aralhub.ui.R.drawable.ic_credit_card_3d
                PaymentMethodType.CASH -> com.aralhub.ui.R.drawable.ic_cash
            }
        )
        binding.tvDriverName.text = activeRide.driver.fullName
        displayAvatar(activeRide.driver.photoUrl.toString(), binding.ivDriver)
        Log.i("Vehicle", "${activeRide.driver.vehicleType}")
        Log.i("Vehicle", "${activeRide.driver.vehicleNumber}")
        binding.tvCarInfo.text = StringUtils.getBoldSpanString(
            fullText = "${activeRide.driver.vehicleType}, ${activeRide.driver.vehicleNumber}",
            boldText = activeRide.driver.vehicleNumber
        )
        binding.tvToLocation.text =
            activeRide.locations.points[activeRide.locations.points.size - 1].name
        binding.tvDriverRating.text =
            getString(com.aralhub.ui.R.string.label_driver_rating, activeRide.driver.rating)
    }

}