package com.aralhub.araltaxi.profile.driver.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.aralhub.araltaxi.core.common.error.ErrorHandler
import com.aralhub.araltaxi.core.common.file.UriEx.getFileFromUri
import com.aralhub.araltaxi.core.common.sharedpreference.DriverSharedPreference
import com.aralhub.araltaxi.profile.driver.R
import com.aralhub.araltaxi.profile.driver.databinding.FragmentProfileBinding
import com.aralhub.ui.adapter.ProfileItemAdapter
import com.aralhub.ui.utils.GlideEx.displayAvatar
import com.aralhub.ui.utils.LifecycleOwnerEx.observeState
import com.aralhub.ui.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val binding by viewBinding(FragmentProfileBinding::bind)

    private val viewModel by viewModels<ProfileViewModel>()

    private val adapter = ProfileItemAdapter()

    @Inject lateinit var errorHandler: ErrorHandler
    @Inject lateinit var driverSharedPreference: DriverSharedPreference

    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest?> = registerForActivityResult<PickVisualMediaRequest, Uri>(PickVisualMedia()) { uri: Uri? ->
        uri?.let { handleMediaSelection(it) } ?: Log.i("PhotoPicker", "No media selected")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getDriverProfile()
        viewModel.getDriverCard()
        initViews()
        initObservers()
        initListeners()
    }

    private fun initViews() {
        binding.rvProfile.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.rvProfile.addItemDecoration(dividerItemDecoration)
    }

    private fun initObservers() {
        observeState(viewModel.combinedUiState){
            adapter.submitList(it)
        }
        observeState(viewModel.profileUiState){ profileUiState ->
            when(profileUiState){
                is ProfileUiState.Error -> errorHandler.showToast(profileUiState.message)
                ProfileUiState.Loading -> {}
                is ProfileUiState.Success -> {
                    driverSharedPreference.avatar = profileUiState.avatar
                    displayAvatar(profileUiState.avatar, binding.ivAvatar)
                }
            }
        }
        observeState(viewModel.uploadImageUiState){ uploadImageUiState ->
            when(uploadImageUiState){
                is UploadImageUiState.Error -> errorHandler.showToast(uploadImageUiState.message)
                UploadImageUiState.Loading -> {}
                UploadImageUiState.Success -> viewModel.getDriverProfile()
            }
        }
    }

    private fun initListeners() {
        binding.tbProfile.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.ivAvatar.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageOnly)
                .build())
        }
    }

    private fun handleMediaSelection(uri: Uri) {
        val file = getFileFromUri(requireContext(), uri)
        file?.let { viewModel.uploadImage(it) }
    }


}