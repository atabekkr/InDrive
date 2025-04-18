package com.aralhub.client.clientauth.addsms

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aralhub.araltaxi.client.auth.R
import com.aralhub.araltaxi.client.auth.databinding.FragmentAddSmsBinding
import com.aralhub.client.clientauth.addphone.AddPhoneViewModel
import com.aralhub.client.clientauth.navigation.FeatureClientAuthNavigation
import com.aralhub.ui.dialog.LoadingDialog
import com.aralhub.ui.utils.StringUtils
import com.aralhub.ui.utils.ViewEx.hide
import com.aralhub.ui.utils.ViewEx.show
import com.aralhub.ui.utils.showKeyboardAndFocus
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddSMSFragment : Fragment(R.layout.fragment_add_sms) {

    private val binding by viewBinding(FragmentAddSmsBinding::bind)
    private var _phone: String = ""
    private val boldTextHex = "#001934"

    private var countDownTimer: CountDownTimer? = null

    private val viewModel by viewModels<AddSMSViewModel>()
    private val addPhoneViewModel by viewModels<AddPhoneViewModel>()

    @Inject
    lateinit var navigator: FeatureClientAuthNavigation

    private var loadingDialog: LoadingDialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadingDialog = LoadingDialog(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initArgs()
        initViews()
        initListeners()
        initObservers()
        startCountDownTimerOfConfirmationCode()

    }

    private fun initObservers() {
        viewModel.addSMSUiState.onEach { state ->
            when (state) {
                is AddSMSUiState.Error -> {
                    displayError(errorMessage = state.message)
                }

                AddSMSUiState.Loading -> {
                    showLoading()
                }

                is AddSMSUiState.Success -> {
                    if (state.isSignedUp)
                        navigateToRequest()
                    else
                        navigateToAddName()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun navigateToAddName() {
        navigator.goToAddName()
    }

    private fun navigateToRequest() {
        navigator.goToRequestFromVerify()
    }

    private fun displayError(errorMessage: String) {
        dismissLoading()
        countDownTimer?.cancel()
        binding.tvError.show()
        binding.tvTimer.hide()
        binding.llResendCode.hide()
        binding.tvError.text = errorMessage

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            binding.etCode.setText("")
        }
    }

    private fun initListeners() {
        binding.tvResendCode.setOnClickListener {
            addPhoneViewModel.auth(_phone)
            startCountDownTimerOfConfirmationCode()
        }

        binding.etCode.addTextChangedListener { code ->
            if (code.toString().length == 5) {
                val trimmedCurrentText = code.toString().trim()
                viewModel.verifyPhone(_phone, trimmedCurrentText)
            }
        }
    }

    private fun initArgs() {
        _phone = requireArguments().getString(ARG_PHONE) ?: ""
    }

    private fun initViews() {
        val fullText = getString(com.aralhub.ui.R.string.label_confirm_description, _phone)
        binding.tvDescription.text = StringUtils.getBoldSpanString(
            fullText = fullText,
            boldText = _phone,
            boldTextColorHex = boldTextHex
        )

        binding.etCode.showKeyboardAndFocus()
    }

    private fun startCountDownTimerOfConfirmationCode(
        millisInFuture: Long = 1 * 60 * 1000,
    ) {
        displayTimer()
        countDownTimer = object : CountDownTimer(
            millisInFuture,
            1000L
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTimer.text =
                    getString(com.aralhub.ui.R.string.resend_code_label, "00:$seconds")
            }

            override fun onFinish() {
                displayResendCode()
            }
        }.start()
    }

    private fun showLoading() {
        loadingDialog?.show()
    }

    private fun dismissLoading() {
        loadingDialog?.dismiss()
    }

    private fun displayTimer() {
        binding.tvError.hide()
        binding.tvTimer.show()
        binding.llResendCode.hide()
    }

    private fun displayResendCode() {
        binding.tvError.hide()
        binding.tvTimer.hide()
        binding.llResendCode.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        countDownTimer = null
        dismissLoading()
    }


    companion object {
        private const val ARG_PHONE = "phone"
        fun args(phone: String): Bundle {
            return Bundle().apply {
                putString(ARG_PHONE, phone)
            }
        }
    }
}