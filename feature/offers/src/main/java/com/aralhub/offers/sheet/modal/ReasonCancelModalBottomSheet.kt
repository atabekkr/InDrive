package com.aralhub.offers.sheet.modal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.aralhub.araltaxi.client.offers.R
import com.aralhub.araltaxi.client.offers.databinding.ModalBottomSheetReasonCancelBinding
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.ui.adapter.CancelItemAdapter
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReasonCancelModalBottomSheet: BottomSheetDialogFragment(R.layout.modal_bottom_sheet_reason_cancel) {
    private val binding by viewBinding(ModalBottomSheetReasonCancelBinding::bind)
    private val viewModel by viewModels<ReasonCancelModalBottomSheetViewModel>()
    private val adapter = CancelItemAdapter()
    @Inject lateinit var errorHandler: ErrorHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initObservers()
        initListeners()
        viewModel.getCancelCauses()
    }

    private fun initListeners() {
        binding.btnSend.setOnClickListener {
        }
    }

    private fun initViews() {
        binding.rvReasons.adapter = adapter
    }

    private fun initObservers() {
        observeState(viewModel.cancelRideUiState){
            when(it){
                is CancelRideUiState.Error -> errorHandler.showToast(it.message)
                CancelRideUiState.Loading -> {}
                is CancelRideUiState.Success -> {
                    adapter.submitList(it.cancelCauses)
                }
            }
        }
    }

    companion object {
        const val TAG = "ReasonCancelModalBottomSheet"
    }
}