package com.aralhub.araltaxi.core.domain.ridehistory

import com.aralhub.indrive.core.data.repository.driver.DriverRepository
import javax.inject.Inject

class GetRideHistoryUseCase @Inject constructor(
    private val repository: DriverRepository
) {
    suspend operator fun invoke() = repository.getRideHistory()
}