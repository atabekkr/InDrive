package com.aralhub.araltaxi.driver.orders.sheet

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aralhub.araltaxi.driver.orders.R
import com.aralhub.araltaxi.driver.orders.databinding.ModalBottomSheetWaitingForClientBinding
import com.aralhub.araltaxi.driver.orders.sheet.sheetviewmodel.GetFreeWaitingTimeViewModel
import com.aralhub.araltaxi.driver.orders.utils.RideTimerDriver
import com.aralhub.araltaxi.driver.orders.utils.showSnackBar
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.ui.model.OrderItem
import com.aralhub.ui.utils.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class WaitingForClientModalBottomSheet :
    BottomSheetDialogFragment(R.layout.modal_bottom_sheet_waiting_for_client) {

    private val binding by viewBinding(ModalBottomSheetWaitingForClientBinding::bind)

    private val viewModel by viewModels<GetFreeWaitingTimeViewModel>()

    private var order: OrderItem? = null

    private val rideTimerDriver = RideTimerDriver()

    private var onGoingToRideListener: (order: OrderItem?) -> Unit = {}
    fun setOnGoingToRideListener(onGoingToRide: (order: OrderItem?) -> Unit) {
        this.onGoingToRideListener = onGoingToRide
    }

    private var rideCanceledListener: (order: OrderItem?) -> Unit = {}
    fun setOnRideCanceledListener(onRideCanceled: (order: OrderItem?) -> Unit) {
        this.rideCanceledListener = onRideCanceled
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        fetchData()
        setupListeners()
        setupObservers()

    }

    private fun fetchData() {

        val rideId = order?.id
        if (rideId != null)
            viewModel.getWaitTime(rideId)
        else
            showSnackBar("Невозможно загрузить данные")

    }

    private fun setupListeners() {
        binding.btnGoToRide.setOnClickListener {
            onGoingToRideListener(order)
        }
        binding.btnCancel.setOnClickListener {
            rideCanceledListener.invoke(order)
        }
    }

    private fun setupObservers() {
        viewModel.getWaitingTime.onEach { result ->
            when (result) {
                is Result.Error -> {
                    showSnackBar(result.message)
                }

                is Result.Success -> {
                    val freeWaitingTime = result.data.toLong()
                    startTimer(freeWaitingTime)
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupUI() = binding.apply {
        order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("OrderDetail", OrderItem::class.java)
        } else {
            arguments?.getParcelable("OrderDetail")
        }
        tvPrice.text = getString(com.aralhub.ui.R.string.standard_uzs_price, order?.roadPrice)
        tvClientName.text = order?.name
        tvDistance.text = order?.roadDistance
        tvFromLocation.text = order?.pickUpAddress
        tvToLocation.text = order?.destinationAddress
        order?.paymentType?.resId?.let { ivPaymentMethod.setImageResource(it) }
        Glide.with(binding.ivAvatar.context)
            .load(order?.avatar)
            .centerCrop()
            .placeholder(com.aralhub.ui.R.drawable.ic_user)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivAvatar)
    }

    private fun startTimer(freeWaitingTime: Long) {
        rideTimerDriver.startTimer(
            freeWaitingTime,
            binding.tvTime
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rideTimerDriver.stopTimer()
    }

    companion object {
        const val TAG = "WaitingForClientModalBottomSheet"
    }
}