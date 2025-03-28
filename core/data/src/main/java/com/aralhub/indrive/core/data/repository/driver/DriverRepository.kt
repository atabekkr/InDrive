package com.aralhub.indrive.core.data.repository.driver

import com.aralhub.indrive.core.data.model.cancel.DriverCancelCause
import com.aralhub.indrive.core.data.model.driver.RideCompleted
import com.aralhub.indrive.core.data.model.offer.ActiveRideByDriverResponse
import com.aralhub.indrive.core.data.result.Result

interface DriverRepository {

    suspend fun getActiveRide(): Result<ActiveRideByDriverResponse?>
    suspend fun cancelRide(rideId: Int, cancelCauseId: Int): Result<Boolean>
    suspend fun updateRideStatus(rideId: Int, status: String): Result<RideCompleted?>
    suspend fun getCancelCauses(): Result<List<DriverCancelCause>>
    suspend fun getWaitAmount(rideId: Int): Result<Double>
}