package com.aralhub.overview.sheet

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.provider.Settings
import android.view.View
import com.aralhub.araltaxi.overview.R
import com.aralhub.araltaxi.overview.databinding.ModalBottomSheetLocationServiceOffBinding
import com.aralhub.ui.utils.viewBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LocationServiceOffModalBottomSheet :
    BottomSheetDialogFragment(R.layout.modal_bottom_sheet_location_service_off) {

    private val binding by viewBinding(ModalBottomSheetLocationServiceOffBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        binding.btnEnable.setOnClickListener {
            showGPSDialog()
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GPS_CODE) {
            if (resultCode == Settings.Secure.LOCATION_MODE_OFF) {
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "LocationServiceOffModalBottomSheet"
        private const val REQUEST_GPS_CODE = 1001
    }
}