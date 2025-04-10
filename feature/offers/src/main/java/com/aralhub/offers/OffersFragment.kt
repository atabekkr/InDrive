package com.aralhub.offers

import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.aralhub.araltaxi.client.offers.R
import com.aralhub.araltaxi.client.offers.databinding.FragmentOffersBinding
import com.aralhub.ui.components.ErrorHandler
import com.aralhub.araltaxi.core.common.utils.MapStyles
import com.aralhub.araltaxi.core.common.utils.loadJsonFromAssets
import com.aralhub.offers.navigation.FeatureOffersNavigation
import com.aralhub.offers.navigation.sheet.SheetNavigator
import com.aralhub.ui.adapter.OfferItemAdapter
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.ViewEx.hide
import com.aralhub.ui.utils.ViewEx.show
import com.aralhub.ui.utils.viewBinding
import com.dotlottie.dlplayer.Mode
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.RotationType
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OffersFragment : Fragment(R.layout.fragment_offers) {

    private val binding by viewBinding(FragmentOffersBinding::bind)

    @Inject
    lateinit var navigator: SheetNavigator

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    lateinit var featureOffersNavigation: FeatureOffersNavigation
    private val offerItemAdapter by lazy { OfferItemAdapter() }
    private val viewModel by activityViewModels<OffersViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMapView()
        setUpRecyclerView()
        initBottomNavController()
        viewModel.getOffers()
        val config = Config.Builder()
            .autoplay(true)
            .speed(1f)
            .loop(true)
            .source(DotLottieSource.Asset("pulse.json")) // file name of json/.lottie
//            .source(DotLottieSource.Url("https://lottie.host/5525262b-4e57-4f0a-8103-cfdaa7c8969e/VCYIkooYX8.json"))
//            .source(DotLottieSource.Url("https://lottiefiles-mobile-templates.s3.amazonaws.com/ar-stickers/swag_sticker_piggy.lottie"))
            .playMode(Mode.FORWARD)
            .useFrameInterpolation(true)
            .build()

        binding.dotLottie.load(config)
        binding.dotLottie.play()
        initObservers()
        setDefaultMapStyle()
    }

    private fun setDefaultMapStyle() {
        val minimalisticMapStyle =
            loadJsonFromAssets(requireContext(), MapStyles.MINIMALISTIC_MAP_STYLE)
        binding.mapView.map.setMapStyle(minimalisticMapStyle)
    }

    private fun initObservers() {

        observeState(viewModel.searchRideUiState) { searchRideUiState ->
            when (searchRideUiState) {
                is SearchRideUiState.Error -> {}
                SearchRideUiState.Loading -> {}
                is SearchRideUiState.Success -> {
                    Log.i("Locations", "${searchRideUiState.searchRide.locations.points}")
                    val startLocation = searchRideUiState.searchRide.locations.points[0].coordinates
                    updateMap(
                        startLocation.latitude,
                        startLocation.longitude
                    )
                }
            }
        }


        observeState(viewModel.offersUiState) { offersUiState ->
            when (offersUiState) {
                is OffersUiState.Error -> errorHandler.showToast(offersUiState.message)
                OffersUiState.Loading -> {}
                is OffersUiState.Success -> {
                    offerItemAdapter.submitList(offersUiState.offers)
                    if (offersUiState.offers.isNotEmpty()) {
                        binding.dotLottie.stop()
                        binding.dotLottie.hide()
                    } else {
                        binding.dotLottie.show()
                        binding.dotLottie.play()
                    }
                }
            }
        }

        observeState(viewModel.autoTakeOfferUiState) { autoTakeOfferUiState ->
            when (autoTakeOfferUiState) {
                is AutoTakeOfferUiState.Error -> errorHandler.showToast(autoTakeOfferUiState.message)
                AutoTakeOfferUiState.Loading -> {}
                is AutoTakeOfferUiState.Success -> {
                    viewModel.closeOffersWebSocket()
                    Log.i("OffersFragment", "going to ride")
                    featureOffersNavigation.goToRideFragment()
                }
            }
        }

        observeState(viewModel.acceptOfferUiState) { acceptOfferUiState ->
            when (acceptOfferUiState) {
                is AcceptOfferUiState.Error -> errorHandler.showToast(acceptOfferUiState.message)
                AcceptOfferUiState.Loading -> {}
                AcceptOfferUiState.Success -> {}
            }
        }

        observeState(viewModel.declineOfferUiState) { declineOfferUiState ->
            when (declineOfferUiState) {
                is DeclineOfferUiState.Error -> errorHandler.showToast(declineOfferUiState.message)
                DeclineOfferUiState.Loading -> {}
                is DeclineOfferUiState.Success -> {
                    //  offerItemAdapter.removeItem(declineOfferUiState.position)
                    errorHandler.showToast("Offer declined")
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        binding.rvOffers.adapter = offerItemAdapter
        offerItemAdapter.setOnItemAcceptClickListener { it ->
            viewModel.acceptOffer(it.id)
        }

        offerItemAdapter.setOnItemDeclineClickListener { offerItem, position ->
            viewModel.declineOffer(offerItem.id, position)
        }
    }

    private fun initBottomNavController() {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.offers_nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        navController.let {
            navigator.bind(navController)
        }
    }

    private fun setUpMapView() {
        binding.mapView.onStart()
        binding.mapView.setNoninteractive(true)
    }

    private fun updateMap(latitude: Number, longitude: Number) {

        val imageProvider =
            ImageProvider.fromResource(context, com.aralhub.ui.R.drawable.ic_pickup_marker)
        val placeMarkObject = binding.mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            geometry = Point(latitude.toDouble(), longitude.toDouble())
            setIcon(imageProvider)
            setIconStyle(IconStyle().apply {
                anchor = PointF(0.5f, 0.5f)
                scale = 0.1f
                rotationType = RotationType.NO_ROTATION
            })

        }
        placeMarkObject.geometry = Point(latitude.toDouble(), longitude.toDouble())
        binding.mapView.mapWindow.map.move(
            CameraPosition(
                Point(latitude.toDouble(), longitude.toDouble()),
                17.0f,
                150.0f,
                30.0f
            )
        )

    }

    override fun onDestroy() {
        navigator.unbind()
        super.onDestroy()
    }

}