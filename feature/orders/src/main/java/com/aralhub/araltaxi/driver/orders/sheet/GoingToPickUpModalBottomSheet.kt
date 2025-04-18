package com.aralhub.araltaxi.driver.orders.sheet

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import com.aralhub.araltaxi.driver.orders.R
import com.aralhub.araltaxi.driver.orders.databinding.ModalBottomSheetGoingToPickUpBinding
import com.aralhub.araltaxi.driver.orders.utils.sendPhoneNumberToDial
import com.aralhub.ui.model.OrderItem
import com.aralhub.ui.utils.GlideEx
import com.aralhub.ui.utils.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GoingToPickUpModalBottomSheet :
    BottomSheetDialogFragment(R.layout.modal_bottom_sheet_going_to_pick_up) {
    private val binding by viewBinding(ModalBottomSheetGoingToPickUpBinding::bind)

    private var order: OrderItem? = null

    private var onClientPickedUp: (order: OrderItem?) -> Unit = {}
    fun setOnClientPickedUp(onPickedUp: (order: OrderItem?) -> Unit) {
        this.onClientPickedUp = onPickedUp
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
        setupListeners()

    }

    private fun setupListeners() {
        binding.slideArrived.setOnSlideChangeListener {
            if (it == 1f) {
                onClientPickedUp(order)
            }
        }
        binding.btnCancel.setOnClickListener {
            rideCanceledListener(order)
        }

        binding.btnNavigator.setOnClickListener {
            val longitude = order!!.locations.getOrNull(0)?.coordinates?.longitude
            val latitude = order!!.locations.getOrNull(0)?.coordinates?.latitude
            openYandexNavigator(requireContext(), latitude!!, longitude!!)
        }

        binding.btnCall.setOnClickListener {
            sendPhoneNumberToDial()
        }
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
        tvDistanceToClient.text = order?.pickUpDistance
        tvFromLocation.text = order?.pickUpAddress
        tvToLocation.text = order?.destinationAddress
        order?.paymentType?.resId?.let { ivPaymentMethod.setImageResource(it) }
        GlideEx.displayAvatar(
            order?.avatar.toString(),
            binding.ivAvatar
        )
    }

    private fun openYandexNavigator(context: Context, latitude: Double, longitude: Double) {
        val yandexPackage = "ru.yandex.yandexnavi"
        val uriYandex = "yandexnavi://build_route_on_map?lat_to=${latitude}&lon_to=${longitude}"
        val intentYandex = Intent(Intent.ACTION_VIEW, Uri.parse(uriYandex))

        val packageManager = context.packageManager
        packageManager.getLaunchIntentForPackage(yandexPackage) != null

        try {
            context.startActivity(intentYandex)
        } catch (e: ActivityNotFoundException) {
            // Открываем Play Маркет для установки Яндекс.Навигатора
            val marketUri = Uri.parse("market://details?id=$yandexPackage")
            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        }
    }

    companion object {
        const val TAG = "GoingToPickUpModalBottomSheet"
    }
}