package com.aralhub.araltaxi.profile.client.profile

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aralhub.araltaxi.core.common.sharedpreference.ClientSharedPreference
import com.aralhub.araltaxi.profile.client.R
import com.aralhub.araltaxi.profile.client.databinding.FragmentProfileBinding
import com.aralhub.araltaxi.profile.client.navigation.FeatureProfileNavigation
import com.aralhub.araltaxi.profile.client.sheet.DeleteAccountBottomSheet
import com.aralhub.indrive.core.data.model.client.ClientProfile
import com.aralhub.ui.dialog.ErrorMessageDialog
import com.aralhub.ui.dialog.LoadingDialog
import com.aralhub.ui.utils.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val binding by viewBinding(FragmentProfileBinding::bind)

    private val viewModel by viewModels<ProfileViewModel>()

    @Inject
    lateinit var navigation: FeatureProfileNavigation

    private var errorDialog: ErrorMessageDialog? = null
    private var loadingDialog: LoadingDialog? = null

    @Inject
    lateinit var clientSharedPreference: ClientSharedPreference

    private val deleteAccountBottomSheet = DeleteAccountBottomSheet()

    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest?> =
        registerForActivityResult<PickVisualMediaRequest, Uri>(PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                val file = getFileFromUri(requireContext(), uri)
                file?.let {
                    viewModel.uploadImage(it)
                }
            } else {
                Log.i("PhotoPicker", "No media selected")
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        errorDialog = ErrorMessageDialog(context)
        loadingDialog = LoadingDialog(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        initObservers()

    }

    private fun initObservers() {
        viewModel.getProfile()
        viewModel.profileUiState.onEach {
            when (it) {
                is ProfileUiState.Error -> showErrorDialog(it.message)
                ProfileUiState.Loading -> showLoading()
                is ProfileUiState.Success -> displayProfile(it.profile)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.uploadImageUiState.onEach {
            when (it) {
                is UploadImageUiState.Error -> Log.i("ProfileFragment: uploadImage", "error: $it")
                UploadImageUiState.Loading -> Log.i("ProfileFragment: uploadImage", "loading: $it")
                UploadImageUiState.Success -> viewModel.getProfile()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.deleteProfileUiState.onEach { result ->
            when (result) {
                is DeleteProfileUiState.Error -> {
                    showErrorDialog(result.message)
                }

                DeleteProfileUiState.Loading -> showLoading()

                DeleteProfileUiState.Success -> {
                    dismissLoading()
                    clientSharedPreference.clear()

                    navigation.goToLogoFragmentFromProfileFragment()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun displayProfile(profile: ClientProfile) {

        dismissLoading()

        clientSharedPreference.apply {
            userName = profile.fullName
            avatar = profile.profilePhoto
            phoneNumber = profile.phone
        }

        binding.tvName.text = profile.fullName
        binding.tvPhone.text = profile.phone
        Glide.with(requireContext())
            .load("https://araltaxi.aralhub.uz/${profile.profilePhoto}")
            .centerCrop()
            .placeholder(com.aralhub.ui.R.drawable.ic_user)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivAvatar)
    }

    private fun initListeners() {
        binding.tbProfile.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.ivAvatar.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(PickVisualMedia.ImageOnly)
                    .build()
            )
        }
        binding.tvDeleteAccount.setOnClickListener {
            if (!deleteAccountBottomSheet.isAdded)
                deleteAccountBottomSheet.show(childFragmentManager, deleteAccountBottomSheet.tag)
        }

        deleteAccountBottomSheet.setOnDeleteAccountClickListener {
            viewModel.deleteProfile()
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        }
        return tempFile
    }

    private fun showLoading() {
        loadingDialog?.show()
    }

    private fun dismissLoading() {
        loadingDialog?.dismiss()
    }

    private fun showErrorDialog(errorMessage: String?) {
        dismissLoading()
        errorDialog?.setOnDismissClicked { errorDialog?.dismiss() }
        errorDialog?.show(errorMessage)

    }

    private fun dismissErrorDialog() {
        errorDialog?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissLoading()
        dismissErrorDialog()
    }
}