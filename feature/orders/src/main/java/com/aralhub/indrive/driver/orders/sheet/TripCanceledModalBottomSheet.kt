package com.aralhub.indrive.driver.orders.sheet

import android.os.Bundle
import android.view.View
import com.aralhub.indrive.driver.orders.R
import com.aralhub.indrive.driver.orders.databinding.ModalBottomSheetTripCanceledBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TripCanceledModalBottomSheet :
    BottomSheetDialogFragment(R.layout.modal_bottom_sheet_trip_canceled) {
    private val binding by viewBinding(ModalBottomSheetTripCanceledBinding::bind)

    private var onBackListener: () -> Unit = {}
    fun setOnBackListener(onBack: () -> Unit) {
        this.onBackListener = onBack
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClose.setOnClickListener {
            onBackListener.invoke()
        }
    }
    companion object {
        const val TAG = "TripCanceledModalBottomSheet"
    }
}