package com.aralhub.araltaxi.driver.orders.sheet

import android.os.Build
import android.os.Bundle
import android.view.View
import com.aralhub.araltaxi.driver.orders.R
import com.aralhub.araltaxi.driver.orders.databinding.ModalBottomSheetRideFinishedBinding
import com.aralhub.ui.model.RideCompletedUI
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RideFinishedModalBottomSheet :
    BottomSheetDialogFragment(R.layout.modal_bottom_sheet_ride_finished) {

    private val binding by viewBinding(ModalBottomSheetRideFinishedBinding::bind)

    private var rideCompletedUI: RideCompletedUI? = null

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener { dismissAllowingStateLoss() }

        rideCompletedUI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("RideCompletedDetail", RideCompletedUI::class.java)
        } else {
            arguments?.getParcelable("RideCompletedDetail")
        }

        setupUI()

    }

    private fun setupUI() {
        rideCompletedUI?.let {
            binding.tvTitle.text = getString(R.string.ride_completed_income_text, getString(com.aralhub.ui.R.string.standard_uzs_price, it.totalAmount.toString()))
            binding.tvTotalAmount.text =
                getString(com.aralhub.ui.R.string.standard_uzs_price, it.totalAmount.toString())
            binding.tvWaitingAmount.text =
                getString(com.aralhub.ui.R.string.standard_uzs_price, it.waitAmount.toString())
            binding.tvWaitingTime.text = "${(it.duration.toInt() / 60)} min."
            val roadDistanceValue =
                if (it.distance < 1000) "${it.distance.toInt()} m" else "${it.distance.toInt() / 1000} km"
            binding.tvTotalDistance.text = roadDistanceValue
            binding.ivPaymentMethod.setImageResource(it.paymentMethodId.resId)
            binding.tvPaymentMethod.text = it.paymentMethodId.title
            binding.tvPaymentMethodDesc.text = getString(com.aralhub.ui.R.string.label_trip_ended_description_payment_cash, it.paymentMethodId.title)
        }
    }

    companion object {
        const val TAG = "RideFinishedModalBottomSheet"
    }
}