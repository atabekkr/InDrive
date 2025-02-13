package com.aralhub.indrive.driver.orders.sheet

import android.os.Bundle
import android.view.View
import com.aralhub.indrive.driver.orders.R
import com.aralhub.indrive.driver.orders.databinding.ModalBottomSheetGoingToPickUpBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GoingToPickUpModalBottomSheet : BottomSheetDialogFragment(R.layout.modal_bottom_sheet_going_to_pick_up) {
    private val binding by viewBinding(ModalBottomSheetGoingToPickUpBinding::bind)
    private var onClientPickedUp: () -> Unit = {}
    fun setOnClientPickedUp(onPickedUp: () -> Unit) {
        this.onClientPickedUp = onPickedUp
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.slideArrived.setOnSlideChangeListener {
            if (it == 1f) {
                onClientPickedUp.invoke()
            }
        }
    }

    companion object {
        const val TAG = "GoingToPickUpModalBottomSheet"
    }
}