package com.aralhub.araltaxi.history.client

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.araltaxi.history.client.databinding.FragmentHistoryBinding
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {
    private val binding by viewBinding(FragmentHistoryBinding::bind)
    private val viewModel by viewModels<HistoryViewModel>()
    @Inject lateinit var errorHandler: ErrorHandler
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initListeners()
   }

    private fun initListeners() {
        binding.tbHistory.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun initObservers() {
    }
}