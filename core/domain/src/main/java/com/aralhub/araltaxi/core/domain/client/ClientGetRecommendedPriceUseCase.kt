package com.aralhub.araltaxi.core.domain.client

import com.aralhub.indrive.core.data.model.client.GeoPoint
import com.aralhub.indrive.core.data.repository.client.ClientRepository
import javax.inject.Inject

class ClientGetRecommendedPriceUseCase @Inject constructor(private val repository: ClientRepository) {
    suspend operator fun invoke(points: List<GeoPoint>) = repository.getRecommendedPrice(points)
}