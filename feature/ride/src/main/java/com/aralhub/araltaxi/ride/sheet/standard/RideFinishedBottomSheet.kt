package com.aralhub.araltaxi.ride.sheet.standard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aralhub.araltaxi.client.ride.R
import com.aralhub.araltaxi.client.ride.databinding.BottomSheetRideFinishedBinding
import com.aralhub.araltaxi.ride.ActiveRideUiState
import com.aralhub.araltaxi.ride.GetDriverCardUiState
import com.aralhub.araltaxi.ride.RideViewModel
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideBottomSheetNavigation
import com.aralhub.indrive.core.data.model.payment.PaymentMethodType
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.ViewEx.hide
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RideFinishedBottomSheet : Fragment(R.layout.bottom_sheet_ride_finished) {
    private val binding by viewBinding(BottomSheetRideFinishedBinding::bind)
    private val rideViewModel: RideViewModel by activityViewModels()

    @Inject
    lateinit var navigation: FeatureRideBottomSheetNavigation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        binding.layoutPlasticCard.setOnClickListener {
            requireContext().copyToClipboard(binding.tvCardNumber.text)
        }
        binding.btnClear.setOnClickListener {
            navigation.goToRateDriverFromRideFinished()
        }
    }

    private fun initObservers() {
        observeState(rideViewModel.activeRideState) { activeRideUiState ->
            when (activeRideUiState) {
                is ActiveRideUiState.Error -> {}
                ActiveRideUiState.Loading -> {}
                is ActiveRideUiState.Success -> {
                    rideViewModel.getDriverCard(activeRideUiState.activeRide.driver.driverId)
                    val commonPrice = getString(
                        com.aralhub.ui.R.string.standard_uzs_price,
                        "${activeRideUiState.activeRide.amount + activeRideUiState.activeRide.waitAmount}"
                    )
                    binding.tvTotalMoney.text = commonPrice

                    when (activeRideUiState.activeRide.paymentMethod.id) {
                        PaymentMethodType.CARD.id -> {
                            binding.tvPaymentLabel.text =
                                "Ótinish, kelisilgen jol haqın naq pulda tikkeley aydawshınıń ózine tóleń."
                        }

                        PaymentMethodType.CASH.id -> {
                            binding.layoutPlasticCard.hide()
                        }
                    }
                }
            }
        }

        observeState(rideViewModel.getDriverCardUiState) { getDriverCardUiState ->
            when (getDriverCardUiState) {
                is GetDriverCardUiState.Error -> {}
                GetDriverCardUiState.Loading -> {}
                is GetDriverCardUiState.Success -> {
                    binding.tvCardNumber.text = getDriverCardUiState.driverCard.cardNumber
                }
            }
        }
    }

    private fun Context.copyToClipboard(text: CharSequence) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(com.aralhub.ui.R.string.label_card_number), text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            requireContext(),
            getString(com.aralhub.ui.R.string.message_card_number_is_copied),
            Toast.LENGTH_SHORT
        ).show()
    }
}