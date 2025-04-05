package com.aralhub.araltaxi.request

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aralhub.araltaxi.client.request.R
import com.aralhub.araltaxi.client.request.databinding.FragmentRequestBinding
import com.aralhub.araltaxi.core.common.error.ErrorHandler
import com.aralhub.araltaxi.core.common.permission.PermissionHelper
import com.aralhub.araltaxi.request.navigation.FeatureRequestNavigation
import com.aralhub.araltaxi.request.utils.BottomSheetBehaviorDrawerListener
import com.aralhub.indrive.core.data.model.client.ClientProfile
import com.aralhub.ui.adapter.location.LocationItemAdapter
import com.aralhub.ui.model.LocationItemClickOwner
import com.aralhub.ui.model.args.LocationType
import com.aralhub.ui.model.args.SelectedLocation
import com.aralhub.ui.sheets.LoadingModalBottomSheet
import com.aralhub.ui.sheets.LogoutModalBottomSheet
import com.aralhub.ui.utils.GlideEx.displayAvatar
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.hideKeyboard
import com.aralhub.ui.utils.showKeyboardAndFocus
import com.aralhub.ui.utils.viewBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class RequestFragment : Fragment(R.layout.fragment_request) {

    private companion object {

        //To fetch args when go back from SelectLocationFragment
        private const val SELECT_LOCATION_OWNER_FROM = 0
        private const val SELECT_LOCATION_OWNER_TO = 1
        private const val SELECT_LOCATION_REQUEST_KEY = "location_key"
        private const val SELECT_LOCATION_KEY_LATITUDE = "latitude"
        private const val SELECT_LOCATION_KEY_LONGITUDE = "longitude"
        private const val SELECT_LOCATION_KEY_LOCATION_NAME = "locationName"
        private const val SELECT_LOCATION_KEY_LOCATION_OWNER = "owner"
        private const val NULL_STRING = "null" //locationName can be null

        //To fetch args when go back from CreateOrderFragment
        private const val CREATE_ORDER_REQUEST_KEY = "cancel"

        //CurrentLocation values
        private const val CURRENT_LOCATION_NOT_INITIALISED_VALUE = 0.0
        private var currentLongitude = CURRENT_LOCATION_NOT_INITIALISED_VALUE
        private var currentLatitude = CURRENT_LOCATION_NOT_INITIALISED_VALUE

        private val SMOOTH_ANIMATION = Animation(Animation.Type.SMOOTH, 0.4f)
    }

    private var startLocationName: String? = null
    private var endLocationName: String? = null

    private val binding by viewBinding(FragmentRequestBinding::bind)
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var searchAddressBottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    @Inject
    lateinit var navigation: FeatureRequestNavigation

    @Inject
    lateinit var errorHandler: ErrorHandler
    private val adapter = LocationItemAdapter()
    private val viewModel by viewModels<RequestViewModel>()
    private val requestViewModel2 by viewModels<RequestViewModel2>()
    private var locationManager: LocationManager? = null

    // Variables to track latest states
    private var latestSearchRideState: SearchRideUiState? = null
    private var latestActiveRideState: ActiveRideUiState? = null

    private var placeMarkObject: PlacemarkMapObject? = null
    private var isNavigatedToCreateOrderFragment = false
    private var isFullscreen = false
    private lateinit var mapWindow: MapWindow
    private lateinit var map: Map
    private var isProgrammaticChange = false

    private var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        binding.mapView.onStop()
        locationManager = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapWindow = binding.mapView.mapWindow
        map = mapWindow.map
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        observeStates()
        initViews()
        initListeners()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        setCurrentUserLocation()
    }

    override fun onResume() {
        super.onResume()
        locationManager?.let { observeLocationUpdates() }
    }

    @SuppressLint("MissingPermission")
    private fun observeLocationUpdates() {
        if (PermissionHelper.arePermissionsGranted(
                requireContext(),
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        ) {
            /*locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_REQUEST_MIN_TIME,
                LOCATION_REQUEST_MIN_DISTANCE,
                locationListener
            )*/
        }
    }

    private fun observeStates() {
        observeState(requestViewModel2.currentLocationFlow) {
            updateMap(it.longitude, it.latitude)
            currentLatitude = it.latitude
            currentLongitude = it.longitude

            setStartLocationName(it.name)
        }
        observeState(requestViewModel2.fromLocationFlow) {
            it?.let { fromLocation ->
                setStartLocationName(fromLocation.name)
            }
        }
        observeState(requestViewModel2.toLocationFlow) {
            it?.let { toLocation ->
                isNavigatedToCreateOrderFragment = false
                binding.tvEndLocation.text = toLocation.name
                endLocationName = toLocation.name
            }
        }
        observeState(requestViewModel2.navigateToCreateOrderFlow) { selectedLocations ->
            Log.i("Location", "Navigate to Create Order $isNavigatedToCreateOrderFragment")
            if (!isNavigatedToCreateOrderFragment && selectedLocations != null) {
                isNavigatedToCreateOrderFragment = true
                navigation.goToCreateOrderFromRequestFragment(selectedLocations)
                requestViewModel2.clearToLocation()
            }
        }
        observeState(viewModel.suggestionsUiState) { suggestionsUiState ->
            when (suggestionsUiState) {
                is SuggestionsUiState.Error -> errorHandler.showToast(suggestionsUiState.message)
                SuggestionsUiState.Loading -> {}
                is SuggestionsUiState.Success -> {
                    adapter.submitList(suggestionsUiState.suggestions)
                }
            }
        }
        observeState(viewModel.searchRideUiState) { searchRideUiState ->
            latestSearchRideState = searchRideUiState
            updateLoadingDialog()
            when (searchRideUiState) {
                is SearchRideUiState.Error -> {}
                SearchRideUiState.Loading -> {}
                is SearchRideUiState.Success -> {
                    navigation.goToGetOffersFromRequestFragment()
                }
            }
        }

        observeState(viewModel.activeRideUiState) { activeRideUiState ->
            latestActiveRideState = activeRideUiState
            updateLoadingDialog()
            when (activeRideUiState) {
                is ActiveRideUiState.Error -> {}
                ActiveRideUiState.Loading -> {}
                is ActiveRideUiState.Success -> {
                    Log.i("RequestFragment", "Active Ride Success")
                    navigation.goToRideFragmentFromRequestFragment()
                }
            }
        }
        observeState(viewModel.profileUiState) { profileUiState ->
            Log.i("Profile", "${profileUiState}")
            when (profileUiState) {
                is ProfileUiState.Error -> errorHandler.showToast(profileUiState.message)
                ProfileUiState.Loading -> {}
                is ProfileUiState.Success -> {
                    displayProfile(profileUiState.profile)
                }
            }
        }
        observeState(viewModel.logOutUiState) { logOutUiState ->
            when (logOutUiState) {
                is LogOutUiState.Error -> errorHandler.showToast(logOutUiState.message)
                LogOutUiState.Loading -> {}
                LogOutUiState.Success -> navigation.goToLogoFromRequestFragment()
            }
        }
    }

    private fun setCurrentUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient?.lastLocation
                ?.addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    location?.let {
                        requestViewModel2.setCurrentLocation(
                            latitude = it.latitude,
                            longitude = location.longitude
                        )
                    }
                }
        }
    }

    private fun initViews() {
        binding.bottomSheetSearchAddress.recyclerViewAddress.adapter = adapter
        setUpBottomSheet()
        searchAddressBottomSheetBehavior =
            BottomSheetBehavior.from(binding.bottomSheetSearchAddress.bottomSheetSearchAddress)
        searchAddressBottomSheetBehavior?.peekHeight =
            (resources.displayMetrics.heightPixels * 0.95).toInt()
        searchAddressBottomSheetBehavior!!.apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
            expandedOffset = 0  // Убирает отступ сверху в раскрытом состоянии
        }
    }

    private fun initListeners() {

        val etFromLocation = binding.bottomSheetSearchAddress.dynamicLayout.etFromLocation
        val etToLocation = binding.bottomSheetSearchAddress.dynamicLayout.etToLocation

        initFragmentResultListener()
        initAdapterListener()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            true
        ) {
            if (isFullscreen || adapter.currentList.isNotEmpty()) {
                adapter.submitList(null)
            } else {
                requireActivity().finish()
            }
        }

        binding.mapView.setOnClickListener {
            if (isFullscreen) {
                isFullscreen = false
            }
        }

        etFromLocation.setOnTextChangedListener {
            if (it.isNotEmpty() && !it.isNullOrBlank()) {
                Log.i("Suggest", "Suggest Location")
                viewModel.suggestLocation(it, LocationItemClickOwner.FROM)
            } else {
                adapter.submitList(null)
            }
        }

        etToLocation.setOnTextChangedListener {
            if (it.isNotEmpty() && !it.isNullOrBlank()) {
                Log.i("Suggest 2", "Suggest Location 2")
                viewModel.suggestLocation(it, LocationItemClickOwner.TO)
            } else {
                adapter.submitList(null)
            }
        }

        binding.layoutFromLocation.setOnClickListener {
            showFullScreenBottomSheet(locationItemClickOwner = LocationItemClickOwner.FROM)
        }
        binding.layoutToLocation.setOnClickListener {
            showFullScreenBottomSheet(locationItemClickOwner = LocationItemClickOwner.TO)
        }

        etFromLocation.setEndTextClickListener {
            navigation.goToSelectFromLocationFromRequestFragment()
        }

        etToLocation.setEndTextClickListener {
            navigation.goToSelectToLocationFromRequestFragment()
        }

        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.btnCurrentLocation.setOnClickListener {
            if (currentLatitude != CURRENT_LOCATION_NOT_INITIALISED_VALUE && currentLongitude != CURRENT_LOCATION_NOT_INITIALISED_VALUE) {
                updateMap(currentLongitude, currentLatitude)
            }
        }


        searchAddressBottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.d("BottomSheet", "Развернут (STATE_EXPANDED)")
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Log.d("BottomSheet", "Свернут (STATE_COLLAPSED)")
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                        binding.root.hideKeyboard()
                        Log.d("BottomSheet", "Перетаскивается (STATE_DRAGGING)")
                    }

                    BottomSheetBehavior.STATE_SETTLING -> {
                        Log.d("BottomSheet", "Фиксируется (STATE_SETTLING)")
                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.root.hideKeyboard()
                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        Log.d("BottomSheet", "Частично развернут (STATE_HALF_EXPANDED)")
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d("BottomSheet", "Скролл, offset: $slideOffset")
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            true
        ) {
            if (searchAddressBottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                searchAddressBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            } else {
                requireActivity().finish()
            }
        }


        initNavigationViewListener()
        initDrawerListener()

    }

    private fun initFragmentResultListener() {
        parentFragmentManager.clearFragmentResultListener(SELECT_LOCATION_REQUEST_KEY)
        parentFragmentManager.setFragmentResultListener(
            SELECT_LOCATION_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val latitude = bundle.getDouble(SELECT_LOCATION_KEY_LATITUDE)
            val longitude = bundle.getDouble(SELECT_LOCATION_KEY_LONGITUDE)
            val locationName = bundle.getString(SELECT_LOCATION_KEY_LOCATION_NAME) ?: NULL_STRING
            val locationOwner = bundle.getInt(SELECT_LOCATION_KEY_LOCATION_OWNER)
            when (locationOwner) {
                SELECT_LOCATION_OWNER_FROM -> {
                    isProgrammaticChange = true
                    setStartLocationName(locationName)
                    isProgrammaticChange = false
                    requestViewModel2.setFromLocation(
                        SelectedLocation(
                            name = locationName,
                            longitude = longitude,
                            latitude = latitude,
                            locationType = LocationType.FROM
                        )
                    )
                }

                SELECT_LOCATION_OWNER_TO -> {
                    Log.i("Location", "To Location $locationName")
                    requestViewModel2.setToLocation(
                        SelectedLocation(
                            name = locationName,
                            longitude = longitude,
                            latitude = latitude,
                            locationType = LocationType.TO
                        )
                    )
                }
            }
        }

        parentFragmentManager.setFragmentResultListener(
            CREATE_ORDER_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, _ ->
            isNavigatedToCreateOrderFragment = false
        }
    }

    private fun setStartLocationName(startLocationName: String?) {
        this.startLocationName = startLocationName
        binding.tvStartLocation.text = startLocationName
        binding.tvStartLocation.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                com.aralhub.ui.R.color.color_text_primary
            )
        )
    }

    private fun initAdapterListener() {
        adapter.setOnItemClickListener {
            when (it.clickOwner) {
                LocationItemClickOwner.FROM -> {
                    requestViewModel2.setFromLocation(
                        SelectedLocation(
                            name = it.title,
                            longitude = it.longitude,
                            latitude = it.latitude,
                            locationType = LocationType.FROM
                        )
                    )
                    adapter.submitList(null)
                }

                LocationItemClickOwner.TO -> {
                    binding.tvEndLocation.text = it.title
                    endLocationName = it.title
                    requestViewModel2.setToLocation(
                        SelectedLocation(
                            name = it.title,
                            longitude = it.longitude,
                            latitude = it.latitude,
                            locationType = LocationType.TO
                        )
                    )
                    adapter.submitList(null)
                }
            }
            searchAddressBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun initDrawerListener() {
        if (bottomSheetBehavior != null) {
            binding.drawerLayout.addDrawerListener(
                BottomSheetBehaviorDrawerListener(bottomSheetBehavior!!)
            )
        }
    }

    private fun initNavigationViewListener() {
        binding.navigationView.getHeaderView(0).setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            navigation.goToProfileFromRequestFragment()
        }
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_support -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigation.goToSupportFromRequestFragment()
                    true
                }

                R.id.action_log_out -> {
                    val logOutModalBottomSheet = LogoutModalBottomSheet()
                    logOutModalBottomSheet.show(childFragmentManager, LogoutModalBottomSheet.TAG)
                    logOutModalBottomSheet.setOnLogoutListener {
                        logOutModalBottomSheet.dismissAllowingStateLoss()
                        viewModel.logOut()
                    }
                    true
                }

                R.id.action_my_addresses -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigation.goToSavedPlacesFromRequestFragment()
                    true
                }

                R.id.action_trip_history -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigation.goToHistoryFromRequestFragment()
                    true
                }

                else -> false
            }
        }
    }

    private fun setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutBottomSheet)
    }

    private fun showFullScreenBottomSheet(locationItemClickOwner: LocationItemClickOwner) {
        searchAddressBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        val etFromLocation = binding.bottomSheetSearchAddress.dynamicLayout.etFromLocation
        val etToLocation = binding.bottomSheetSearchAddress.dynamicLayout.etToLocation

        when (locationItemClickOwner) {
            LocationItemClickOwner.FROM -> {
                etFromLocation.showKeyboardAndFocus()
            }

            LocationItemClickOwner.TO -> {
                etToLocation.showKeyboardAndFocus()
            }
        }

        startLocationName?.let { etFromLocation.text = it }
        endLocationName?.let { etToLocation.text = it }

    }

    private fun updateLoadingDialog() {
        val searchState = latestSearchRideState
        val activeState = latestActiveRideState

        // Show dialog if we haven't received final states yet
        if (searchState == null || activeState == null ||
            (searchState is SearchRideUiState.Loading || activeState is ActiveRideUiState.Loading)
        ) {
            showLoadingDialog()
            return
        }

        // Hide dialog when:
        // 1. One of them is Success
        // 2. Both are Error
        when {
            searchState is SearchRideUiState.Success || activeState is ActiveRideUiState.Success -> {
                hideLoadingDialog()
            }

            searchState is SearchRideUiState.Error && activeState is ActiveRideUiState.Error -> {
                hideLoadingDialog()
            }
        }
    }

    private fun updateMap(longitude: Double, latitude: Double) {
        Log.i("RequestFragment", "Update Map")
        placeMarkObject?.let {
            binding.mapView.mapWindow.map.mapObjects.clear()
        }
        val imageProvider = ImageProvider.fromResource(context, com.aralhub.ui.R.drawable.ic_vector)
        placeMarkObject = binding.mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            geometry = Point(latitude, longitude)
            setIcon(imageProvider)
            isVisible = true
        }
        binding.mapView.mapWindow.map.move(
            CameraPosition(Point(latitude, longitude), 17.0f, 0f, 0f),
            SMOOTH_ANIMATION,
            null
        )
    }

    private fun displayProfile(profile: ClientProfile) {
        binding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.tv_name).text =
            profile.fullName
        binding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.tv_phone).text =
            profile.phone
        val imageView =
            binding.navigationView.getHeaderView(0).findViewById<ImageView>(R.id.iv_avatar)
        displayAvatar(profile.profilePhoto, imageView)
    }

    private fun showLoadingDialog() {
        Log.i("Dialog", "Show")
        LoadingModalBottomSheet.show(childFragmentManager)
    }

    private fun hideLoadingDialog() {
        Log.i("Dialog", "Hide")
        LoadingModalBottomSheet.hide(childFragmentManager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LoadingModalBottomSheet.hide(childFragmentManager)
    }


}