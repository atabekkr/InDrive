package com.aralhub.araltaxi.core.domain.ridehistory

import com.aralhub.indrive.core.data.repository.client.ClientRepository
import javax.inject.Inject

class GetClientRideHistoryUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke() = repository.getRideHistory()
}