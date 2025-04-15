package com.aralhub.araltaxi.select_location

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.araltaxi.core.common.permission.PermissionHelper
import com.aralhub.araltaxi.core.common.utils.MapStyles
import com.aralhub.araltaxi.core.common.utils.loadJsonFromAssets
import com.aralhub.araltaxi.select_location.databinding.FragmentSelectLocationBinding
import com.aralhub.offers.utils.updateMapStyle
import com.aralhub.ui.utils.FloatLandAnimation
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.ViewEx.disable
import com.aralhub.ui.utils.ViewEx.enable
import com.aralhub.ui.utils.viewBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.map.SizeChangedListener
import com.yandex.mapkit.search.Address
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.mapkit.user_location.UserLocationLayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectLocationFragment : Fragment(R.layout.fragment_select_location) {

    private var mapWindow: MapWindow? = null
    private var map: Map? = null
    private var floatLandAnimation: FloatLandAnimation? = null

    @Inject
    lateinit var errorHandler: ErrorHandler
    private val viewModel by viewModels<SelectLocationViewModel>()
    private val binding by viewBinding(FragmentSelectLocationBinding::bind)
    private val movementHandler = Handler(Looper.getMainLooper())
    private val movementDelay = 500L

    private val handler = Handler(Looper.getMainLooper())
    private var isUpdating = false

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var userLocationLayer: UserLocationLayer? = null

    private val cameraListener =
        CameraListener { map, cameraPosition, cameraUpdateReason, finished ->
            if (!finished && !isMapMoving) {
                isMapMoving = true
                binding.btnSelectLocation.disable()
                floatLandAnimation?.startFloating()
            } else if (finished) {
                isMapMoving = false
                floatLandAnimation?.land()
            }
            // Handle map movement start
            if (!isMapMoving) {
                binding.btnSelectLocation.disable()
                isMapMoving = true
            }

            // Debounce to determine when map movement has stoppedte
            movementHandler.removeCallbacksAndMessages(null)
            movementHandler.postDelayed({
                if (finished) {
                    isMapMoving = false
                    // Update placemark and trigger search when map stops moving
                    val centerPoint = cameraPosition.target
                    updatePlacemarkAndSearchLocation(centerPoint)
                }
            }, movementDelay)
        }
    private val sizeChangedListener = SizeChangedListener { _, _, _ ->
        // Recalculate FocusRect and FocusPoint on every map's size change
        updateFocusInfo()
    }

    private var isMapMoving = false
    private var selectedLatitude = 0.0
    private var selectedLongitude = 0.0
    private var selectedTitle = ""
    private var selectedSubtitle = ""
    private var locationOwner = LOCATION_OWNER_UNSPECIFIED

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initMap()
        initObservers()
        initArgs()
        initViews()
        initListeners()

    }

    private fun initData() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun initViews() {
        binding.itemSelectLocation.ivIcon.setImageResource(com.aralhub.ui.R.drawable.ic_ic_round_pin_drop)
    }

    private fun initMap() {
        mapWindow = binding.mapView.mapWindow
        map = mapWindow?.map
        if (map?.isValid == true) {
            viewModel.setVisibleRegion(map!!.visibleRegion)
            mapWindow?.addSizeChangedListener(sizeChangedListener)
            map?.addCameraListener(cameraListener)
            updateFocusInfo()
        }

        val mapkit = MapKitFactory.getInstance()
        userLocationLayer = mapkit.createUserLocationLayer(binding.mapView.mapWindow)

        userLocationLayer?.isVisible = true

        setDefaultMapStyle()
        showCurrentLocation()
        mapCameraListener()

    }

    private fun moveCamera(point: Point) {
        map?.move(
            CameraPosition(point, 17.0f, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 0.5f),
            null
        )
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
        binding.mapView.mapWindow.map.updateMapStyle(zoom, requireContext())
    }

    private fun initListeners() {
        binding.btnSelectLocation.setOnClickListener {
            val result = Bundle().apply {
                putDouble("latitude", selectedLatitude)
                putDouble("longitude", selectedLongitude)
                putString("locationName", selectedTitle)
                putString("locationAddress", selectedSubtitle)
                putInt("owner", locationOwner)
            }

            parentFragmentManager.setFragmentResult("location_key", result)
            if (map?.isValid == true) {
                map?.removeCameraListener(cameraListener)
                Log.i(
                    "NavigationUp",
                    "SelectLocationFragment: $selectedLatitude, $selectedLongitude"
                )
                findNavController().navigateUp()
            }
        }
        binding.btnCurrentLocation.setOnClickListener { showCurrentLocation() }
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
    }

    private fun initArgs() {
        val owner = when (requireArguments().getInt("owner")) {
            LOCATION_OWNER_FROM -> LocationOwner.FROM
            LOCATION_OWNER_TO -> LocationOwner.TO
            else -> LocationOwner.UNSPECIFIED
        }
        binding.tvTitle.text = when (owner) {
            LocationOwner.FROM -> "Alıp ketiw ornı"
            LocationOwner.TO -> "Mánzil"
            LocationOwner.UNSPECIFIED -> "Mánzil"
        }
        locationOwner = when (owner) {
            LocationOwner.FROM -> LOCATION_OWNER_FROM
            LocationOwner.TO -> LOCATION_OWNER_TO
            LocationOwner.UNSPECIFIED -> LOCATION_OWNER_UNSPECIFIED
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
        floatLandAnimation = FloatLandAnimation(binding.iconCenter)
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        floatLandAnimation?.stopAnimations()
        super.onStop()
    }

    private fun initObservers() {
        observeState(viewModel.uiState) { selectLocationUiState ->
            when (selectLocationUiState.searchState) {
                SearchState.Error -> {
                    errorHandler.showToast("Error to find location name")
                }

                SearchState.Loading -> {
                    binding.itemSelectLocation.tvTitle.text = "Updating..."
                }

                SearchState.Off -> binding.itemSelectLocation.tvTitle.text = "Searching..."
                is SearchState.Success -> {
                    val address = selectLocationUiState.searchState.items.firstOrNull()?.geoObject
                        ?.metadataContainer
                        ?.getItem(ToponymObjectMetadata::class.java)
                        ?.address
                        ?.components
                        ?.map {
                            Log.i("Map", "${it.name} ${it.kinds}")
                        }

                    val house = selectLocationUiState.searchState.items.firstOrNull()?.geoObject
                        ?.metadataContainer
                        ?.getItem(ToponymObjectMetadata::class.java)
                        ?.address
                        ?.components
                        ?.firstOrNull { it.kinds.contains(Address.Component.Kind.STREET) }
                        ?.name

                    val street = selectLocationUiState.searchState.items.firstOrNull()?.geoObject
                        ?.metadataContainer
                        ?.getItem(ToponymObjectMetadata::class.java)
                        ?.address
                        ?.components
                        ?.firstOrNull { it.kinds.contains(Address.Component.Kind.STREET) }
                        ?.name

                    Log.i("Search", "$street, $house, $address")
                    /*selectLocationUiState.searchState.items.firstOrNull()?.geoObject?.name ?: "Unknown Location",*/
                    viewModel.selectLocation(
                        selectLocationUiState.searchState.items.firstOrNull()?.geoObject?.name
                            ?: "Unknown Location",
                        selectLocationUiState.searchState.items.firstOrNull()?.geoObject?.descriptionText
                            ?: "",
                        selectLocationUiState.searchState.items.firstOrNull()?.point ?: Point(
                            0.0,
                            0.0
                        )
                    )
                }
            }
        }

        observeState(viewModel.locationSelectedUiState) { locationSelectedUiState ->
            when (locationSelectedUiState) {
                LocationSelectedUiState.Error -> {}
                LocationSelectedUiState.Loading -> {}
                is LocationSelectedUiState.Success -> {
                    selectedLongitude = locationSelectedUiState.point.longitude
                    selectedLatitude = locationSelectedUiState.point.latitude
                    selectedTitle = locationSelectedUiState.title
                    selectedSubtitle = locationSelectedUiState.subtitle

                    binding.itemSelectLocation.tvTitle.text = locationSelectedUiState.title
                    binding.itemSelectLocation.tvSubtitle.text =
                        locationSelectedUiState.subtitle
                    binding.btnSelectLocation.enable()
                }
            }
        }
    }

    private fun showCurrentLocation() {
        if (PermissionHelper.arePermissionsGranted(
                requireContext(),
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        ) {
            fusedLocationClient?.lastLocation
                ?.addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    location?.let {
                        viewLifecycleOwner.lifecycleScope.launch {
                            moveCamera(
                                Point(
                                    it.latitude,
                                    it.longitude
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun updatePlacemarkAndSearchLocation(point: Point) {
        map?.let { viewModel.submitLocation(point, 17, it.visibleRegion) }
    }

    private fun updateFocusInfo() {
        val defaultPadding = resources.getDimension(R.dimen.default_focus_rect_padding)
        mapWindow?.let {
            it.focusRect = ScreenRect(
                ScreenPoint(defaultPadding, defaultPadding),
                ScreenPoint(
                    it.width() - defaultPadding,
                    it.height() - defaultPadding,
                )
            )
            it.focusPoint = ScreenPoint(
                it.width() / 2f,
                it.height() / 2f,
            )
        }
    }


    companion object {
        private const val LOCATION_OWNER_FROM = 0
        private const val LOCATION_OWNER_TO = 1
        private const val LOCATION_OWNER_UNSPECIFIED = -1

        enum class LocationOwner {
            FROM, TO, UNSPECIFIED
        }

        fun args(owner: LocationOwner) = Bundle().apply {
            putInt(
                "owner", when (owner) {
                    LocationOwner.FROM -> LOCATION_OWNER_FROM
                    LocationOwner.TO -> LOCATION_OWNER_TO
                    LocationOwner.UNSPECIFIED -> LOCATION_OWNER_UNSPECIFIED
                }
            )
        }
    }
}