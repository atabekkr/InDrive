package com.aralhub.client.client_auth

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aralhub.client.client_auth.databinding.FragmentAddNameBinding
import com.aralhub.client.client_auth.navigation.FeatureClientAuthNavigation
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddNameFragment : Fragment(R.layout.fragment_add_name) {
    private val binding by viewBinding(FragmentAddNameBinding::bind)
    @Inject
    lateinit var navigator: FeatureClientAuthNavigation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etName.addTextChangedListener {
            binding.btnSave.isEnabled = it.toString().isNotEmpty()
        }

        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                delay(4000)
                binding.btnSave.stopProgress()
                navigator.goToRequestFromAddName()
            }
        }

    }
}