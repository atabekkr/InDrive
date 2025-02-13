package com.aralhub.overview

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.aralhub.overview.databinding.FragmentOverviewBinding
import com.aralhub.overview.navigation.FeatureOverviewNavigation
import com.aralhub.overview.sheet.LocationServiceOffModalBottomSheet
import com.aralhub.ui.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OverviewFragment : Fragment(R.layout.fragment_overview) {
    private val binding by viewBinding(FragmentOverviewBinding::bind)

    @Inject
    lateinit var navigation: FeatureOverviewNavigation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpDrawerLayout()
        binding.btnAcceptOrders.setOnClickListener {
          //  LocationServiceOffModalBottomSheet().show(childFragmentManager, LocationServiceOffModalBottomSheet.TAG)
        navigation.goToAcceptOrders()
        }
    }

    private fun setUpDrawerLayout() {

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutBottomSheet)

        binding.tbOverview.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                bottomSheetBehavior.apply {
                    isHideable = true
                    state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            override fun onDrawerOpened(drawerView: View) {
                bottomSheetBehavior.apply {
                    isHideable = true
                    state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
            override fun onDrawerClosed(drawerView: View) {
                bottomSheetBehavior.apply {
                    isHideable = false
                    state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })
    }
}