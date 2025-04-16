package com.aralhub.indrive.core.data.repository.driver.impl

import androidx.paging.PagingData
import androidx.paging.map
import com.aralhub.indrive.core.data.model.cancel.DriverCancelCause
import com.aralhub.indrive.core.data.model.cancel.toDomain
import com.aralhub.indrive.core.data.model.driver.RideCompleted
import com.aralhub.indrive.core.data.model.driver.toDomain
import com.aralhub.indrive.core.data.model.offer.ActiveRideByDriverResponse
import com.aralhub.indrive.core.data.model.offer.toActiveOfferDomain
import com.aralhub.indrive.core.data.model.ride.RideHistory
import com.aralhub.indrive.core.data.model.ride.toDomain
import com.aralhub.indrive.core.data.repository.driver.DriverRepository
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.network.DriverNetworkDataSource
import com.aralhub.network.models.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DriverRepositoryImpl @Inject constructor(
    private val driverNetworkDataSource: DriverNetworkDataSource
) : DriverRepository {
    override suspend fun getActiveRide(): Result<ActiveRideByDriverResponse?> {
        driverNetworkDataSource.getActiveRide().let {
            return when (it) {
                is NetworkResult.Error -> {
                    Result.Error(it.message)
                }

                is NetworkResult.Success -> {
                    val response = ActiveRideByDriverResponse(
                        it.data?.toActiveOfferDomain(),
                        it.data?.status
                    )
                    Result.Success(response)
                }
            }
        }
    }

    override suspend fun cancelRide(rideId: Int, cancelCauseId: Int): Result<Boolean> {
        driverNetworkDataSource.cancelRide(rideId, cancelCauseId).let {
            return when (it) {
                is NetworkResult.Error -> {
                    Result.Error(it.message)
                }

                is NetworkResult.Success -> {
                    Result.Success(it.data)
                }
            }
        }
    }

    override suspend fun updateRideStatus(rideId: Int, status: String): Result<RideCompleted?> {
        driverNetworkDataSource.updateRideStatus(rideId, status).let {
            return when (it) {
                is NetworkResult.Error -> {
                    Result.Error(it.message)
                }

                is NetworkResult.Success -> {
                    Result.Success(it.data?.toDomain())
                }
            }
        }
    }

    override suspend fun getCancelCauses(): Result<List<DriverCancelCause>> {
        driverNetworkDataSource.getCancelCauses().let {
            return when (it) {
                is NetworkResult.Error -> {
                    Result.Error(it.message)
                }

                is NetworkResult.Success -> {
                    Result.Success(it.data.toDomain())
                }
            }
        }
    }

    override suspend fun getWaitAmount(rideId: Int): Result<Double> {
        return driverNetworkDataSource.getWaitTime(rideId).let {
            when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(it.data.paidWaitingTime)
            }
        }
    }

    override suspend fun getRideHistory(): Flow<PagingData<RideHistory>> {
        return driverNetworkDataSource.getRideHistory()
            .map { pagingData ->
                pagingData.map { it.toDomain() }
            }
    }


    override suspend fun getHistoryRideDetails(rideId: Int): Result<RideHistory> {
        return driverNetworkDataSource.getHistoryRideDetails(rideId).let {
            when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(it.data.toDomain())
            }
        }
    }
}