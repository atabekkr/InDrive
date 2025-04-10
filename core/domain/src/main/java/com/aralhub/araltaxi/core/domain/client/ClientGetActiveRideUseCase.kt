package com.aralhub.araltaxi.core.domain.client

import com.aralhub.indrive.core.data.repository.client.ClientRepository
import javax.inject.Inject

class ClientGetActiveRideUseCase @Inject constructor(private val webSocketRepository: ClientRepository) {
    suspend  operator fun invoke() = webSocketRepository.getActiveRideByPassenger()
}