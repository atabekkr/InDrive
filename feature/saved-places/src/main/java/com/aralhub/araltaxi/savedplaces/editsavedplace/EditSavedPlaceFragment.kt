package com.aralhub.araltaxi.savedplaces.editsavedplace

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.araltaxi.saved_places.R
import com.aralhub.araltaxi.saved_places.databinding.FragmentEditSavedPlaceBinding
import com.aralhub.araltaxi.savedplaces.sheet.DeleteSavedAddressModalBottomSheet
import com.aralhub.indrive.core.data.model.address.Address
import com.aralhub.indrive.core.data.model.address.CreateAddressRequest
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class EditSavedPlaceFragment: Fragment(R.layout.fragment_edit_saved_place) {
    private var savedPlaceId: Int = DEFAULT_SAVED_PLACE_ID
    @Inject lateinit var errorHandler: ErrorHandler
    private val viewModel by viewModels<EditSavedPlaceViewModel>()
    private val deleteSavedAddressModalBottomSheet by lazy { DeleteSavedAddressModalBottomSheet() }
    private val binding by viewBinding(FragmentEditSavedPlaceBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initArgs()
        initObservers()
        initListeners()

    }

    private fun initObservers() {

        viewModel.getAddressByIdUiState.onEach {
            when(it){
                is GetAddressByIdUiState.Error -> errorHandler.showToast(it.message)
                GetAddressByIdUiState.Loading -> {}
                is GetAddressByIdUiState.Success -> { displayAddress(it.data) }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.updateAddressUiState.onEach {
            when(it){
                is UpdateAddressUiState.Error -> errorHandler.showToast(it.message)
                UpdateAddressUiState.Loading -> {}
                is UpdateAddressUiState.Success -> {
                    findNavController().navigateUp()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.deleteAddressUiState.onEach {
            when(it){
                is DeleteAddressUiState.Error -> {
                    errorHandler.showToast(it.message)
                }
                DeleteAddressUiState.Loading -> {}
                is DeleteAddressUiState.Success -> {
                    findNavController().navigateUp()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun displayAddress(address: Address) {
        binding.etPlaceName.setText(address.name)
        binding.itemAddress.tvTitle.text = address.name
        binding.itemAddress.tvSubtitle.text = address.address
        binding.itemAddress.ivIcon.setImageResource(com.aralhub.ui.R.drawable.ic_ic_round_pin_drop)
        setWebView(address.latitude.toDouble(), address.longitude.toDouble())
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebView(latitude: Double, longitude: Double) {
        binding.wvMap.apply {
            settings.javaScriptEnabled = true
            settings.apply {
                domStorageEnabled = true
                loadsImagesAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    view?.loadUrl(request?.url.toString())
                    return true
                }
            }
            loadUrl("https://unique-banoffee-29f6df.netlify.app/?z=15&loc=$latitude%2C$longitude&hl=ru&alt=0")
        }
    }

    private fun initArgs() {
        savedPlaceId = requireArguments().getInt(ARG_SAVED_PLACE_ID)
        if (savedPlaceId != DEFAULT_SAVED_PLACE_ID) {
            viewModel.getAddressById(savedPlaceId)
        } else {
            errorHandler.showToast("Invalid saved place id")
        }
    }

    private fun initListeners() {
        binding.flBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvDelete.setOnClickListener {
            deleteSavedAddressModalBottomSheet.show(childFragmentManager, DeleteSavedAddressModalBottomSheet.TAG)
            deleteSavedAddressModalBottomSheet.setOnDeleteClickListener {
                viewModel.deleteAddress(savedPlaceId)
                deleteSavedAddressModalBottomSheet.dismissAllowingStateLoss()
            }
        }
        binding.btnSave.setOnClickListener {
            viewModel.updateAddress(savedPlaceId, CreateAddressRequest(
                name = binding.etPlaceName.text.toString(),
                address = binding.itemAddress.tvSubtitle.text.toString(),
                latitude = 0.0,
                longitude = 0.0))
        }
    }

    companion object {
        private const val ARG_SAVED_PLACE_ID = "savedPlaceId"
        private const val DEFAULT_SAVED_PLACE_ID = 0
        fun args(savedPlaceId: Int): Bundle {
            return Bundle().apply {
                putInt(ARG_SAVED_PLACE_ID, savedPlaceId)
            }
        }
    }
}