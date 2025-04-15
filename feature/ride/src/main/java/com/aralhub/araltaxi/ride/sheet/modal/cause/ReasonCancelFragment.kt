package com.aralhub.araltaxi.ride.sheet.modal.cause

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.aralhub.araltaxi.client.ride.R
import com.aralhub.araltaxi.client.ride.databinding.FragmentReasonCancelBinding
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.araltaxi.ride.ActiveRideUiState
import com.aralhub.araltaxi.ride.CancelRideUiState
import com.aralhub.araltaxi.ride.RideViewModel
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideNavigation
import com.aralhub.ui.adapter.CancelItemAdapter
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReasonCancelFragment: BottomSheetDialogFragment(R.layout.fragment_reason_cancel){
    private val binding by viewBinding(FragmentReasonCancelBinding::bind)
    private val adapter by lazy { CancelItemAdapter() }
    private val viewModel by viewModels<ReasonCancelViewModel>()
    private val rideViewModel by activityViewModels<RideViewModel>()
    @Inject lateinit var errorHandler: ErrorHandler
    private var rideId = -1

    @Inject
    lateinit var navigation: FeatureRideNavigation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
        viewModel.getCancelCauses()
    }

    private fun initListeners() {
        binding.btnSend.setOnClickListener {
            rideViewModel.cancelRide(rideId)
        }
    }

    private fun initViews() {
        binding.rvReasons.adapter = adapter
    }

    private fun initObservers() {
        observeState(rideViewModel.cancelRideState){ cancelRideUiState ->
            when(cancelRideUiState){
                is CancelRideUiState.Error -> {
                    errorHandler.showToast(cancelRideUiState.message)
                }
                CancelRideUiState.Loading -> {}
                CancelRideUiState.Success -> {
                    if (rideId != -1){
                        val reasonId = adapter.currentList.find { it.isSelected }?.id
                        reasonId?.let { id ->
                            rideViewModel.cancelRideWithReason(rideId, id)
                        } ?: run {
                            navigation.goBackToCreateOfferFromRide()
                            dismissAllowingStateLoss()
                        }
                    } else {
                        errorHandler.showToast("Ride not found")
                    }
                }
            }
        }

        observeState(rideViewModel.cancelRideWithReasonState){cancelRideUiState ->
            when(cancelRideUiState){
                is CancelRideUiState.Error -> {}
                CancelRideUiState.Loading -> {}
                CancelRideUiState.Success -> {
                    dismissAllowingStateLoss()
                    navigation.goBackToCreateOfferFromRide()
                }
            }
        }
        observeState(viewModel.reasonCancelUiState){ reasonCancelUiState ->
            when(reasonCancelUiState){
                is ReasonCancelUiState.Success -> {
                    adapter.submitList(reasonCancelUiState.causes)
                }
                is ReasonCancelUiState.Error -> {
                    errorHandler.showToast(reasonCancelUiState.message)
                }
                ReasonCancelUiState.Loading -> {}
            }
        }

        observeState(rideViewModel.activeRideState) { activeRideUiState ->
            when(activeRideUiState){
                is ActiveRideUiState.Error -> errorHandler.showToast(activeRideUiState.message)
                ActiveRideUiState.Loading -> {}
                is ActiveRideUiState.Success -> rideId = activeRideUiState.activeRide.id
            }
        }
    }

    companion object {
        const val TAG = "ReasonCancelFragment"
    }
}