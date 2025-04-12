package com.aralhub.araltaxi.language.driver

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aralhub.araltaxi.core.common.sharedpreference.DriverSharedPreference
import com.aralhub.araltaxi.language.driver.databinding.FragmentChangeLanguageBinding
import com.aralhub.ui.utils.viewBinding
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeLanguageFragment : Fragment(R.layout.fragment_change_language) {

    private val binding by viewBinding(FragmentChangeLanguageBinding::bind)

    @Inject
    lateinit var driverSharedPreference: DriverSharedPreference

    companion object {
        const val QR_LANGUAGE_CODE = "kaa"
        const val RU_LANGUAGE_CODE = "ru"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()

    }

    private fun setupUI() {
        updateLanguageContainerByIndex(driverSharedPreference.languageIndex)
    }

    private fun setupListeners() {
        binding.apply {
            tbHistory.setNavigationOnClickListener {
                popBackStack()
            }
            qrLanguageContainer.setOnClickListener {
                driverSharedPreference.languageIndex = 0
                updateLanguageContainerByIndex(0)
            }
            ruLanguageContainer.setOnClickListener {
                driverSharedPreference.languageIndex = 1
                updateLanguageContainerByIndex(1)
            }
            btnSave.setOnClickListener {
                Lingver.getInstance().setLocale(
                    requireContext(),
                    if (driverSharedPreference.languageIndex == 0) QR_LANGUAGE_CODE else RU_LANGUAGE_CODE
                )

                popBackStack()
            }
        }
    }

    private fun updateLanguageContainerByIndex(
        selectedLanguageContainerIndex: Int,
    ) {
        when (selectedLanguageContainerIndex) {
            0 -> {
                binding.qrSelectableIcon.apply {
                    imageTintList = ContextCompat.getColorStateList(
                        requireContext(),
                        com.aralhub.ui.R.color.color_interactive_control
                    )
                    setImageResource(com.aralhub.ui.R.drawable.ic_check_circle)
                }
                binding.ruSelectableIcon.apply {
                    setImageResource(com.aralhub.ui.R.drawable.shape_circle_icon_gray)
                    imageTintList = ContextCompat.getColorStateList(
                        requireContext(),
                        com.aralhub.ui.R.color.color_gray_2
                    )
                }
            }

            1 -> {
                binding.qrSelectableIcon.apply {
                    setImageResource(com.aralhub.ui.R.drawable.shape_circle_icon_gray)
                    imageTintList = ContextCompat.getColorStateList(
                        requireContext(),
                        com.aralhub.ui.R.color.color_gray_2
                    )
                }
                binding.ruSelectableIcon.apply {
                    setImageResource(com.aralhub.ui.R.drawable.ic_check_circle)
                    imageTintList = ContextCompat.getColorStateList(
                        requireContext(),
                        com.aralhub.ui.R.color.color_interactive_control
                    )
                }
            }
        }
    }

    private fun popBackStack() {
        findNavController().navigateUp()
    }

}