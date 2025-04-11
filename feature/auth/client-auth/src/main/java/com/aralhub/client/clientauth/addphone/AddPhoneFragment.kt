package com.aralhub.client.clientauth.addphone

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aralhub.araltaxi.client.auth.R
import com.aralhub.araltaxi.client.auth.databinding.FragmentAddPhoneBinding
import com.aralhub.client.clientauth.navigation.FeatureClientAuthNavigation
import com.aralhub.ui.utils.KeyboardUtils
import com.aralhub.ui.utils.PhoneNumberFormatter
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class AddPhoneFragment : Fragment(R.layout.fragment_add_phone) {

    private val binding by viewBinding(FragmentAddPhoneBinding::bind)

    private val viewModel by viewModels<AddPhoneViewModel>()

    private var _phone: String = ""

    @Inject lateinit var navigator: FeatureClientAuthNavigation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initObservers()

    }

    private fun initObservers() {
        viewModel.addPhoneUiState.onEach { state ->
            when (state) {
                is AddPhoneUiState.Error -> {
                    displayLoading(show = false)
                    displayError(errorMessage = state.message)
                }
                AddPhoneUiState.Loading -> displayLoading(show = true)
                AddPhoneUiState.Success -> {
                    displayLoading(show = false)
                    navigateToAddSMSCode()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun initListeners() {
        binding.btnStart.setOnClickListener {
            _phone = binding.etPhone.text.toString().replace(" ", "")
            sendPhoneNumber()
        }
    }

    private fun initViews() { initPhoneEditText() }

    private fun initPhoneEditText() {
        PhoneNumberFormatter(binding.etPhone) { isFinished ->
            binding.btnStart.isEnabled = isFinished
            if (isFinished) {
                KeyboardUtils.hideKeyboardFragment(
                     context = requireContext(),
                    view = binding.etPhone
                )
            }
        }
    }

    private fun sendPhoneNumber() { viewModel.auth(_phone) }

    private fun navigateToAddSMSCode() { navigator.goToAddSMSCode(phone = _phone) }

    private fun displayLoading(show: Boolean) {
        if (show) {
            binding.btnStart.startProgress()
        } else {
            binding.btnStart.stopProgress()
        }
    }

    private fun displayError(errorMessage: String) { binding.tvError.text = errorMessage }

}