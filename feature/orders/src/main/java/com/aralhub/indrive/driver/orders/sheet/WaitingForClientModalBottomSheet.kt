package com.aralhub.indrive.driver.orders.sheet

import android.os.Bundle
import android.view.View
import com.aralhub.indrive.driver.orders.R
import com.aralhub.indrive.driver.orders.databinding.ModalBottomSheetWaitingForClientBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WaitingForClientModalBottomSheet :
    BottomSheetDialogFragment(R.layout.modal_bottom_sheet_waiting_for_client) {
    private val binding by viewBinding(ModalBottomSheetWaitingForClientBinding::bind)

    private var onGoingToRideListener: () -> Unit = {}
    fun setOnGoingToRideListener(onGoingToRide: () -> Unit) {
        this.onGoingToRideListener = onGoingToRide
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnGoToRide.setOnClickListener {
            onGoingToRideListener.invoke()
        }
    }

    companion object {
        const val TAG = "WaitingForClientModalBottomSheet"
    }
}