package com.aralhub.araltaxi.core.domain.client

import com.aralhub.indrive.core.data.repository.client.ClientRepository
import javax.inject.Inject

class ClientGetRideHistoryDetailsUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(rideId: Int) = repository.getHistoryRideDetails(rideId)
}