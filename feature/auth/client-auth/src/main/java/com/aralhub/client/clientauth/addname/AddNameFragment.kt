package com.aralhub.client.clientauth.addname

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aralhub.araltaxi.client.auth.R
import com.aralhub.araltaxi.client.auth.databinding.FragmentAddNameBinding
import com.aralhub.client.clientauth.navigation.FeatureClientAuthNavigation
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddNameFragment : Fragment(R.layout.fragment_add_name) {

    private val binding by viewBinding(FragmentAddNameBinding::bind)

    @Inject lateinit var navigator: FeatureClientAuthNavigation

    private val viewModel by viewModels<AddNameViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        initObservers()

    }

    private fun initObservers() {
        viewModel.addNameUiState.onEach { state ->
            when (state) {
                is AddNameUiState.Error -> {
                    displayLoading(false)
                    displayError(state.message)
                }
                AddNameUiState.Loading -> displayLoading(true)
                AddNameUiState.Success -> {
                    displayLoading(false)
                    navigator.goToRequestFromAddName()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun initListeners() {
        binding.etName.addTextChangedListener {
            binding.btnSave.isEnabled = it.toString().length >= 3
        }

        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                val name = binding.etName.text.toString()
                if (name.isNotEmpty() && name.isNotBlank()){
                    viewModel.addName(name)
                }
            }
        }
    }

    private fun displayLoading(show: Boolean) {
        if (show) {
            binding.btnSave.startProgress()
        } else {
            binding.btnSave.stopProgress()
        }
    }

    private fun displayError(errorMessage: String) { binding.tvError.text = errorMessage }
}