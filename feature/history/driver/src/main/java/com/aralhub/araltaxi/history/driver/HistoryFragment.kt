package com.aralhub.araltaxi.history.driver

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.aralhub.araltaxi.history.driver.databinding.FragmentHistoryBinding
import com.aralhub.ui.adapter.HistoryAdapter
import com.aralhub.ui.dialog.ErrorMessageDialog
import com.aralhub.ui.dialog.LoadingDialog
import com.aralhub.ui.model.RideHistoryUI
import com.aralhub.ui.sheets.RideHistoryDetailsBottomSheet
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.ViewEx.hide
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val binding by viewBinding(FragmentHistoryBinding::bind)

    private val viewModel by viewModels<RideHistoryViewModel>()

    private val adapter = HistoryAdapter()

    private val rideHistoryDetailsBottomSheet = RideHistoryDetailsBottomSheet()

    private var errorDialog: ErrorMessageDialog? = null
    private var loadingDialog: LoadingDialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        errorDialog = ErrorMessageDialog(context)
        loadingDialog = LoadingDialog(context)
    }

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
            viewModel.getRideHistoryDetails(
                it.rideId
            )
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rideHistoryFlow.collectLatest {
                    binding.tvNotFound.hide()
                    adapter.submitData(it)
                    dismissLoading()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collect { loadStates ->
                    val refreshState = loadStates.refresh
                    val appendState = loadStates.append

                    if (refreshState is LoadState.Error) {
                        showErrorDialog(refreshState.error.localizedMessage)
                    }

                    if (appendState is LoadState.Error) {
                        showErrorDialog(appendState.error.localizedMessage)
                    }

                    if (refreshState is LoadState.Loading) {
                        showLoading()
                    }
                    if (refreshState is LoadState.NotLoading) {
                        dismissLoading()
                    }
                }
            }
        }

        observeState(viewModel.rideHistoryDetailsResult) { result ->
            when (result) {
                is RideDetailsUiState.Error -> showErrorDialog(result.message)
                RideDetailsUiState.Idle -> {}
                RideDetailsUiState.Loading -> showLoading()
                is RideDetailsUiState.Success -> {
                    dismissLoading()
                    showOrderDetails(result.data)
                }
            }
        }

    }

    private fun showOrderDetails(rideHistoryUI: RideHistoryUI) {
        rideHistoryDetailsBottomSheet.arguments = Bundle().apply {
            putParcelable("RideHistoryDetails", rideHistoryUI)
        }
        rideHistoryDetailsBottomSheet.show(
            childFragmentManager,
            rideHistoryDetailsBottomSheet.tag
        )
    }

    private fun showErrorDialog(errorMessage: String?) {
        dismissLoading()
        errorDialog?.setOnDismissClicked { errorDialog?.dismiss() }
        errorDialog?.show(errorMessage)

    }

    private fun showLoading() {
        loadingDialog?.show()
    }

    private fun dismissLoading() {
        loadingDialog?.dismiss()
    }

    private fun dismissErrorDialog() {
        errorDialog?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissErrorDialog()
        dismissLoading()
    }
}