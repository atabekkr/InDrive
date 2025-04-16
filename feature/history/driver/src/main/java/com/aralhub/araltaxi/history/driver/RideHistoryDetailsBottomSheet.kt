package com.aralhub.araltaxi.history.driver

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.aralhub.ui.R
import com.aralhub.ui.databinding.BottomSheetRideHistoryDetailsBinding
import com.aralhub.ui.model.RideHistoryUI
import com.aralhub.ui.utils.formatDateTime
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RideHistoryDetailsBottomSheet : BottomSheetDialogFragment(
    R.layout.bottom_sheet_ride_history_details
) {

    private val binding by viewBinding(BottomSheetRideHistoryDetailsBinding::bind)
    private var historyDetails: RideHistoryUI? = null

    private val viewModel by viewModels<RideHistoryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchData()
        setupUI()
        setupListeners()

    }

    private fun fetchData() {
        viewModel
    }

    private fun setupListeners() {
    }

    private fun setupUI() = binding.apply {
        historyDetails = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("RideHistoryDetails", RideHistoryUI::class.java)
        } else {
            arguments?.getParcelable("RideHistoryDetails")
        }
        tvDate.text = historyDetails?.createdAt?.formatDateTime()
        tvFromLocation.text = historyDetails?.locations?.getOrNull(0)?.name
        tvToLocation.text = historyDetails?.locations?.getOrNull(1)?.name
        tvPrice.text = getString(R.string.standard_uzs_price, historyDetails?.ridePrice.toString())
        tvClientName.text = historyDetails?.userName.toString()
        historyDetails?.paymentType?.resId?.let { ivPaymentMethod.setImageResource(it) }
    }

}