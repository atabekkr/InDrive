package com.aralhub.indrive.driver.orders.sheet

import android.os.Bundle
import android.view.View
import com.aralhub.indrive.driver.orders.R
import com.aralhub.indrive.driver.orders.databinding.ModalBottomSheetFilterBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterModalBottomSheet: BottomSheetDialogFragment(R.layout.modal_bottom_sheet_filter) {
    private val binding by viewBinding(ModalBottomSheetFilterBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.slider.setOnValueChangeListener { value ->
            binding.tvRadius.text = "$value km"
        }
    }

    companion object {
        const val TAG ="FilterModalBottomSheet"
    }
}