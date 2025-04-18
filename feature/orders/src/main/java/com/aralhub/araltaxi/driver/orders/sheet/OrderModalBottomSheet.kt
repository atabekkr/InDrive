package com.aralhub.araltaxi.driver.orders.sheet

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.aralhub.araltaxi.core.common.utils.rejectOfferState
import com.aralhub.araltaxi.driver.orders.R
import com.aralhub.araltaxi.driver.orders.databinding.ModalBottomSheetOrderBinding
import com.aralhub.araltaxi.driver.orders.orders.CreateOfferUiState
import com.aralhub.araltaxi.driver.orders.orders.OfferViewModel
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.ui.dialog.ErrorMessageDialog
import com.aralhub.ui.dialog.LoadingDialog
import com.aralhub.ui.model.OrderItem
import com.aralhub.ui.utils.MoneyFormatter
import com.aralhub.ui.utils.ViewEx.show
import com.aralhub.ui.utils.setOnSafeClickListener
import com.aralhub.ui.utils.showSnackBar
import com.aralhub.ui.utils.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class OrderModalBottomSheet : BottomSheetDialogFragment(R.layout.modal_bottom_sheet_order) {

    private val binding by viewBinding(ModalBottomSheetOrderBinding::bind)

    private val orderLoadingModalBottomSheet = OrderLoadingModalBottomSheet()
    private val orderRejectedByClientModalBottomSheet = OrderRejectedByClientModalBottomSheet()
    private val commentModalBottomSheet = CommentModalBottomSheet()

    private val offerViewModel by activityViewModels<OfferViewModel>()

    private var order: OrderItem? = null
    private var offerAmount = 0
    private var baseAmount = 0

    private var minimumPrice = 1000
    private var maximumPrice = 500000

    @Inject
    lateinit var errorHandler: ErrorHandler

    private var errorDialog: ErrorMessageDialog? = null
    private var loadingDialog: LoadingDialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        errorDialog = ErrorMessageDialog(context)
        loadingDialog = LoadingDialog(context)
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
    }

    private fun updateUI() {
        binding.btnAcceptOffer.visibility = View.GONE
        binding.btnSendOffer.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (offerAmount != 0) updateUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MoneyFormatter(binding.etPrice)
        setupUI()
        setupListeners()

    }

    private fun setupUI() = binding.apply {
        order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("OrderDetail", OrderItem::class.java)
        } else {
            arguments?.getParcelable("OrderDetail")
        }
        order?.let { order ->
            tvPrice.text = getString(com.aralhub.ui.R.string.standard_uzs_price, order?.roadPrice)
            etPrice.setText(getString(com.aralhub.ui.R.string.standard_uzs_price, order?.roadPrice))
            tvClientName.text = order?.name
            tvRecommendPrice.text = getString(
                com.aralhub.ui.R.string.recommend_price,
                getString(com.aralhub.ui.R.string.standard_uzs_price, order?.recommendedPrice)
            )
            tvDistance.text = order?.roadDistance
            tvDistanceToClient.text = order?.pickUpDistance
            tvFromLocation.text = order?.pickUpAddress
            tvToLocation.text = order?.destinationAddress
            baseAmount = order?.roadPrice?.toInt() ?: 0
            order?.paymentType?.resId?.let { ivPaymentMethod.setImageResource(it) }

            if (order.comment.isNotEmpty()) binding.layoutComment.show()
            if (order.hasLuggage) layoutLuggage.show()

            Glide.with(binding.ivAvatar.context)
                .load(order?.avatar)
                .centerCrop()
                .placeholder(com.aralhub.ui.R.drawable.ic_user)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivAvatar)
        } ?: run {
            showSnackBar("Error occurred")
        }
    }

    private fun setupListeners() {
        binding.btnSendOffer.setOnClickListener {
            sendOffer()
        }
        binding.btnAcceptOffer.setOnClickListener {
            sendOffer()
        }
        binding.tvDecrease500.setOnClickListener {
            updateUI()
            var price = binding.etPrice.text.toString().filter { it.isDigit() }.toIntOrNull()
            if (price == null) price = 0
            if (price - 500 >= minimumPrice) {
                val editable = Editable.Factory.getInstance().newEditable("${price - 500}")
                binding.etPrice.text = editable
            } else {
                errorHandler.showToast("Minimum price: $minimumPrice")
            }
        }
        binding.tvIncrease500.setOnClickListener {
            updateUI()
            var price = binding.etPrice.text.toString().filter { it.isDigit() }.toIntOrNull()
            if (price == null) price = 0
            if (price!! + 500 <= maximumPrice) {
                val editable = Editable.Factory.getInstance().newEditable("${price!! + 500}")
                binding.etPrice.text = editable
            } else {
                errorHandler.showToast("Maximum price: $maximumPrice")
            }
        }
        binding.llPickUpAddress.setOnClickListener { onAddressClick(order) }
        binding.llDestinationAddress.setOnClickListener { onAddressClick(order) }

        binding.layoutComment.setOnSafeClickListener {
            val bundle = Bundle()
            bundle.putString("Comment", order?.comment)
            commentModalBottomSheet.arguments = bundle
            commentModalBottomSheet.show(
                childFragmentManager,
                commentModalBottomSheet.tag
            )
        }
    }

    private fun sendOffer() {
        val priceValue = binding.etPrice.text.toString().filter { it.isDigit() }
        offerAmount = if (priceValue.isNotBlank()) priceValue.toInt() else 0
        if (order != null && offerAmount != 0) {
            offerViewModel.createOffer(
                order!!.uuid,
                offerAmount
            )
            initObservers()
        } else {
            showErrorDialog("Нельзя отправить оффер")
        }
    }

    private fun initObservers() {
        offerViewModel.createOfferUiState.onEach { result ->
            Timber.i("initObservers: $result")
            when (result) {
                is CreateOfferUiState.Error -> {
                    showErrorDialog(result.message)
                    binding.btnSendOffer.isEnabled = true
                }

                CreateOfferUiState.Loading -> {
                    showLoading()
                    binding.btnSendOffer.isEnabled = false
                }

                is CreateOfferUiState.Success -> {
                    dismissLoading()
                    binding.btnSendOffer.isEnabled = true
                    val bundle = Bundle()
                    val customOfferAmount = binding.etPrice.text.toString()
                    val offerAmount = customOfferAmount.ifEmpty {
                        getString(
                            com.aralhub.ui.R.string.standard_uzs_price, "$baseAmount"
                        )
                    }
                    bundle.putString("OfferAmount", offerAmount)
                    if (!orderLoadingModalBottomSheet.isAdded) {
                        orderLoadingModalBottomSheet.arguments = bundle
                        orderLoadingModalBottomSheet.show(
                            parentFragmentManager,
                            OrderLoadingModalBottomSheet.TAG
                        )
                    }
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        offerViewModel.dismissOfferWaitingTimeBottomSheet.onEach {
            if (orderLoadingModalBottomSheet.isAdded) {
                orderLoadingModalBottomSheet.dismissAllowingStateLoss()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        rejectOfferState.onEach {
            lifecycleScope.launchWhenResumed {
                if (!orderRejectedByClientModalBottomSheet.isAdded) {
                    orderRejectedByClientModalBottomSheet.arguments = arguments
                    orderRejectedByClientModalBottomSheet.show(
                        childFragmentManager,
                        orderRejectedByClientModalBottomSheet.tag
                    )
                }
                orderLoadingModalBottomSheet.dismissAllowingStateLoss()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissErrorDialog()
        dismissLoading()
        if (orderLoadingModalBottomSheet.isAdded)
            orderLoadingModalBottomSheet.dismissAllowingStateLoss()
    }

    private var onAddressClick: (order: OrderItem?) -> Unit = {}
    fun setOnAddressClickListener(onAddressClick: (order: OrderItem?) -> Unit) {
        this.onAddressClick = onAddressClick
    }

    private fun showErrorDialog(errorMessage: String?) {
        dismissLoading()
        errorDialog?.setOnDismissClicked { errorDialog?.dismiss() }
        errorDialog?.show(errorMessage)

        errorDialog?.setOnDismissClicked { errorDialog?.dismiss() }
    }

    private fun showLoading() {
        loadingDialog?.show()
    }

    private fun dismissLoading() {
        loadingDialog?.dismiss()
    }

    private fun dismissErrorDialog() {
        errorDialog?.dismiss()
    }

    companion object {
        const val TAG = "OrderModalBottomSheet"
    }
}