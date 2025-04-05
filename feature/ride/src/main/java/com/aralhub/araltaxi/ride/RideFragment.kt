package com.aralhub.araltaxi.ride

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.aralhub.araltaxi.client.ride.R
import com.aralhub.araltaxi.client.ride.databinding.FragmentRideBinding
import com.aralhub.araltaxi.core.common.error.ErrorHandler
import com.aralhub.araltaxi.core.common.utils.MapStyles
import com.aralhub.araltaxi.core.common.utils.loadJsonFromAssets
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideBottomSheetNavigation
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideNavigation
import com.aralhub.araltaxi.ride.navigation.sheet.SheetNavigator
import com.aralhub.araltaxi.ride.sheet.modal.CancelTripFragment
import com.aralhub.araltaxi.ride.sheet.modal.TripCanceledByDriverFragment
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.viewBinding
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class RideFragment : Fragment(R.layout.fragment_ride) {
    private val binding by viewBinding(FragmentRideBinding::bind)

    @Inject
    lateinit var navigator: SheetNavigator

    @Inject
    lateinit var navigation: FeatureRideNavigation

    @Inject
    lateinit var bottomSheetNavigation: FeatureRideBottomSheetNavigation

    @Inject
    lateinit var errorHandler: ErrorHandler
    private val rideViewModel by activityViewModels<RideViewModel>()

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMapView()
        initObservers()
        startService()
        setMapStyle()
    }

    private fun setMapStyle() {
        val minimalisticMapStyle =
            loadJsonFromAssets(requireContext(), MapStyles.MINIMALISTIC_MAP_STYLE)
        binding.mapView.map.setMapStyle(minimalisticMapStyle)
    }

    private fun startService() {
        val intent = Intent(requireContext(), RideService::class.java)
        requireActivity().startService(intent)
    }

    private fun initObservers() {
        observeState(rideViewModel.cancelRideState) { cancelRideUiState ->
            when (cancelRideUiState) {
                is CancelRideUiState.Error -> {}
                CancelRideUiState.Loading -> {}
                CancelRideUiState.Success -> {
                    findNavController().navigateUp()
                }
            }
        }
        observeState(rideViewModel.activeRideState) { activeRideUiState ->
            when (activeRideUiState) {
                is ActiveRideUiState.Error -> {}
                ActiveRideUiState.Loading -> {}
                is ActiveRideUiState.Success -> {
                    Log.i("Status", "${activeRideUiState.activeRide.status}")
                    when (activeRideUiState.activeRide.status) {
                        FragmentRideStatus.DRIVER_ON_THE_WAY.status -> {
                            setStartDestination(R.id.waitingForDriverBottomSheet)
                        }

                        FragmentRideStatus.DRIVER_WAITING_CLIENT.status -> {
                            setStartDestination(R.id.driverIsWaitingBottomSheet)
                        }

                        FragmentRideStatus.PAID_WAITING_STARTED.status -> {
                            setStartDestination(R.id.driverIsWaitingBottomSheet)
                        }

                        FragmentRideStatus.PAID_WAITING.status -> {
                            setStartDestination(R.id.driverIsWaitingBottomSheet)
                        }

                        FragmentRideStatus.RIDE_STARTED.status -> {
                            setStartDestination(R.id.rideBottomSheet)
                        }

                        FragmentRideStatus.RIDE_COMPLETED.status -> {
                            setStartDestination(R.id.rideFinishedBottomSheet)
                        }

                        FragmentRideStatus.CANCELED_BY_DRIVER.status -> {
                            TripCanceledByDriverFragment(
                                onClearClick = {
                                    navigation.goBackToCreateOfferFromRide()
                                }
                            ).show(childFragmentManager, CancelTripFragment.TAG)
                        }
                    }

                }
            }
        }
    }

    private fun setStartDestination(fragment: Int) {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.ride_nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        navController.let { navigator.bind(navController) }
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_ride)
        graph.setStartDestination(fragment)
        navController.graph = graph
    }

    private fun setUpMapView() {
        binding.mapView.mapWindow.map.move(
            CameraPosition(
                Point(42.4619, 59.6166),
                17.0f,
                150.0f,
                30.0f
            )
        )
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        navigator.unbind()
        super.onDestroyView()
    }

}