package com.aralhub.ui.sheets

import android.os.Build
import android.os.Bundle
import android.view.View
import com.aralhub.ui.R
import com.aralhub.ui.databinding.BottomSheetRideHistoryDetailsBinding
import com.aralhub.ui.model.RideHistoryUI
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RideHistoryDetailsBottomSheet : BottomSheetDialogFragment(
    R.layout.bottom_sheet_ride_history_details
) {

    private val binding by viewBinding(BottomSheetRideHistoryDetailsBinding::bind)
    private var historyDetails: RideHistoryUI? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()

    }

    private fun setupListeners() {
    }

    private fun setupUI() = binding.apply {
        historyDetails = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("RideHistoryDetails", RideHistoryUI::class.java)
        } else {
            arguments?.getParcelable("RideHistoryDetails")
        }
        tvDate.text = historyDetails?.createdAt
        tvFromLocation.text = historyDetails?.locations?.getOrNull(0)?.name
        tvToLocation.text = historyDetails?.locations?.getOrNull(1)?.name
        tvPrice.text = getString(R.string.standard_uzs_price, historyDetails?.amount.toString())
        tvClientName.text = historyDetails?.amount.toString()
        historyDetails?.paymentType?.resId?.let { ivPaymentMethod.setImageResource(it) }
    }

}