package com.aralhub.araltaxi.core.domain.client

import com.aralhub.indrive.core.data.repository.client.ClientRepository
import javax.inject.Inject

class ClientUpdateAutoTakeUseCase @Inject constructor(private val clientWebSocketRepository: ClientRepository) {
    suspend operator fun invoke(rideId: String, autoTake: Boolean) = clientWebSocketRepository.updateAutoTake(rideId, autoTake)
}