package com.aralhub.ui.sheets

import android.os.Build
import android.os.Bundle
import android.view.View
import com.aralhub.ui.R
import com.aralhub.ui.databinding.BottomSheetClientRideHistoryDetailsBinding
import com.aralhub.ui.model.RideHistoryUI
import com.aralhub.ui.model.args.ShowRideRouteArg
import com.aralhub.ui.utils.formatDateTime
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ClientRideHistoryDetailsBottomSheet : BottomSheetDialogFragment(
    R.layout.bottom_sheet_client_ride_history_details
) {

    private val binding by viewBinding(BottomSheetClientRideHistoryDetailsBinding::bind)
    private var historyDetails: RideHistoryUI? = null

    private var action: (ShowRideRouteArg) -> Unit = {}
    fun setOnShowRouteClickListener(action: (ShowRideRouteArg) -> Unit) {
        this.action = action
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()

    }

    private fun setupListeners() {
        binding.btnShowRoute.setOnClickListener {
            val item = ShowRideRouteArg(
                locations = historyDetails?.locations ?: emptyList()
            )
            action(
                item
            )
        }
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
        tvClientName.text = historyDetails?.userName
        tvCarNumber.text = historyDetails?.carName
        historyDetails?.paymentType?.resId?.let { ivPaymentMethod.setImageResource(it) }
    }

}