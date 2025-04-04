package com.aralhub.client.clientauth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.aralhub.araltaxi.client.auth.R
import com.aralhub.araltaxi.client.auth.databinding.FragmentLogoBinding
import com.aralhub.client.clientauth.navigation.FeatureClientAuthNavigation
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LogoFragment : Fragment(R.layout.fragment_logo) {

    private val binding by viewBinding(FragmentLogoBinding::bind)

    @Inject
    lateinit var navigator: FeatureClientAuthNavigation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewNextButton.setOnClickListener { navigator.goToAddPhoneNumber() }
    }
}