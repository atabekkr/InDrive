package com.aralhub.araltaxi.history.driver

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.aralhub.araltaxi.history.driver.databinding.FragmentHistoryBinding
import com.aralhub.ui.sheets.RideHistoryDetailsBottomSheet
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.ViewEx.hide
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val binding by viewBinding(FragmentHistoryBinding::bind)

    private val viewModel by viewModels<RideHistoryViewModel>()

    private val adapter = HistoryAdapter()

    private val rideHistoryDetailsBottomSheet = RideHistoryDetailsBottomSheet()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupData()
        setupListeners()
        setupObservers()

    }

    private fun setupData() {
        binding.rvHistory.adapter = adapter
        binding.rvHistory.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun setupListeners() {
        binding.tbHistory.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        adapter.setOnItemClickListener {
            rideHistoryDetailsBottomSheet.arguments = Bundle().apply {
                putParcelable("RideHistoryDetails", it)
            }
            rideHistoryDetailsBottomSheet.show(
                childFragmentManager,
                rideHistoryDetailsBottomSheet.tag
            )
        }
    }

    private fun setupObservers() {
        observeState(viewModel.getRideHistory) { result ->
            when (result) {
                is RideHistoryUiState.Error -> {
                    Log.e("RideHistoryFragment", result.message)
                }
                is RideHistoryUiState.Idle ->  {}
                is RideHistoryUiState.Loading -> {}
                is RideHistoryUiState.Success -> {
                    binding.tvNotFound.hide()
                    adapter.submitList(result.data)
                }
            }
        }
    }
}