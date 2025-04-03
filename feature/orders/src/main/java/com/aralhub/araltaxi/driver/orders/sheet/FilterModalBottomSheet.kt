package com.aralhub.araltaxi.driver.orders.sheet

import android.os.Bundle
import android.view.View
import com.aralhub.araltaxi.core.common.sharedpreference.DriverSharedPreference
import com.aralhub.araltaxi.driver.orders.R
import com.aralhub.araltaxi.driver.orders.databinding.ModalBottomSheetFilterBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FilterModalBottomSheet : BottomSheetDialogFragment(R.layout.modal_bottom_sheet_filter) {
    private val binding by viewBinding(ModalBottomSheetFilterBinding::bind)

    @Inject
    lateinit var driverSharedPreference: DriverSharedPreference

    private var onClick: () -> Unit = {}
    fun setOnConfirmClickListener(onClick: () -> Unit) {
        this.onClick = onClick
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val distance = (driverSharedPreference.distance.toFloat() / 1000f)
        binding.slider.currentValue = distance

        binding.tvRadius.text = createStringBuilder(distance)

        binding.slider.setOnValueChangeListener { value ->
            binding.tvRadius.text = createStringBuilder(value)
            driverSharedPreference.distance = (value * 1000).toInt()
        }

        binding.btnApply.setOnClickListener {
            onClick()
            dismissAllowingStateLoss()
        }

        binding.tvClearFilter.setOnClickListener {
            driverSharedPreference.distance = 3000
            onClick()
            dismissAllowingStateLoss()
        }

    }

    private fun createStringBuilder(distance: Float) = buildString {
        append(distance).append(" km")
    }

    companion object {
        const val TAG = "FilterModalBottomSheet"
    }
}