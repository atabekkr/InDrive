package com.aralhub.araltaxi

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.aralhub.araltaxi.client.R
import com.aralhub.araltaxi.navigation.Navigator
import com.aralhub.araltaxi.core.common.sharedpreference.ClientSharedPreference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var localStorage: ClientSharedPreference
    private val requiredPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions -> permissions.forEach { permission ->     } }
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.aralhub.ui.R.style.Theme_InDrive)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askPermissions()
        setPadding()
        if (localStorage.isLogin){
            setStartDestination(R.id.requestFragment)
        } else {
            setStartDestination(R.id.logoFragment)
        }
    }

    private fun setStartDestination(fragment: Int) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(fragment)
        navController.graph = graph
    }

    private fun askPermissions() {
        locationPermissionLauncher.launch(requiredPermissions)
    }

    private fun setPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_nav_host)) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view. This solution sets
            // only the bottom, left, and right dimensions, but you can apply whichever
            // insets are appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }

            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onResume() {
        super.onResume()
        navigator.bind(findNavController(R.id.main_nav_host))
    }

    override fun onPause() {
        navigator.unbind()
        super.onPause()
    }
}