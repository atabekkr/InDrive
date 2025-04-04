package com.aralhub.araltaxi.request.utils

import android.view.View
import android.widget.LinearLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BottomSheetBehaviorDrawerListener(private val bottomSheetBehavior: BottomSheetBehavior<View>): DrawerLayout.DrawerListener {
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
}