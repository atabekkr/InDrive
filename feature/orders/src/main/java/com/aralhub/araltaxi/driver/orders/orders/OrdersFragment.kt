package com.aralhub.araltaxi.driver.orders.orders

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.aralhub.araltaxi.core.common.sharedpreference.DriverSharedPreference
import com.aralhub.araltaxi.driver.orders.R
import com.aralhub.araltaxi.driver.orders.databinding.FragmentOrdersBinding
import com.aralhub.araltaxi.driver.orders.model.RideStatus
import com.aralhub.araltaxi.driver.orders.model.SendDriverLocationUI
import com.aralhub.araltaxi.driver.orders.navigation.FeatureOrdersNavigation
import com.aralhub.araltaxi.driver.orders.sheet.CancelTripModalBottomSheet
import com.aralhub.araltaxi.driver.orders.sheet.ExitLineModalBottomSheet
import com.aralhub.araltaxi.driver.orders.sheet.FilterModalBottomSheet
import com.aralhub.araltaxi.driver.orders.sheet.GoingToPickUpModalBottomSheet
import com.aralhub.araltaxi.driver.orders.sheet.LogoutModalBottomSheet
import com.aralhub.araltaxi.driver.orders.sheet.OrderModalBottomSheet
import com.aralhub.araltaxi.driver.orders.sheet.ReasonCancelModalBottomSheet
import com.aralhub.araltaxi.driver.orders.sheet.RideCancelledByPassengerModalBottomSheet
import com.aralhub.araltaxi.driver.orders.sheet.RideFinishedModalBottomSheet
import com.aralhub.araltaxi.driver.orders.sheet.RideModalBottomSheet
import com.aralhub.araltaxi.driver.orders.sheet.TripCanceledModalBottomSheet
import com.aralhub.araltaxi.driver.orders.sheet.WaitingForClientModalBottomSheet
import com.aralhub.araltaxi.services.LocationService
import com.aralhub.ui.adapter.OrderItemAdapter
import com.aralhub.ui.dialog.ErrorMessageDialog
import com.aralhub.ui.dialog.LoadingDialog
import com.aralhub.ui.model.OrderItem
import com.aralhub.ui.model.args.ShowRideRouteArg
import com.aralhub.ui.utils.GlideEx
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.setOnSafeClickListener
import com.aralhub.ui.utils.viewBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class OrdersFragment : Fragment(R.layout.fragment_orders) {

    private val binding by viewBinding(FragmentOrdersBinding::bind)
    private val adapter = OrderItemAdapter()

    private val viewModel by viewModels<OrdersViewModel>()

    private val orderModalBottomSheet = OrderModalBottomSheet()
    private val goingToPickUpModalBottomSheet = GoingToPickUpModalBottomSheet()
    private val waitingForClientModalBottomSheet = WaitingForClientModalBottomSheet()
    private val rideModalBottomSheet = RideModalBottomSheet()
    private val rideFinishedModalBottomSheet = RideFinishedModalBottomSheet()
    private val cancelTripModalBottomSheet = CancelTripModalBottomSheet()
    private val reasonCancelModalBottomSheet = ReasonCancelModalBottomSheet()
    private val rideCanceledByPassengerModalBottomSheet = RideCancelledByPassengerModalBottomSheet()
    private val tripCanceledModalBottomSheet = TripCanceledModalBottomSheet()
    private val filterModalBottomSheet = FilterModalBottomSheet()
    private val exitLineModalBottomSheet =
        ExitLineModalBottomSheet { findNavController().navigateUp() }

    private var errorDialog: ErrorMessageDialog? = null
    private var loadingDialog: LoadingDialog? = null

    private var fusedLocationClient: FusedLocationProviderClient? = null

    @Inject
    lateinit var navigation: FeatureOrdersNavigation

    @Inject
    lateinit var driverSharedPreference: DriverSharedPreference

    override fun onAttach(context: Context) {
        super.onAttach(context)
        errorDialog = ErrorMessageDialog(context)
        loadingDialog = LoadingDialog(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startService()
        initData()
        fetchData()
        initViews()
        initObservers()
        initGoingToPickUpModalBottomSheet()
        initWaitingForClientModalBottomSheet()
        initRideModalBottomSheet()
        initTripCanceledModalBottomSheet()
        initListeners()

    }

    private fun initData() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun getExistingOrders() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient?.lastLocation
                ?.addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    val distance = driverSharedPreference.distance
                    location?.let {
                        viewModel.getExistingOrders(
                            SendDriverLocationUI(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                distance = distance
                            )
                        )
                    }
                }
        }
    }

    private fun fetchData() {
        getExistingOrders()
        arguments?.let {
            showActiveRideSheet()
        }
    }

    private fun showActiveRideSheet() {
        viewModel.disconnect()
        val status = arguments?.getString("Status") ?: ""
        when (status) {
            "driver_on_the_way" -> {
                showGoingToPickUpSheet(arguments)
            }

            "agreed_with_driver" -> {
                showGoingToPickUpSheet(arguments)
            }

            "driver_waiting_client" -> {
                if (!waitingForClientModalBottomSheet.isAdded) {
                    waitingForClientModalBottomSheet.arguments = arguments
                    waitingForClientModalBottomSheet.show(
                        childFragmentManager,
                        WaitingForClientModalBottomSheet.TAG
                    )
                }
            }

            "ride_started" -> {
                if (!rideModalBottomSheet.isAdded) {
                    rideModalBottomSheet.arguments = arguments
                    rideModalBottomSheet.show(
                        childFragmentManager,
                        RideModalBottomSheet.TAG
                    )
                }
            }
        }
    }

    private fun showGoingToPickUpSheet(arguments: Bundle?) {
        if (!goingToPickUpModalBottomSheet.isAdded) {
            goingToPickUpModalBottomSheet.arguments = arguments
            goingToPickUpModalBottomSheet.show(
                childFragmentManager,
                GoingToPickUpModalBottomSheet.TAG
            )
        }
    }

    private fun startService() {
        val intent = Intent(requireContext(), LocationService::class.java)
        requireActivity().startForegroundService(intent)
    }

    private fun stopService() {
        val intent = Intent(requireContext(), LocationService::class.java)
        requireActivity().stopService(intent)
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ordersListState.collect { orders ->

                val sortedOrders = orders.sortedByDescending { it.pickUpDistance }
                adapter.submitList(sortedOrders)

                binding.tvOrdersNotFound.visibility =
                    if (orders.isEmpty()) View.VISIBLE else View.GONE
                binding.rvOrders.visibility =
                    if (orders.isEmpty()) View.GONE else View.VISIBLE

                dismissLoading()
            }
        }

        observeState(viewModel.logoutUiState) { logoutUiState ->
            when (logoutUiState) {
                is LogoutUiState.Error -> showErrorDialog(logoutUiState.message)
                LogoutUiState.Loading -> showLoading()
                LogoutUiState.Success -> navigation.goToLogoFromOrders()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ordersState
                    .collectLatest { result ->
                        Timber.d("initObservers: $result")

                        when (result) {

                            GetActiveOrdersUiState.Idle -> {}

                            is GetActiveOrdersUiState.Error -> showErrorDialog(
                                result.message
                            )

                            is GetActiveOrdersUiState.Loading -> showLoading()

                            is GetActiveOrdersUiState.OfferAccepted -> {
                                val bundle = Bundle()
                                bundle.putParcelable(
                                    "OrderDetail",
                                    result.data
                                )

                                showGoingToPickUpSheet(bundle)

                                viewModel.updateRideStatus(
                                    result.data.id,
                                    RideStatus.DRIVER_ON_THE_WAY.status
                                )
                                orderModalBottomSheet.dismissAllowingStateLoss() //todo crash in this field
                                viewModel.disconnect()
                            }

                            is GetActiveOrdersUiState.GetExistOrder -> {
                                dismissLoading()
                            }

                            is GetActiveOrdersUiState.RideCanceledByPassenger -> {
                                if (!rideCanceledByPassengerModalBottomSheet.isAdded)
                                    rideCanceledByPassengerModalBottomSheet.show(
                                        childFragmentManager,
                                        rideCanceledByPassengerModalBottomSheet.tag
                                    )
                            }

                            is GetActiveOrdersUiState.ConnectionFailed -> {
                                showWebSocketErrorDialog("Connection failed")
                            }
                        }
                    }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateRideStatusResult.collectLatest { result ->
                    when (result) {
                        is RideUpdateUiState.Error -> showErrorDialog(result.message)
                        is RideUpdateUiState.Success -> {
                            if (result.data != null) {
                                rideModalBottomSheet.dismissAllowingStateLoss()

                                val bundle = Bundle()
                                bundle.putParcelable("RideCompletedDetail", result.data)

                                if (!rideFinishedModalBottomSheet.isAdded) {
                                    rideFinishedModalBottomSheet.arguments = bundle
                                    rideFinishedModalBottomSheet.show(
                                        childFragmentManager,
                                        RideFinishedModalBottomSheet.TAG
                                    )
                                }
                                startService()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun displayProfile() {
        dismissLoading()
        binding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.tv_name).text =
            driverSharedPreference.userName
        binding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.tv_phone).text =
            driverSharedPreference.phoneNumber
        GlideEx.displayAvatar(
            driverSharedPreference.avatar,
            binding.navigationView.getHeaderView(0).findViewById(R.id.iv_avatar)
        )

        binding.textAppVersion.text = driverSharedPreference.appVersion
    }

    private fun initListeners() {

        binding.swiperefresh.setOnRefreshListener {
            getExistingOrders()
            startService()
            binding.swiperefresh.isRefreshing = false
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            true
        ) { showExitLineBottomSheet() }
        binding.navigationView.getHeaderView(0).setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            navigation.goToProfileFromOrders()
        }

        filterModalBottomSheet.setOnConfirmClickListener { getExistingOrders() }

        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_support -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigation.goToSupportFromOrders()
                    true
                }

                R.id.action_log_out -> {
                    val logoutModalBottomSheet = LogoutModalBottomSheet()
                    logoutModalBottomSheet.show(childFragmentManager, LogoutModalBottomSheet.TAG)
                    logoutModalBottomSheet.setOnLogoutListener {
                        logoutModalBottomSheet.dismissAllowingStateLoss()
                        viewModel.logout()
                    }
                    true
                }

                R.id.action_my_revenue -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigation.goToRevenueFromOrders()
                    true
                }

                R.id.action_order_history -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigation.goToHistoryFromOrders()
                    true
                }

                R.id.action_change_language -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigation.goToChangeLanguageFromOrders()
                    true
                }

                else -> false
            }
        }
        orderModalBottomSheet.setOnAddressClickListener { order: OrderItem? ->
            val item = ShowRideRouteArg(
                locations = order?.locations ?: emptyList()
            )
            navigation.goToMapFromOrders(
                item
            )
        }

        rideCanceledByPassengerModalBottomSheet.setOnUnderstandClickListener {
            dismissAllBottomSheets()
            getExistingOrders()
            viewModel.switchBackToOrdersSocket()
            viewModel.setIdleState()
            startService()
        }

        binding.btnFilter.setOnSafeClickListener {
            filterModalBottomSheet.show(
                childFragmentManager,
                FilterModalBottomSheet.TAG
            )
        }
    }

    private fun initTripCanceledModalBottomSheet() {

        cancelTripModalBottomSheet.setOnRideCancelClickListener { order: OrderItem? ->
            if (order != null) {
                val bundle = Bundle()
                bundle.putInt("rideId", order.id)
                reasonCancelModalBottomSheet.arguments = bundle
                reasonCancelModalBottomSheet.show(
                    childFragmentManager,
                    ReasonCancelModalBottomSheet.TAG
                )
            } else {
                Timber.d("initTripCanceledModalBottomSheet: order is null")
            }
        }

        tripCanceledModalBottomSheet.setOnCloseListener {
            tripCanceledModalBottomSheet.dismissAllowingStateLoss()
            dismissAllBottomSheets()
            getExistingOrders()
            startService()
        }

        reasonCancelModalBottomSheet.setOnRideCancelledListener {
            tripCanceledModalBottomSheet.show(
                childFragmentManager,
                TripCanceledModalBottomSheet.TAG
            )
            reasonCancelModalBottomSheet.dismissAllowingStateLoss()
        }
    }

    private fun initRideModalBottomSheet() {
        rideModalBottomSheet.setOnRideFinishedListener { order: OrderItem? ->
            viewModel.updateRideStatus(
                order!!.id,
                RideStatus.RIDE_COMPLETED.status
            )
            getExistingOrders()
            viewModel.setIdleState()
        }
        rideModalBottomSheet.setOnRideCanceledListener { order -> showCancelTripBottomSheet(order) }
    }

    private fun showCancelTripBottomSheet(order: OrderItem?) {
        val bundle = Bundle()
        bundle.putParcelable("OrderDetail", order)
        cancelTripModalBottomSheet.arguments = bundle
        cancelTripModalBottomSheet.show(childFragmentManager, CancelTripModalBottomSheet.TAG)
    }

    private fun initWaitingForClientModalBottomSheet() {
        waitingForClientModalBottomSheet.setOnGoingToRideListener { order: OrderItem? ->
            waitingForClientModalBottomSheet.dismissAllowingStateLoss()
            val bundle = Bundle()
            bundle.putParcelable(
                "OrderDetail",
                order
            )
            rideModalBottomSheet.arguments = bundle
            rideModalBottomSheet.show(childFragmentManager, RideModalBottomSheet.TAG)
            viewModel.updateRideStatus(
                order?.id!!,
                RideStatus.RIDE_STARTED.status
            )
        }
        waitingForClientModalBottomSheet.setOnRideCanceledListener { order ->
            showCancelTripBottomSheet(
                order
            )
        }
    }

    private fun initGoingToPickUpModalBottomSheet() {
        goingToPickUpModalBottomSheet.setOnClientPickedUp { order: OrderItem? ->
            goingToPickUpModalBottomSheet.dismissAllowingStateLoss()
            val bundle = Bundle()
            bundle.putParcelable(
                "OrderDetail",
                order
            )
            if (!waitingForClientModalBottomSheet.isAdded) {
                waitingForClientModalBottomSheet.arguments = bundle
                waitingForClientModalBottomSheet.show(
                    childFragmentManager,
                    WaitingForClientModalBottomSheet.TAG
                )
            }
            viewModel.updateRideStatus(
                order!!.id,
                RideStatus.DRIVER_WAITING_CLIENT.status
            )
            viewModel.setIdleState()
        }
        goingToPickUpModalBottomSheet.setOnRideCanceledListener { order ->
            showCancelTripBottomSheet(
                order
            )
        }
    }

    private fun initViews() {
        displayProfile()
        initToolbar()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.rvOrders.adapter = adapter
        adapter.setOnItemClickListener { order ->
            val bundle = Bundle()
            bundle.putParcelable(
                "OrderDetail",
                order
            )
            orderModalBottomSheet.arguments = bundle
            orderModalBottomSheet.show(
                childFragmentManager,
                OrderModalBottomSheet.TAG
            )
        }
    }

    private fun initToolbar() {
        binding.tbOrders.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.tbOrders.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_exit_line -> {
                    showExitLineBottomSheet()
                    true
                }

                else -> false
            }
        }
    }

    private fun showExitLineBottomSheet() {
        if (!exitLineModalBottomSheet.isAdded)
            exitLineModalBottomSheet.show(childFragmentManager, ExitLineModalBottomSheet.TAG)
    }

    private fun dismissAllBottomSheets() {
        listOf(
            orderModalBottomSheet,
            goingToPickUpModalBottomSheet,
            waitingForClientModalBottomSheet,
            rideModalBottomSheet,
            rideFinishedModalBottomSheet,
            cancelTripModalBottomSheet,
            tripCanceledModalBottomSheet,
            filterModalBottomSheet,
            exitLineModalBottomSheet
        ).forEach { sheet ->
            if (sheet.isAdded) sheet.dismissAllowingStateLoss()
        }
    }

    private fun showErrorDialog(errorMessage: String?) {
        dismissLoading()
        errorDialog?.setOnDismissClicked { errorDialog?.dismiss() }
        errorDialog?.show(errorMessage)

    }

    private fun showWebSocketErrorDialog(errorMessage: String?) {
        dismissLoading()
        errorDialog?.setOnDismissClicked {
            startService()
            errorDialog?.dismiss()
        }
        errorDialog?.show(errorMessage = errorMessage, isWebSocketError = true)

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

    override fun onDestroyView() {
        super.onDestroyView()
        dismissErrorDialog()
        dismissLoading()
    }

    override fun onDetach() {
        super.onDetach()
        stopService()
    }

}