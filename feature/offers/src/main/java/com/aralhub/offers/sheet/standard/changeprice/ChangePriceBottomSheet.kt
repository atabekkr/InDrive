package com.aralhub.offers.sheet.standard.changeprice

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aralhub.araltaxi.client.offers.R
import com.aralhub.araltaxi.client.offers.databinding.BottomSheetChangePriceBinding
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangePriceBottomSheet: Fragment(R.layout.bottom_sheet_change_price) {
    private val binding by viewBinding(BottomSheetChangePriceBinding::bind)
    @Inject lateinit var errorHandler: ErrorHandler
    private var searchRideId = ARG_RIDE_ID_DEFAULT
    private val viewModel by viewModels<ChangePriceViewModel>()
    private var recommendedPrice = 0
    private var maxPrice = 0
    private var minPrice = 0
    private var currentPrice = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initArgs()
        initListeners()
        initObservers()
        viewModel.getSearchRide()
    }

    private fun initArgs() {}

    private fun initListeners() {
       binding.btnChange.setOnClickListener {
           viewModel.changePrice(searchRideId, binding.etPrice.text.toString().toInt())
       }
        binding.tvDecrease500.setOnClickListener {
            val price = binding.etPrice.text.toString().toInt() -500
            if (minPrice != 0 && currentPrice > minPrice){
                currentPrice = price
                binding.etPrice.text = "$price"
            }
        }

        binding.tvIncrease500.setOnClickListener {
            val price= binding.etPrice.text.toString().toInt() + 500
            if (maxPrice != 0 && currentPrice < maxPrice){
                currentPrice = price
                binding.etPrice.text = "$price"
            }
        }
    }

    private fun initObservers() {
        observeState(viewModel.changePriceUiState){ changePriceUiState ->
            when(changePriceUiState){
                is ChangePriceUiState.Error -> errorHandler.showToast(changePriceUiState.message)
                ChangePriceUiState.Loading -> {}
                is ChangePriceUiState.Success -> {
                    findNavController().navigateUp()
                }
            }
        }
        observeState(viewModel.searchRideUiState){ searchRideUiState ->
            Log.i("SearchRideUiState", searchRideUiState.toString())
            when(searchRideUiState){
                is SearchRideUiState.Error -> errorHandler.showToast(searchRideUiState.message)
                SearchRideUiState.Loading -> {}
                is SearchRideUiState.Success -> {
                    searchRideId = searchRideUiState.data.uuid
                    currentPrice = if (searchRideUiState.data.updatedAmount == null || searchRideUiState.data.updatedAmount == 0) {
                        searchRideUiState.data.baseAmount.toInt()
                    } else {
                        searchRideUiState.data.updatedAmount!!.toInt()

                    }
                    recommendedPrice = searchRideUiState.data.recommendedAmount.recommendedAmount.toInt()
                    maxPrice = searchRideUiState.data.recommendedAmount.maxAmount.toInt()
                    minPrice = searchRideUiState.data.recommendedAmount.minAmount.toInt()
                    binding.tvPrice.text = "Usınıs etilgen baha $recommendedPrice"
                    binding.etPrice.text = "$currentPrice"
                }
            }
        }
    }

    companion object {
        private const val ARG_RIDE_ID = "rideId"
        private const val ARG_RIDE_ID_DEFAULT = "rideId"
        fun args(searchRideId: String) = Bundle().apply {
            putString(ARG_RIDE_ID, searchRideId)
        }
    }
}