package com.aralhub.araltaxi.revenue

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.araltaxi.revenue.databinding.FragmentRevenueBinding
import com.aralhub.indrive.core.data.model.driver.DriverBalanceInfo
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RevenueFragment: Fragment(R.layout.fragment_revenue) {
    private val binding by viewBinding(FragmentRevenueBinding::bind)
    private val viewModel by viewModels<RevenueViewModel>()
    @Inject lateinit var errorHandler: ErrorHandler
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        initObservers()
    }

    private fun initObservers() {
        observeState(viewModel.revenueUiState){ revenueUiState ->
            when(revenueUiState){
                is RevenueUiState.Error -> errorHandler.showToast(revenueUiState.message)
                RevenueUiState.Loading -> {}
                is RevenueUiState.Success -> {
                    displayRevenue(revenueUiState.driverBalanceInfo)
                }
            }
        }
    }

    private fun displayRevenue(driverBalanceInfo: DriverBalanceInfo) {
        binding.tvTodayRevenue.text = driverBalanceInfo.dayBalance.toString()
        binding.tvBalance.text = driverBalanceInfo.balance.toString()
    }

    private fun initListeners() {
        binding.tbRevenue.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
}