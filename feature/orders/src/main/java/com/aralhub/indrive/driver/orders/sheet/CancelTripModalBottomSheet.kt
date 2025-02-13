package com.aralhub.indrive.driver.orders.sheet

import android.os.Bundle
import android.view.View
import com.aralhub.indrive.driver.orders.R
import com.aralhub.indrive.driver.orders.databinding.ModalBottomSheetCancelTripBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CancelTripModalBottomSheet: BottomSheetDialogFragment(R.layout.modal_bottom_sheet_cancel_trip) {
    private val binding by viewBinding(ModalBottomSheetCancelTripBinding::bind)

    private var onCancelTripListener: () -> Unit = {}
    fun setOnCancelTripListener(onCancelTrip: () -> Unit) {
        this.onCancelTripListener = onCancelTrip
    }

    private var onBackListener: () -> Unit = {}
    fun setOnBackListener(onBack: () -> Unit) {
        this.onBackListener = onBack
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            onBackListener.invoke()
        }
        binding.btnCancel.setOnClickListener {
            onCancelTripListener.invoke()
        }
    }
    companion object {
        const val TAG = "CancelTripModalBottomSheet"
    }
}