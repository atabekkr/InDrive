package com.aralhub.indrive.driver.orders.sheet

import android.os.Bundle
import android.view.View
import com.aralhub.indrive.driver.orders.R
import com.aralhub.indrive.driver.orders.databinding.ModalBottomSheetRideBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RideModalBottomSheet: BottomSheetDialogFragment(R.layout.modal_bottom_sheet_ride) {
    private val binding by viewBinding(ModalBottomSheetRideBinding::bind)

    private var rideFinishedListener: () -> Unit = {}
    fun setOnRideFinishedListener(onRideFinished: () -> Unit) {
        this.rideFinishedListener = onRideFinished
    }

    private var rideCanceledListener: () -> Unit = {}
    fun setOnRideCanceledListener(onRideCanceled: () -> Unit) {
        this.rideCanceledListener = onRideCanceled
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener {
            rideCanceledListener.invoke()
        }
        binding.slideButtonFinish.setOnSlideChangeListener {
            if (it == 1f) {
                rideFinishedListener.invoke()
            }
        }
    }

    companion object {
        const val TAG = "RideModalBottomSheet"
    }
}