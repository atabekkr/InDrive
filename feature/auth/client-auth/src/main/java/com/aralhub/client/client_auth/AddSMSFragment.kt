package com.aralhub.client.client_auth

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aralhub.client.client_auth.databinding.FragmentAddSmsBinding
import com.aralhub.client.client_auth.navigation.FeatureClientAuthNavigation
import com.aralhub.ui.utils.KeyboardUtils
import com.aralhub.ui.utils.StringUtils
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddSMSFragment : Fragment(R.layout.fragment_add_sms) {
    private val binding by viewBinding(FragmentAddSmsBinding::bind)

    @Inject
    lateinit var navigator: FeatureClientAuthNavigation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val phoneNumber = requireArguments().getString(ARG_PHONE) ?: ""
        val fullText = getString(com.aralhub.ui.R.string.label_confirm_description, phoneNumber)
        binding.tvDescription.text = StringUtils.getBoldSpanString(fullText, phoneNumber, "#001934")
        binding.etPhone.addTextChangedListener {
            binding.btnStart.isEnabled = it.toString().length == 4
            if (binding.btnStart.isEnabled) {
                KeyboardUtils.hideKeyboardFragment(requireContext(), binding.etPhone)
            }
        }
        binding.btnStart.setOnClickListener {
            lifecycleScope.launch {
                delay(3000)
                binding.btnStart.stopProgress()
                navigator.goToAddName()
            }
        }
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