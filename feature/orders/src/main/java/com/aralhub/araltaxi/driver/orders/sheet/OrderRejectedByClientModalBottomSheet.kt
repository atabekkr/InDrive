package com.aralhub.araltaxi.driver.orders.sheet

import android.os.Bundle
import android.view.View
import com.aralhub.araltaxi.driver.orders.R
import com.aralhub.araltaxi.driver.orders.databinding.ModalBottomSheetOrderRejectedByClientBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OrderRejectedByClientModalBottomSheet :
    BottomSheetDialogFragment(R.layout.modal_bottom_sheet_order_rejected_by_client) {

    private val binding by viewBinding(ModalBottomSheetOrderRejectedByClientBinding::bind)

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
        behavior.isHideable = false
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initListeners()

    }

    private fun initUI() {
        val offerAmount = arguments?.getString("OfferAmount")
        binding.tvOfferAmount.text = getString(com.aralhub.ui.R.string.not_confirmed_desc, offerAmount)
    }

    private fun initListeners() {
        binding.btnClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

}