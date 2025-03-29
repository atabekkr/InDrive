package com.aralhub.araltaxi.driver.orders.sheet

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.aralhub.araltaxi.core.common.utils.rejectOfferState
import com.aralhub.araltaxi.driver.orders.R
import com.aralhub.araltaxi.driver.orders.databinding.ModalBottomSheetOrderLoadingBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class OrderLoadingModalBottomSheet :
    BottomSheetDialogFragment(R.layout.modal_bottom_sheet_order_loading) {

    private val binding by viewBinding(ModalBottomSheetOrderLoadingBinding::bind)

    private val orderRejectedByClientModalBottomSheet = OrderRejectedByClientModalBottomSheet()

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
        initObservers()

    }

    private fun initUI() {
        val offerAmount = arguments?.getString("OfferAmount")
        Log.d(TAG, offerAmount.toString())
        binding.tvOfferAmount.text = getString(R.string.driver_offer_loading, offerAmount)
    }

    private fun initObservers() {
        rejectOfferState.onEach {
            Log.d(TAG, "initObservers: $it")
            if (!orderRejectedByClientModalBottomSheet.isAdded) {
                orderRejectedByClientModalBottomSheet.arguments = arguments
                orderRejectedByClientModalBottomSheet.show(
                    requireActivity().supportFragmentManager,
                    orderRejectedByClientModalBottomSheet.tag
                )
            }
            dismissAllowingStateLoss()
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    companion object {
        const val TAG = "OrderLoadingModalBottomSheet"
    }
}