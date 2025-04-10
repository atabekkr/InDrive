package com.aralhub.offers.sheet.standard.changeorcancel

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.aralhub.araltaxi.client.offers.R
import com.aralhub.araltaxi.client.offers.databinding.BottomSheetChangeOrCancelRequestBinding
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.offers.OffersUiState
import com.aralhub.offers.OffersViewModel
import com.aralhub.offers.navigation.FeatureOffersNavigation
import com.aralhub.offers.navigation.sheet.FeatureOffersBottomSheetNavigation
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.ViewEx.hide
import com.aralhub.ui.utils.ViewEx.show
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeOrCancelRequestBottomSheet : Fragment(R.layout.bottom_sheet_change_or_cancel_request) {
    private val binding by viewBinding(BottomSheetChangeOrCancelRequestBinding::bind)

    @Inject
    lateinit var featureOffersBottomSheetNavigation: FeatureOffersBottomSheetNavigation

    @Inject
    lateinit var errorHandler: ErrorHandler
    private var searchRideId = SEARCH_ID_DEFAULT
    @Inject
    lateinit var navigation: FeatureOffersNavigation
    private val offersViewModel by activityViewModels<OffersViewModel>()
    private val viewModel by viewModels<ChangeOrCancelRequestViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        initObservers()
        viewModel.getSearchRide()
    }

    private fun initObservers() {
        var isOfferCanceled = false
        observeState(viewModel.cancelSearchRideUiState) { cancelSearchRideUiState ->
            when (cancelSearchRideUiState) {
                is CancelSearchRideUiState.Error -> errorHandler.showToast(cancelSearchRideUiState.message)
                CancelSearchRideUiState.Loading -> {}
                CancelSearchRideUiState.Success -> {
                    if (!isOfferCanceled) {
                        navigation.goBackToRequestFragmentFromOffersFragment()
                        isOfferCanceled = true
                    }
                }
            }
        }

        observeState(offersViewModel.offersUiState){ offersUiState ->
            when(offersUiState){
                is OffersUiState.Error -> {

                }
                OffersUiState.Loading -> {
                    binding.tvTitle.show()
                }
                is OffersUiState.Success -> {
                    if (offersUiState.offers.isEmpty()) {
                        binding.tvTitle.show()
                    } else {
                        binding.tvTitle.hide()
                    }
                }
            }
        }

        observeState(viewModel.searchRideUiState) { searchRideUiState ->
            when (searchRideUiState) {
                is SearchRideUiState.Error -> errorHandler.showToast(searchRideUiState.message)
                SearchRideUiState.Loading -> {}
                is SearchRideUiState.Success -> {
                    searchRideId = searchRideUiState.searchRide.uuid
                    Log.i("SearchRide", "${searchRideUiState.searchRide}")
                    if (searchRideUiState.searchRide.updatedAmount == null || searchRideUiState.searchRide.updatedAmount == 0) {
                        binding.tvAutoTakeDescription.text =
                            "Eń jaqın aydawshınıń ${searchRideUiState.searchRide.baseAmount.toInt()} somǵa shekemgi bolǵan usınısın avtomat túrde qabıllaw"
                    } else {
                        binding.tvAutoTakeDescription.text =
                            "Eń jaqın aydawshınıń ${searchRideUiState.searchRide.updatedAmount!!.toInt()} somǵa shekemgi bolǵan usınısın avtomat túrde qabıllaw"
                    }

                }
            }
        }

        observeState(viewModel.updateAutoTakeUiState) { updateAutoTakeUiState ->
            when (updateAutoTakeUiState) {
                is UpdateAutoTakeUiState.Error -> {
                    errorHandler.showToast(updateAutoTakeUiState.message)
                    binding.autoTakeToggle.setChecked(false)
                }

                UpdateAutoTakeUiState.Loading -> {}
                UpdateAutoTakeUiState.Success -> {}
            }
        }
    }

    private fun initListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, true) {
            viewModel.cancelSearchRide(searchRideId)
        }

        binding.autoTakeToggle.setOnCheckedListener { isChecked ->
            if (searchRideId != SEARCH_ID_DEFAULT) {
                viewModel.updateAutoTake(searchRideId, isChecked)
            } else {
                errorHandler.showToast("Ride is not available")
            }
        }

        binding.btnCancel.setOnClickListener {
            Log.i("SearchId Cancel", searchRideId)
            viewModel.cancelSearchRide(searchRideId)
        }

        binding.btnChange.setOnClickListener {
            if (searchRideId != SEARCH_ID_DEFAULT) {
                featureOffersBottomSheetNavigation.goToChangePriceFragment(searchRideId)
            } else {
                errorHandler.showToast("Ride is not available")
            }
        }
    }

    companion object {
        private const val SEARCH_ID_DEFAULT = "searchId"
    }
}