package com.aralhub.indrive.driver.orders.sheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import com.aralhub.indrive.driver.orders.R
import com.aralhub.indrive.driver.orders.databinding.ModalBottomSheetOrderBinding
import com.aralhub.ui.utils.MoneyFormatter
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OrderModalBottomSheet : BottomSheetDialogFragment(R.layout.modal_bottom_sheet_order) {
    private val binding by viewBinding(ModalBottomSheetOrderBinding::bind)
    private val orderLoadingModalBottomSheet = OrderLoadingModalBottomSheet()

    private var onOrderAccepted: (() -> Unit)? = null
    fun setOnOrderAccepted(onOrderAccepted: () -> Unit) {
        this.onOrderAccepted = onOrderAccepted
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MoneyFormatter(binding.etPrice)
        setWebView()
        binding.btnSendOffer.setOnClickListener {
            orderLoadingModalBottomSheet.show(parentFragmentManager, OrderLoadingModalBottomSheet.TAG)
            lifecycleScope.launch {
                delay(3000)
                orderLoadingModalBottomSheet.dismissAllowingStateLoss()
                onOrderAccepted?.invoke()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebView() {
        binding.wvMap.apply {
            setBackgroundColor(0)
            // Enable JavaScript
            settings.javaScriptEnabled = true

            // Configure WebView settings
            settings.apply {
                domStorageEnabled = true
                loadsImagesAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                useWideViewPort = true
                loadWithOverviewMode = true
            }

            // Set WebViewClient with override for error handling
            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Log.e(TAG, "WebView error: ${error?.description}")
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    view?.loadUrl(request?.url.toString())
                    return true
                }
            }
            // Load the URL
            val startLat = 42.473810
            val startLong = 59.615314

            val endLat = 42.465140
            val endLong = 59.612868
            loadUrl("https://unique-banoffee-29f6df.netlify.app/?z=15&loc=$startLat%2C$startLong&loc=$endLat%2C$endLong&hl=ru&alt=0")
        }
    }

    companion object {
        const val TAG = "OrderModalBottomSheet"
    }
}