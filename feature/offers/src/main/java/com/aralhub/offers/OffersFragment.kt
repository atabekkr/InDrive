package com.aralhub.offers

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.aralhub.offers.adapter.OfferItemAdapter
import com.aralhub.offers.databinding.FragmentOffersBinding
import com.aralhub.offers.model.OfferItem
import com.aralhub.offers.model.OfferItemDriver
import com.aralhub.offers.navigation.FeatureOffersNavigation
import com.aralhub.offers.navigation.sheet.SheetNavigator
import com.aralhub.ui.utils.viewBinding
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OffersFragment : Fragment(R.layout.fragment_offers) {
    private val binding by viewBinding(FragmentOffersBinding::bind)
    @Inject
    lateinit var navigator: SheetNavigator
    @Inject
    lateinit var featureOffersNavigation: FeatureOffersNavigation
    private val offerItemAdapter by lazy { OfferItemAdapter() }
    private val offers = listOf(
        OfferItem(
            id = 1,
            driver = OfferItemDriver(
                id = 1,
                name = "John Doe",
                carName = "Toyota Camry",
                rating = 4.5f,
                avatar = "https://randomuser.me/api/portraits/men/1.jpg"
            ),
            offeredPrice = "10000",
            timeToArrive = "5 min"
        ),
        OfferItem(
            id = 1,
            driver = OfferItemDriver(
                id = 1,
                name = "John Doe",
                carName = "Toyota Camry",
                rating = 4.5f,
                avatar = "https://randomuser.me/api/portraits/men/3.jpg"
            ),
            offeredPrice = "10000",
            timeToArrive = "5 min"
        ),
        OfferItem(
            id = 1,
            driver = OfferItemDriver(
                id = 1,
                name = "John Doe",
                carName = "Toyota Camry",
                rating = 4.5f,
                avatar = "https://randomuser.me/api/portraits/men/4.jpg"
            ),
            offeredPrice = "10000",
            timeToArrive = "5 min"
        ),
        OfferItem(
            id = 1,
            driver = OfferItemDriver(
                id = 1,
                name = "John Doe",
                carName = "Toyota Camry",
                rating = 4.5f,
                avatar = "https://randomuser.me/api/portraits/women/3.jpg"
            ),
            offeredPrice = "10000",
            timeToArrive = "5 min"
        ),
        OfferItem(
            id = 1,
            driver = OfferItemDriver(
                id = 1,
                name = "John Doe",
                carName = "Last",
                rating = 4.5f,
                avatar = "https://randomuser.me/api/portraits/women/4.jpg"
            ),
            offeredPrice = "10000",
            timeToArrive = "5 min"
        ),


        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMapView()
        setUpRecyclerView()
        initBottomNavController()
    }

    private fun setUpRecyclerView() {
        binding.rvOffers.adapter = offerItemAdapter
        offerItemAdapter.submitList(offers)
        offerItemAdapter.setOnItemAcceptClickListener {
            featureOffersNavigation.goToRideFragment()
        }

    }

    private fun initBottomNavController() {
        val navHostFragment = childFragmentManager.findFragmentById(R.id.offers_nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        navController.let {
            navigator.bind(navController)
        }
    }

    private fun setUpMapView() {
        binding.mapView.onStart()
        binding.mapView.mapWindow.map.move(
            CameraPosition(
                Point(42.4619, 59.6166),
                /* zoom = */ 17.0f,
                /* azimuth = */ 150.0f,
                /* tilt = */ 30.0f
            )
        )
    }

    override fun onDestroy() {
        navigator.unbind()
        super.onDestroy()
    }

}