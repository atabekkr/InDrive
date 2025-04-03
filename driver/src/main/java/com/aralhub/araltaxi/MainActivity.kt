package com.aralhub.araltaxi

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.aralhub.araltaxi.core.common.sharedpreference.DriverSharedPreference
import com.aralhub.araltaxi.driver.R
import com.aralhub.araltaxi.navigation.Navigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var localStorage: DriverSharedPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.aralhub.ui.R.style.Theme_InDrive)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setPadding()
        if (localStorage.isLogin){
            setStartDestination(R.id.overviewFragment)
        } else {
            setStartDestination(R.id.logoFragment)
        }
    }

    private fun setPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, windowInsets ->
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

    private fun setStartDestination(fragment: Int) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main) as NavHostFragment
        val navController = navHostFragment.navController
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(fragment)
        navController.graph = graph
    }

    override fun onResume() {
        super.onResume()
        navigator.bind(findNavController(R.id.main))
    }

    override fun onPause() {
        navigator.unbind()
        super.onPause()
    }
}