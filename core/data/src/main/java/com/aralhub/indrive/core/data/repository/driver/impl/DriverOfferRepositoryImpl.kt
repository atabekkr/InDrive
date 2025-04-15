package com.aralhub.indrive.core.data.repository.driver.impl

import com.aralhub.indrive.core.data.repository.driver.DriverOfferRepository
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.network.DriverNetworkDataSource
import com.aralhub.network.models.NetworkResult
import javax.inject.Inject

class DriverOfferRepositoryImpl @Inject constructor(
    private val driverNetworkDataSource: DriverNetworkDataSource
) : DriverOfferRepository {
    override suspend fun createRide(rideUUID: String, amount: Int): Result<String> {
        driverNetworkDataSource.createOffer(rideUUID, amount).let {
            return when (it) {
                is NetworkResult.Error -> {
                    Result.Error(it.message)
                }
                is NetworkResult.Success -> {
                    Result.Success(it.data.expiresAt)
                }
            }
        }
    }
}