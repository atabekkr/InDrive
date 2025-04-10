package com.aralhub.araltaxi.core.domain.client

import com.aralhub.indrive.core.data.model.client.ClientRideRequest
import com.aralhub.indrive.core.data.repository.client.ClientAuthRepository
import com.aralhub.indrive.core.data.repository.client.ClientRepository
import javax.inject.Inject

class ClientCreateRideUseCase @Inject constructor(private val repository: ClientRepository,
                                                  private val clientAuthRepository: ClientAuthRepository) {

    suspend operator fun invoke(clientRideRequest: ClientRideRequest) = repository.createRide(clientRideRequest)
}