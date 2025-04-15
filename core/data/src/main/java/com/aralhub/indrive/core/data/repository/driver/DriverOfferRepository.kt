package com.aralhub.indrive.core.data.repository.driver

import com.aralhub.indrive.core.data.result.Result

interface DriverOfferRepository {

    suspend fun createRide(rideUUID: String, amount: Int): Result<String>

}