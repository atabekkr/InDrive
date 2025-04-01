package com.aralhub.indrive.core.data.repository.driver.impl

import com.aralhub.araltaxi.core.common.sharedpreference.DriverSharedPreference
import com.aralhub.indrive.core.data.model.driver.DriverBalance
import com.aralhub.indrive.core.data.model.driver.DriverBalanceInfo
import com.aralhub.indrive.core.data.model.driver.DriverCard
import com.aralhub.indrive.core.data.model.driver.DriverInfo
import com.aralhub.indrive.core.data.model.driver.DriverProfileWithVehicle
import com.aralhub.indrive.core.data.model.location.SendLocationRequest
import com.aralhub.indrive.core.data.model.location.toDTO2
import com.aralhub.indrive.core.data.model.offer.ActiveOfferResponse
import com.aralhub.indrive.core.data.model.offer.toDomain
import com.aralhub.indrive.core.data.repository.driver.DriverAuthRepository
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.network.DriverNetworkDataSource
import com.aralhub.network.models.NetworkResult
import com.aralhub.network.requests.auth.NetworkDriverAuthRequest
import com.aralhub.network.requests.logout.NetworkLogoutRequest
import com.aralhub.network.requests.verify.NetworkVerifyRequest
import java.io.File
import javax.inject.Inject

class DriverAuthRepositoryImpl @Inject constructor(
    private val localStorage: DriverSharedPreference,
    private val driverNetworkDataSource: DriverNetworkDataSource
) : DriverAuthRepository {
    override suspend fun driverAddPhone(phone: String): Result<Boolean> {
        driverNetworkDataSource.driverAuth(
            networkDriverAuthRequest = NetworkDriverAuthRequest(
                phoneNumber = phone
            )
        ).let {
            return when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(data = true)
            }
        }
    }

    override suspend fun driverAuthVerify(phone: String, code: String): Result<Boolean> {
        driverNetworkDataSource.driverVerify(
            networkDriverVerifyRequest = NetworkVerifyRequest(
                phoneNumber = phone,
                code = code
            )
        ).let {
            return when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> {
                    localStorage.apply {
                        access = it.data.accessToken
                        refresh = it.data.refreshToken
                        isLogin = true
                        phoneNumber = phone
                    }
                    Result.Success(data = true)
                }
            }
        }
    }

    override suspend fun driverInfo(): Result<DriverInfo> {
        driverNetworkDataSource.getDriverInfo().let {
            return when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> {
                    localStorage.apply {
                        userName = it.data.fullName
                        avatar = it.data.avatar.toString()
                        phoneNumber = it.data.phoneNumber
                    }
                    Result.Success(
                        DriverInfo(
                            phoneNumber = it.data.phoneNumber,
                            fullName = it.data.fullName,
                            avatar = it.data.avatar
                        )
                    )
                }
            }
        }
    }

    override suspend fun driverInfoWithVehicle(): Result<DriverProfileWithVehicle> {
        driverNetworkDataSource.getDriverInfoWithVehicle().let {
            return when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> {
                    Result.Success(
                        DriverProfileWithVehicle(
                            driverId = it.data.driverId,
                            fullName = it.data.fullName,
                            rating = it.data.rating,
                            color = it.data.vehicleColor.kk,
                            vehicleType = it.data.vehicleType.kk,
                            plateNumber = it.data.plateNumber
                        )
                    )
                }
            }
        }
    }

    override suspend fun getDriverCard(): Result<DriverCard> {
        driverNetworkDataSource.getDriverCard().let {
            return when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> {
                    Result.Success(
                        DriverCard(
                            cardNumber = it.data.cardNumber,
                            nameOnCard = it.data.nameOnCard
                        )
                    )
                }
            }
        }
    }

    override suspend fun getDriverBalance(): Result<DriverBalance> {
        driverNetworkDataSource.getDriverBalance().let {
            return when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> {
                    Result.Success(
                        DriverBalance(
                            balance = it.data.balance ?: 0,
                            dailyBalance = it.data.dayBalance ?: 0
                        )
                    )
                }
            }
        }
    }

    override suspend fun driverLogout(): Result<Boolean> {
        driverNetworkDataSource.driverLogout(NetworkLogoutRequest(localStorage.refresh))
            .let {
                return when (it) {
                    is NetworkResult.Error -> Result.Error(it.message)
                    is NetworkResult.Success -> {
                        localStorage.clear()
                        Result.Success(data = true)
                    }
                }
            }
    }

    override suspend fun driverPhoto(file: File): Result<Boolean> {
        driverNetworkDataSource.driverPhoto(file).let {
            return when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> {
                    Result.Success(data = true)
                }
            }
        }
    }

    override suspend fun getActiveOrders(sendLocationRequest: SendLocationRequest): Result<List<ActiveOfferResponse>> {
        driverNetworkDataSource.getActiveOrders(sendLocationRequest.toDTO2()).let {
            return when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> {
                    val listOfOrders = it.data.map { order -> order.toDomain() }
                    Result.Success(listOfOrders)
                }
            }
        }
    }

    override suspend fun getDriverBalanceInfo(): Result<DriverBalanceInfo> {
        driverNetworkDataSource.getDriverBalanceInfo().let {
            return when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> {
                    Result.Success(
                        DriverBalanceInfo(
                            balance = it.data.balance ?: 0,
                            dayBalance = it.data.dayBalance ?: 0,
                            dayBalanceCard = it.data.dayBalanceCard ?: 0,
                            dayBalanceCash = it.data.dayBalanceCash ?: 0,
                            lastMonthBalance = it.data.lastMonthBalance ?: 0,
                            lastWeekBalance = it.data.lastWeekBalance ?: 0
                        )
                    )
                }
            }
        }
    }

}