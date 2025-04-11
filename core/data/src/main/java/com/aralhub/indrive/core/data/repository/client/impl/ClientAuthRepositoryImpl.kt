package com.aralhub.indrive.core.data.repository.client.impl

import com.aralhub.indrive.core.data.model.client.ClientProfile
import com.aralhub.indrive.core.data.repository.client.ClientAuthRepository
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.network.UserNetworkDataSource
import com.aralhub.araltaxi.core.common.sharedpreference.ClientSharedPreference
import com.aralhub.network.models.NetworkResult
import com.aralhub.network.requests.auth.NetworkUserAuthRequest
import com.aralhub.network.requests.profile.NetworkUserProfileRequest
import com.aralhub.network.requests.verify.NetworkVerifyRequest
import java.io.File
import javax.inject.Inject

class ClientAuthRepositoryImpl @Inject constructor(private val localStorage: ClientSharedPreference, private val clientNetworkDataSource: UserNetworkDataSource) :
    ClientAuthRepository {

    override suspend fun clientAuth(phone: String): Result<Boolean> {
        clientNetworkDataSource.userAuth(NetworkUserAuthRequest(phone)).let {
            return when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success ->  Result.Success(data = true)
            }
        }
    }

    override suspend fun userVerify(phone: String, code: String): Result<Boolean> {
        clientNetworkDataSource.userVerify(NetworkVerifyRequest(phone, code)).let {
            return when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> {
                    localStorage.access = it.data.accessToken
                    localStorage.refresh = it.data.refreshToken
                    localStorage.isLogin = true
                    localStorage.phoneNumber = phone
                    Result.Success(data = true)
                }
            }
        }
    }

    override suspend fun userProfile(fullName: String): Result<Boolean> {
        clientNetworkDataSource.userProfile(NetworkUserProfileRequest(fullName)).let {
            return when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> {
                    localStorage.apply {
                        userName = it.data.fullName
                        avatar = it.data.profilePhotoUrl.toString()
                        phoneNumber = it.data.phoneNumber
                    }
                    Result.Success(data = true)
                }
            }
        }
    }

    override suspend fun uploadPhoto(file: File): Result<Boolean> {
        clientNetworkDataSource.userPhoto(file).let {
            return when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(data = true)
            }
        }
    }

    override suspend fun userMe(): Result<ClientProfile> {
        clientNetworkDataSource.getUserMe().let {
            return when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> {
                    localStorage.userId = it.data.id
                    Result.Success(
                        ClientProfile(
                            id = it.data.id,
                            fullName = it.data.fullName,
                            phone = it.data.phoneNumber,
                            profilePhoto = it.data.profilePhotoUrl ?: ""
                        )
                    )
                }
            }
        }
    }

    override suspend fun deleteUserProfile(): Result<Boolean> {
        clientNetworkDataSource.deleteUserProfile().let {
            return when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(data = true)
            }
        }
    }

    override suspend fun logout(): Result<Boolean> {
        clientNetworkDataSource.userLogout(refreshToken = localStorage.refresh).let {
            return when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(data = true)
            }
        }
    }
}