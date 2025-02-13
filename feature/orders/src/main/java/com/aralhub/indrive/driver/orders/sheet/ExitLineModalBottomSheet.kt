package com.aralhub.indrive.driver.orders.sheet

import android.os.Bundle
import android.view.View
import com.aralhub.indrive.driver.orders.R
import com.aralhub.indrive.driver.orders.databinding.ModalBottomSheetExitLineBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ExitLineModalBottomSheet(
    private val onExit: () -> Unit
): BottomSheetDialogFragment(R.layout.modal_bottom_sheet_exit_line) {
    private val binding by viewBinding(ModalBottomSheetExitLineBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            dismissAllowingStateLoss()
        }
        binding.btnExit.setOnClickListener {
            onExit.invoke()
            dismissAllowingStateLoss()
        }
    }

    companion object {
        const val TAG ="ExitLineModalBottomSheet"
    }
}