package com.aralhub.araltaxi.ride.sheet.standard

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aralhub.araltaxi.client.ride.R
import com.aralhub.araltaxi.client.ride.databinding.BottomSheetRateDriverBinding
import com.aralhub.araltaxi.ride.ActiveRideUiState
import com.aralhub.araltaxi.ride.CreateReviewUiState
import com.aralhub.araltaxi.ride.RideViewModel
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideBottomSheetNavigation
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideNavigation
import com.aralhub.araltaxi.ride.utils.FragmentEx.loadAvatar
import com.aralhub.indrive.core.data.model.review.PassengerReview
import com.aralhub.indrive.core.data.model.review.Review
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RateDriverBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_rate_driver) {

    private val rideViewModel: RideViewModel by activityViewModels()
    private val binding: BottomSheetRateDriverBinding by viewBinding(BottomSheetRateDriverBinding::bind)
    private var rideId = -1
    private var driverId = -1

    @Inject
    lateinit var navigation: FeatureRideNavigation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        binding.btnSend.setOnClickListener {
            rideViewModel.createReview(
                PassengerReview(
                    driverId,
                    rideId,
                    binding.ratingBar.rating,
                    binding.etComment.text.toString()
                )
            )

        }
    }

    private fun initObservers() {
        observeState(rideViewModel.activeRideState){ activeRideUiState ->
            when(activeRideUiState){
                is ActiveRideUiState.Error -> {}
                ActiveRideUiState.Loading -> {}
                is ActiveRideUiState.Success -> {
                    driverId = activeRideUiState.activeRide.driver.driverId
                    rideId = activeRideUiState.activeRide.id
                    Log.i("RateDriver", "${activeRideUiState.activeRide}")
                    loadAvatar("https://araltaxi.aralhub.uz/${activeRideUiState.activeRide.driver.photoUrl}", binding.ivDriver)
                    binding.tvDriverName.text = activeRideUiState.activeRide.driver.fullName
                }
            }
        }

        observeState(rideViewModel.createReviewUiState){ createReviewUiState ->
            when(createReviewUiState){
                is CreateReviewUiState.Error -> {}
                CreateReviewUiState.Loading -> {}
                is CreateReviewUiState.Success -> {
                    navigation.goBackToCreateOfferFromRide()
                }
            }
        }
    }
}