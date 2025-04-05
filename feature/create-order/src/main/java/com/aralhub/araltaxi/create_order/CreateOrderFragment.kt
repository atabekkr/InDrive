package com.aralhub.araltaxi.create_order

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aralhub.araltaxi.core.common.error.ErrorHandler
import com.aralhub.araltaxi.core.common.permission.PermissionHelper
import com.aralhub.araltaxi.core.common.utils.MapStyles
import com.aralhub.araltaxi.core.common.utils.loadJsonFromAssets
import com.aralhub.araltaxi.create_order.databinding.FragmentCreateOrderBinding
import com.aralhub.araltaxi.create_order.navigation.FeatureCreateOrderNavigation
import com.aralhub.araltaxi.create_order.utils.NewCurrentLocationListener
import com.aralhub.araltaxi.create_order.utils.updateMapStyle
import com.aralhub.indrive.core.data.model.client.GeoPoint
import com.aralhub.indrive.core.data.model.client.RecommendedPrice
import com.aralhub.indrive.core.data.model.payment.PaymentMethodType
import com.aralhub.indrive.core.data.model.ride.RecommendedAmount
import com.aralhub.ui.adapter.option.RideOptionItemAdapter
import com.aralhub.ui.model.args.SelectedLocations
import com.aralhub.ui.sheets.ChangePaymentMethodModalBottomSheet
import com.aralhub.ui.sheets.CommentToDriverModalBottomSheet
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.ViewEx.disable
import com.aralhub.ui.utils.ViewEx.enable
import com.aralhub.ui.utils.viewBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.DrivingSession.DrivingRouteListener
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateOrderFragment : Fragment(R.layout.fragment_create_order) {
    private val binding by viewBinding(FragmentCreateOrderBinding::bind)
    private var isConfiguring: Boolean = false

    @Inject
    lateinit var errorHandler: ErrorHandler
    private val changePaymentMethodModalBottomSheet by lazy { ChangePaymentMethodModalBottomSheet() }
    private val commentToDriverModalBottomSheet by lazy { CommentToDriverModalBottomSheet() }
    private var minimumPrice = 0
    private var maximumPrice = 0
    private var comment = ""
    private var recommendedPrice: RecommendedPrice? = null
    private val rideOptionItemAdapter by lazy { RideOptionItemAdapter() }
    private var enabledOptionsIds: MutableList<Int> = mutableListOf()
    private val viewModel by viewModels<CreateOrderViewModel>()
    private var selectedLocations: SelectedLocations? = null
    private var locationManager: LocationManager? = null
    private lateinit var mapWindow: MapWindow
    private var map: Map? = null
    private var mapObjects: MapObjectCollection? = null
    private lateinit var drivingSession: DrivingSession

    private val drivingRouteListener = object : DrivingRouteListener {
        override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
            drawDrivingRoutes(drivingRoutes)
        }

        override fun onDrivingRoutesError(error: Error) {
            when (error) {
                is NetworkError -> errorHandler.showToast(getString(com.aralhub.ui.R.string.error_network_connection))
                else -> errorHandler.showToast("Error in drawing route")
            }
        }
    }

    private fun showFullRoute(route: DrivingRoute) {
        val routePoints = route.geometry.points
        val bounds = BoundingBox(
            Point(
                routePoints.minOf { it.latitude },
                routePoints.minOf { it.longitude }
            ),
            Point(
                routePoints.maxOf { it.latitude },
                routePoints.maxOf { it.longitude }
            )
        )

        map?.let {
            val routeCamePosition = it.cameraPosition(Geometry.fromBoundingBox(bounds))
            val updatedCameraPosition = CameraPosition(
                routeCamePosition.target,
                routeCamePosition.zoom - 1.0f,
                routeCamePosition.azimuth,
                routeCamePosition.tilt
            )
            it.move(updatedCameraPosition, Animation(Animation.Type.SMOOTH, 1f), null)
        }

    }

    @Inject
    lateinit var navigation: FeatureCreateOrderNavigation
    private val newCurrentLocationListener = NewCurrentLocationListener(
        onInitMapPosition = { point ->
            /* createCurrentLocationPlaceMarkObject(point)*/
        },
        onUpdateMapPosition = { point -> /*createCurrentLocationPlaceMarkObject(point) */ },
        onProviderDisabledListener = { point ->
            /*   createCurrentLocationPlaceMarkObject(point)*/
        },
        onProviderEnabledListener = { }
    )
    private var currentLocationPlaceMarkObject: PlacemarkMapObject? = null
    private var fromRoutePlaceMarkObject: PlacemarkMapObject? = null
    private var toRoutePlaceMarkObject: PlacemarkMapObject? = null
    private var imageProvider: ImageProvider? = null

    private val handler = Handler(Looper.getMainLooper())
    private var isUpdating = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        initMap()
        initObservers()
        initViews()
        initListeners()
        initArgs()
        viewModel.setPaymentMethodType(PaymentMethodType.CASH)
        viewModel.getActivePaymentMethods()
        viewModel.getRideOptions()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun initMap() {
        mapWindow = binding.mapView.mapWindow
        map = mapWindow.map
        mapObjects = mapWindow.map.mapObjects.addCollection()
        imageProvider =
            ImageProvider.fromResource(requireContext(), com.aralhub.ui.R.drawable.ic_vector)

        setDefaultMapStyle()
        mapCameraListener()

    }

    private fun setDefaultMapStyle() {
        val minimalisticMapStyle =
            loadJsonFromAssets(requireContext(), MapStyles.MINIMALISTIC_MAP_STYLE)
        binding.mapView.map.setMapStyle(minimalisticMapStyle)
    }

    private fun mapCameraListener() {
        binding.mapView.map.addCameraListener(object : CameraListener {
            override fun onCameraPositionChanged(
                p0: Map,
                cameraPosition: CameraPosition,
                p2: CameraUpdateReason,
                p3: Boolean
            ) {
                val zoom = cameraPosition.zoom
                Log.i("ZoomInCreateOrder", zoom.toString())
                if (isUpdating) return
                isUpdating = true

                handler.postDelayed({
                    updateMapStyle(zoom)
                    isUpdating = false
                }, 500)
            }
        })
    }

    private fun updateMapStyle(zoom: Float) {
        map?.updateMapStyle(zoom, requireContext())
    }

    override fun onResume() {
        super.onResume()
        locationManager?.let { observeLocationUpdates(it) }
    }

    override fun onPause() {
        super.onPause()
        locationManager?.removeUpdates(newCurrentLocationListener)
    }

    @SuppressLint("MissingPermission")
    private fun observeLocationUpdates(locationManager: LocationManager) {
        if (PermissionHelper.arePermissionsGranted(
                requireContext(),
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                newCurrentLocationListener
            )
        }
    }

    private fun initArgs() {
        selectedLocations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(
                "selectedLocations",
                SelectedLocations::class.java
            )
        } else {
            requireArguments().getParcelable("selectedLocations")
        }
        selectedLocations?.let {
            viewModel.getRecommendedPrice(
                listOf(
                    GeoPoint(
                        latitude = it.from.latitude,
                        longitude = it.from.longitude,
                        name = it.from.name
                    ),
                    GeoPoint(
                        latitude = it.to.latitude,
                        longitude = it.to.longitude,
                        name = it.to.name
                    )
                )
            )

            Log.i(
                "Locations", "${
                    listOf(
                        GeoPoint(
                            latitude = it.from.latitude,
                            longitude = it.from.longitude,
                            name = it.from.name
                        ),
                        GeoPoint(
                            latitude = it.to.latitude,
                            longitude = it.to.longitude,
                            name = it.to.name
                        )
                    )
                }"
            )
            requestRoutes(it)
            binding.tvFromLocationName.text = it.from.name
            binding.tvToLocationName.text = it.to.name
        }


    }

    private fun requestRoutes(routeLocations: SelectedLocations) {
        val drivingRouter =
            DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
        val points = buildList {
            add(
                RequestPoint(
                    Point(routeLocations.from.latitude, routeLocations.from.longitude),
                    RequestPointType.WAYPOINT,
                    null,
                    null,
                    null
                )
            )
            add(
                RequestPoint(
                    Point(routeLocations.to.latitude, routeLocations.to.longitude),
                    RequestPointType.WAYPOINT,
                    null,
                    null,
                    null
                )
            )
        }
        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()
        drivingSession = drivingRouter.requestRoutes(
            points,
            drivingOptions,
            vehicleOptions,
            drivingRouteListener
        )
    }

    private fun initViews() {
        binding.rvRideOptions.adapter = rideOptionItemAdapter
        binding.btnSendOffer.disable()
    }

    private fun initListeners() {

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            true
        ) {
            val result = Bundle().apply {
                putBoolean("cancel", true)
            }
            parentFragmentManager.setFragmentResult("cancel", result)
            findNavController().navigateUp()
        }

        binding.tvFromLocationName.setOnClickListener {
            //  navigation.goToSelectFromLocationFromCreateOrderFragment()
        }

        binding.tvToLocationName.setOnClickListener {
            // navigation.goToSelectToLocationFromCreateOrderFragment()
        }


        initChangePaymentMethodListener()
        initCommentToDriverListener()

        binding.ivConfigure.setOnClickListener {
            isConfiguring = !isConfiguring
            binding.layoutConfigure.isVisible = isConfiguring
        }

        binding.btnSendOffer.setOnClickListener {

            Log.i("Payment", "${viewModel.paymentMethod.value.id}")
            Log.i("Payment", "${viewModel.paymentMethod.value}")
            val fakeRecommendedAmount = RecommendedAmount(
                7000,
                10000,
                70000
            )

            enabledOptionsIds.addAll(rideOptionItemAdapter.currentList.filter { it.isEnabled }
                .map { it.id })
            recommendedPrice?.let {
                viewModel.createRide(
                    baseAmount = binding.etPrice.text.toString().replace(" ", "").toInt(),
                    recommendedAmount = fakeRecommendedAmount,
                    selectedLocations = selectedLocations!!,
                    comment = comment,
                    paymentId = viewModel.paymentMethod.value.id,
                    options = enabledOptionsIds
                )
            }
        }

        binding.ivChangePaymentMethod.setOnClickListener {
            changePaymentMethodModalBottomSheet.show(
                requireActivity().supportFragmentManager,
                ChangePaymentMethodModalBottomSheet.TAG
            )
        }

        binding.layoutCommentToDriver.setOnClickListener {
            commentToDriverModalBottomSheet.show(requireActivity().supportFragmentManager, "")
        }
    }

    private fun initCommentToDriverListener() {
        commentToDriverModalBottomSheet.setOnSaveCommentToDriver {
            comment = it
            commentToDriverModalBottomSheet.dismissAllowingStateLoss()
        }
    }

    private fun initChangePaymentMethodListener() {
        changePaymentMethodModalBottomSheet.setOnCashClickListener {
            viewModel.setPaymentMethodType(PaymentMethodType.CASH)
            changePaymentMethodModalBottomSheet.dismissAllowingStateLoss()

        }

        changePaymentMethodModalBottomSheet.setOnOnlineClickListener {
            viewModel.setPaymentMethodType(PaymentMethodType.CARD)
            changePaymentMethodModalBottomSheet.dismissAllowingStateLoss()
        }

        binding.tvDecrease500.setOnClickListener {
            val price = Integer.parseInt(binding.etPrice.text.toString().replace(" ", ""))
            if (price - 500 >= minimumPrice) {
                val editable = Editable.Factory.getInstance().newEditable("${price - 500}")
                binding.etPrice.text = editable
            } else {
                errorHandler.showToast("Minimum price: $minimumPrice")
            }
        }
        binding.tvIncrease500.setOnClickListener {
            val price = Integer.parseInt(binding.etPrice.text.toString().replace(" ", ""))
            if (price + 500 <= maximumPrice) {
                val editable = Editable.Factory.getInstance().newEditable("${price + 500}")
                binding.etPrice.text = editable
            } else {
                errorHandler.showToast("Maximum price: $maximumPrice")
            }
        }
    }

    private fun displayPaymentMethod(paymentMethod: PaymentMethodType) {
        when (paymentMethod) {
            PaymentMethodType.CARD -> {
                binding.ivChangePaymentMethod.setImageResource(com.aralhub.ui.R.drawable.ic_credit_card_3d)
            }

            PaymentMethodType.CASH -> {
                binding.ivChangePaymentMethod.setImageResource(com.aralhub.ui.R.drawable.ic_cash)
            }
        }
    }

    private fun initObservers() {
        observeState(viewModel.paymentMethod) { paymentMethod -> displayPaymentMethod(paymentMethod) }
        observeState(viewModel.recommendedPriceUiState) { recommendedPriceUiState ->
            when (recommendedPriceUiState) {
                is RecommendedPriceUiState.Error -> errorHandler.showToast(recommendedPriceUiState.message)
                RecommendedPriceUiState.Loading -> {
                    binding.etPrice.hint = "..."
                }

                is RecommendedPriceUiState.Success -> {
                    displayRecommendedPrice(
                        recommendedPriceUiState.recommendedPrice
                    )
                    recommendedPrice = recommendedPriceUiState.recommendedPrice
                    binding.btnSendOffer.enable()
                }
            }
        }
        observeState(viewModel.sendOrderBottomSheetUiState) { sendOrderBottomSheetUiState ->
            when (sendOrderBottomSheetUiState) {
                is SendOrderBottomSheetUiState.Error -> errorHandler.showToast(
                    sendOrderBottomSheetUiState.message
                )

                SendOrderBottomSheetUiState.Loading -> {}
                is SendOrderBottomSheetUiState.Success -> {
                    navigation.goToOffersFromCreateOrderFragment()
                }
            }
        }
        observeState(viewModel.rideOptionsUiState) { rideOptionsUiState ->
            when (rideOptionsUiState) {
                is RideOptionsUiState.Error -> errorHandler.showToast(rideOptionsUiState.message)
                RideOptionsUiState.Loading -> {}
                is RideOptionsUiState.Success -> rideOptionItemAdapter.submitList(rideOptionsUiState.rideOptions)
            }
        }
    }


    private fun createCurrentLocationPlaceMarkObject(point: Point) {
        map?.let {
            if (it.isValid) {
                if (currentLocationPlaceMarkObject == null) {
                    currentLocationPlaceMarkObject = it.mapObjects.addPlacemark().apply {
                        geometry = point
                        imageProvider?.let { imageProvider -> setIcon(imageProvider) }
                    }
                } else {
                    currentLocationPlaceMarkObject?.geometry = point
                }
            }
        }
    }

    private fun displayRecommendedPrice(recommendedPrice: RecommendedPrice) {
        binding.tvPrice.text = getString(
            com.aralhub.ui.R.string.placeholder_recommended_price,
            recommendedPrice.recommendedAmount.toInt()
        )
        val editable = Editable.Factory.getInstance()
            .newEditable("${recommendedPrice.recommendedAmount.toInt()}")
        minimumPrice = recommendedPrice.minAmount.toInt()
        maximumPrice = recommendedPrice.maxAmount.toInt()
        binding.etPrice.text = editable
    }

    private fun drawDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
        Log.i("Routes", "Route: $drivingRoutes")
        val route = drivingRoutes[0]
        mapObjects?.clear()
        mapObjects?.addPolyline(route.geometry)
        val firstPoint = route.geometry.points.first()
        val lastPoint = route.geometry.points.last()
        addRouteFromPointMarker(firstPoint)
        addRouteToPointMarker(lastPoint)
        showFullRoute(route)
        drivingSession.cancel()
    }

    private fun addRouteFromPointMarker(point: Point) {
        val fromPointImageProvider =
            ImageProvider.fromResource(requireContext(), com.aralhub.ui.R.drawable.ic_pickup_marker)
        map?.let {
            if (it.isValid) {
                if (fromRoutePlaceMarkObject == null) {
                    fromRoutePlaceMarkObject = it.mapObjects.addPlacemark().apply {
                        geometry = point
                        setIcon(fromPointImageProvider)
                        setIconStyle(IconStyle().apply {
                            anchor = PointF(0.5f, 0.5f)
                            scale = 0.1f
                        })
                    }
                } else {
                    fromRoutePlaceMarkObject?.geometry = point
                }
            }
            //it.move(CameraPosition(point, 17.0f, 150.0f, 30.0f))
        }
    }

    private fun addRouteToPointMarker(point: Point) {
        val toPointImageProvider = ImageProvider.fromResource(
            requireContext(),
            com.aralhub.ui.R.drawable.ic_destination_marker
        )
        map?.let {
            if (it.isValid) {
                if (toRoutePlaceMarkObject == null) {
                    toRoutePlaceMarkObject = it.mapObjects.addPlacemark().apply {
                        geometry = point
                        setIcon(toPointImageProvider)
                        setIconStyle(IconStyle().apply {
                            anchor = PointF(0.5f, 0.5f)
                            scale = 0.1f
                        })

                    }

                } else {
                    toRoutePlaceMarkObject?.geometry = point
                }
            }
        }
    }


    companion object {
        fun args(selectedLocations: SelectedLocations) = Bundle().apply {
            putParcelable("selectedLocations", selectedLocations)
        }
    }
}