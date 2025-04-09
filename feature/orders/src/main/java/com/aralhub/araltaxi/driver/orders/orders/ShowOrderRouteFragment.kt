package com.aralhub.araltaxi.driver.orders.orders

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aralhub.araltaxi.driver.orders.R
import com.aralhub.araltaxi.driver.orders.databinding.FragmentShowOrderRouteBinding
import com.aralhub.araltaxi.driver.orders.utils.updateMapStyle
import com.aralhub.ui.model.ClientRideLocationsCoordinatesUI
import com.aralhub.ui.model.OrderItem
import com.aralhub.ui.utils.viewBinding
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
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
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ShowOrderRouteFragment : Fragment(R.layout.fragment_show_order_route),
    DrivingSession.DrivingRouteListener {

    private val binding by viewBinding(FragmentShowOrderRouteBinding::bind)

    private var pLauncher: ActivityResultLauncher<Array<String>>? = null

    private var mapObjects: MapObjectCollection? = null
    private var placeMarkObject: PlacemarkMapObject? = null
    private var imageProvider: ImageProvider? = null

    private var drivingRouter: DrivingRouter? = null
    private var drivingRoute: DrivingRoute? = null

    private var routeStartLocation: Point? = null
    private var routeEndLocation: Point? = null

    private val handler = Handler(Looper.getMainLooper())
    private var isUpdating = false

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        binding.mapView.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerPermissionListener()
        checkLocationPermission()
        setupData()
        setupUI()
        setupListeners()
        mapCameraListener()

    }

    private fun setupUI() {
        val order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("OrderDetail", OrderItem::class.java)
        } else {
            arguments?.getParcelable("OrderDetail")
        }

        binding.tvFromLocation.text = order?.pickUpAddress
        binding.tvToLocation.text = order?.destinationAddress

        val startPoint = order?.locations?.getOrNull(0)?.coordinates
            ?: ClientRideLocationsCoordinatesUI(
                latitude = 42.39235517584616,
                longitude = 59.63039641031781
            )
        val endPoint = order?.locations?.getOrNull(1)?.coordinates
            ?: ClientRideLocationsCoordinatesUI(
                latitude = 42.42427044556464,
                longitude = 59.640008538840675
            )
        routeStartLocation = Point(startPoint.latitude, startPoint.longitude)
        routeEndLocation = Point(endPoint.latitude, endPoint.longitude)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            submitRouteRequest()
        }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { findNavController().navigateUp() }
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

    private fun addPointMarkerToRoute(point: Point, resId: Int) {
        imageProvider =
            ImageProvider.fromResource(requireContext(), resId)
        binding.mapView.map?.let {
            if (it.isValid) {
                placeMarkObject = it.mapObjects.addPlacemark().apply {
                    geometry = point
                    imageProvider?.let { imageProvider -> setIcon(imageProvider) }
                }
                placeMarkObject?.setIconStyle(
                    IconStyle().apply {
                        anchor = PointF(0.5f, 0.5f)
                        scale = 0.1f
                    }
                )
            }
        }

    }

    private fun setupData() {
        drivingRouter =
            DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
        mapObjects = binding.mapView.map.mapObjects.addCollection()
    }

    private fun submitRouteRequest() {
        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()
        if (routeStartLocation != null && routeEndLocation != null) {
            val requestPoints = buildList {
                add(
                    RequestPoint(
                        routeStartLocation!!, RequestPointType.WAYPOINT, null, null,
                        null
                    )
                )
                add(
                    RequestPoint(
                        routeEndLocation!!, RequestPointType.WAYPOINT, null, null,
                        null
                    )
                )
            }
            drivingRouter?.requestRoutes(
                requestPoints,
                drivingOptions,
                vehicleOptions,
                this
            )
        } else {
            Timber.d("Marshrut sızıw ımkani bolmadı")
        }
    }

    override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
        if (routes.isNotEmpty()) {
            drivingRoute = routes[0]
            mapObjects?.clear()
            mapObjects?.addPolyline(drivingRoute!!.geometry)
            val p1 = drivingRoute!!.geometry.points.first()
            val p2 = drivingRoute!!.geometry.points.last()
            addPointMarkerToRoute(p1!!, R.drawable.ic_pickup_marker)
            addPointMarkerToRoute(p2!!, R.drawable.ic_destination_marker)
            showFullRoute(drivingRoute!!)
        }
    }

    override fun onDrivingRoutesError(p0: Error) {
    }


    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                moveCamera()
            }

            else -> {
                pLauncher?.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun registerPermissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                moveCamera()
            } else {
                Timber.d("Permission denied")
            }
        }
    }

    private fun moveCamera() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationPermission()
            return
        } else {
            LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
                .addOnSuccessListener { location ->
                    binding.mapView.map.move(
                        CameraPosition(
                            Point(location.latitude, location.longitude), 14f, 0f, 0f
                        ), Animation(Animation.Type.SMOOTH, 0.5f), null
                    )
                }
        }
    }

    private fun showFullRoute(route: DrivingRoute) {

        // Получаем все точки маршрута
        val routePoints = route.geometry.points

        // Создаем границы, включающие все точки маршрута
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

        // Добавляем отступ для лучшего отображения
        val cameraPosition = binding.mapView.map.cameraPosition(Geometry.fromBoundingBox(bounds))
        val newPosition = CameraPosition(
            cameraPosition.target,
            cameraPosition.zoom - 0.5f, // Небольшой отступ от краев
            cameraPosition.azimuth,
            cameraPosition.tilt
        )

        // Анимированное перемещение камеры
        binding.mapView.map.move(
            newPosition,
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        drivingRouter=null
        drivingRoute=null
    }

}