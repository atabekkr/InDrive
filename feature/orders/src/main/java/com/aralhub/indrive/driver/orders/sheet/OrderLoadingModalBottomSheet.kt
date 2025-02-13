package com.aralhub.indrive.driver.orders.sheet

import android.view.View
import com.aralhub.indrive.driver.orders.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OrderLoadingModalBottomSheet: BottomSheetDialogFragment(R.layout.modal_bottom_sheet_order_loading) {
    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
    }

    companion object {
        const val TAG = "OrderLoadingModalBottomSheet"
    }
}