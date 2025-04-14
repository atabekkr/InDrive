package com.aralhub.araltaxi.driver.orders.sheet

import android.os.Bundle
import android.view.View
import com.aralhub.araltaxi.driver.orders.R
import com.aralhub.araltaxi.driver.orders.databinding.ModalBottomSheetCommentBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentModalBottomSheet :
    BottomSheetDialogFragment(R.layout.modal_bottom_sheet_comment) {

    private val binding by viewBinding(ModalBottomSheetCommentBinding::bind)

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initListeners()

    }

    private fun initUI() {
        val comment = arguments?.getString("Comment")
        binding.tvComment.text = comment
    }

    private fun initListeners() {
        binding.btnClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

}