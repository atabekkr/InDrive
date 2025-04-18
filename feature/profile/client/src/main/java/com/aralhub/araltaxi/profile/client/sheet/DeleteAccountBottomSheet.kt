package com.aralhub.araltaxi.profile.client.sheet

import android.os.Bundle
import android.view.View
import com.aralhub.araltaxi.profile.client.R
import com.aralhub.araltaxi.profile.client.databinding.BottomSheetDeleteAccountBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DeleteAccountBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_delete_account) {

    private val binding by viewBinding(BottomSheetDeleteAccountBinding::bind)

    private var action: () -> Unit = {}
    fun setOnDeleteAccountClickListener(action: () -> Unit) {
        this.action = action
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()

    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            dismissAllowingStateLoss()
        }
        binding.btnDelete.setOnClickListener {
            action()
        }
    }
}