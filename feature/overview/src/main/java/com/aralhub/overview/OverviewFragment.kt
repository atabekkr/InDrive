package com.aralhub.overview

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aralhub.araltaxi.core.common.sharedpreference.DriverSharedPreference
import com.aralhub.araltaxi.core.common.utils.isGPSEnabled
import com.aralhub.araltaxi.overview.R
import com.aralhub.araltaxi.overview.databinding.FragmentOverviewBinding
import com.aralhub.indrive.core.data.model.driver.DriverInfo
import com.aralhub.overview.navigation.FeatureOverviewNavigation
import com.aralhub.overview.sheet.LocationServiceOffModalBottomSheet
import com.aralhub.overview.sheet.LogoutModalBottomSheet
import com.aralhub.overview.utils.BottomSheetBehaviorDrawerListener
import com.aralhub.ui.dialog.ErrorMessageDialog
import com.aralhub.ui.dialog.LoadingDialog
import com.aralhub.ui.utils.GlideEx
import com.aralhub.ui.utils.viewBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OverviewFragment : Fragment(R.layout.fragment_overview) {

    private val binding by viewBinding(FragmentOverviewBinding::bind)

    @Inject
    lateinit var driverSharedPreference: DriverSharedPreference

    private var pLauncher: ActivityResultLauncher<Array<String>>? = null

    private val locationServiceOffModalBottomSheet = LocationServiceOffModalBottomSheet()

    private var errorDialog: ErrorMessageDialog? = null
    private var loadingDialog: LoadingDialog? = null
    private var isResponseReceived = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        errorDialog = ErrorMessageDialog(context)
        loadingDialog = LoadingDialog(context)
    }

    @Inject
    lateinit var navigation: FeatureOverviewNavigation
    private val viewModel by viewModels<OverviewViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchData()
        initObservers()
        initViews()
        initListeners()
        registerPermissionListener()
        checkLocationPermission()

    }

    private fun fetchData() {
        viewModel.getProfile()
        viewModel.getBalance()
    }

    private fun initObservers() {
        viewModel.profileUiState.onEach {
            when (it) {
                is ProfileUiState.Error -> showErrorDialog(it.message)

                is ProfileUiState.Loading -> showLoading()

                is ProfileUiState.Success -> displayProfile(it.profile)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.balanceUiState.onEach {
            when (it) {
                is DriverBalanceUiState.Error -> showErrorDialog(it.message)

                is DriverBalanceUiState.Loading -> showLoading()

                is DriverBalanceUiState.Success -> {
                    dismissLoading()
                    binding.tvBalance.text =
                        getString(com.aralhub.ui.R.string.standard_uzs_price, it.balance.toString())
                    binding.tvDailyIncome.text = getString(
                        com.aralhub.ui.R.string.standard_uzs_price,
                        it.dayBalance.toString()
                    )
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.logoutUiState.onEach {
            when (it) {
                is LogoutUiState.Error -> showErrorDialog(it.message)

                LogoutUiState.Loading -> showLoading()

                LogoutUiState.Success -> navigation.goToLogoFromOverview()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.activeOrdersUiState.onEach { result ->
            val bundle = Bundle()
            bundle.putParcelable("OrderDetail", result?.order)
            bundle.putString("Status", result?.status)
            navigation.goToAcceptOrders(bundle)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun initListeners() {
        binding.btnAcceptOrders.setOnClickListener {
            // check for location, if not enabled show: LocationServiceOffModalBottomSheet().show(childFragmentManager, LocationServiceOffModalBottomSheet.TAG)
            navigation.goToAcceptOrders()
        }
        binding.tbOverview.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(
                GravityCompat.START
            )
        }
        binding.navigationView.getHeaderView(0).setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            navigation.goToProfileFromOverview()
        }
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_support -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigation.goToSupportFromOverview()
                    true
                }

                R.id.action_log_out -> {
                    val logoutModalBottomSheet = LogoutModalBottomSheet()
                    logoutModalBottomSheet.show(childFragmentManager, LogoutModalBottomSheet.TAG)
                    logoutModalBottomSheet.setOnLogoutListener {
                        logoutModalBottomSheet.dismissAllowingStateLoss()
                        viewModel.logout()
                    }
                    true
                }

                R.id.action_my_revenue -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigation.goToRevenueFromOverview()
                    true
                }

                R.id.action_order_history -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigation.goToHistoryFromOverview()
                    true
                }

                R.id.action_change_language -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigation.goToChangeLanguageFromOverview()
                    true
                }

                else -> false
            }
        }
    }

    private fun initViews() {
        setUpDrawerLayout()
    }

    private fun displayProfile(profile: DriverInfo) {
        dismissLoading()
        binding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.tv_name).text =
            profile.fullName
        binding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.tv_phone).text =
            profile.phoneNumber
        GlideEx.displayAvatar(
            profile.avatar.toString(),
            binding.navigationView.getHeaderView(0).findViewById<ImageView>(R.id.iv_avatar)
        )

    }

    private fun setUpDrawerLayout() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutBottomSheet)
        binding.drawerLayout.addDrawerListener(BottomSheetBehaviorDrawerListener(bottomSheetBehavior))
        binding.textAppVersion.text = driverSharedPreference.appVersion
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkGpsEnabled()
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

    override fun onResume() {
        super.onResume()
        if (isGPSEnabled(requireContext()) && locationServiceOffModalBottomSheet.isAdded)
            locationServiceOffModalBottomSheet.dismiss()
    }

    private fun checkGpsEnabled() {
        if (!isGPSEnabled(requireContext()))
            locationServiceOffModalBottomSheet.show(
                childFragmentManager,
                LocationServiceOffModalBottomSheet.TAG
            )
    }

    private fun registerPermissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                Log.d("OverviewFragment", "go to accept orders")
                checkGpsEnabled()
            } else {
                Log.d("OverviewFragment", "Permission denied")
            }
        }
    }

    private fun showErrorDialog(errorMessage: String?) {
        errorDialog?.show(errorMessage)
        errorDialog?.setOnDismissClicked { errorDialog?.dismiss() }
        loadingDialog?.dismiss()
    }

    private fun showLoading() {
        Log.d("OverviewFragment", "show loading")
        loadingDialog?.show()
        viewLifecycleOwner.lifecycleScope.launch {
            delay(500)
            if (!isResponseReceived) {
                loadingDialog?.show()
            }
        }
    }

    private fun dismissLoading() {
        isResponseReceived = true
        loadingDialog?.dismiss()
    }

    private fun dismissErrorDialog() {
        errorDialog?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissErrorDialog()
        dismissLoading()
    }

    private fun showGPSDialog() {
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true) // 👈 this ensures the dialog appears

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show system dialog
                    exception.startResolutionForResult(requireActivity(), REQUEST_GPS_CODE)
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.printStackTrace()
                }
            }
        }
    }

    private val REQUEST_GPS_CODE = 1001
}