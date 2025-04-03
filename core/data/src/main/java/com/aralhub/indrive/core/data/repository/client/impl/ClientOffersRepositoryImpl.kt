package com.aralhub.indrive.core.data.repository.client.impl

import com.aralhub.indrive.core.data.model.offer.Offer
import com.aralhub.indrive.core.data.model.ride.toDomain
import com.aralhub.indrive.core.data.repository.client.ClientOffersRepository
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.indrive.core.data.util.ClientWebSocketEvent
import com.aralhub.network.ClientOffersNetworkDataSource
import com.aralhub.network.WebSocketClientOffersNetworkDataSource
import com.aralhub.network.models.NetworkResult.Error
import com.aralhub.network.models.NetworkResult.Success
import com.aralhub.network.utils.ClientWebSocketEventNetwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClientOffersRepositoryImpl @Inject constructor(
    private val clientOffersNetworkDataSource: ClientOffersNetworkDataSource,
    private val webSocketClientOffersNetworkDataSource: WebSocketClientOffersNetworkDataSource
) :
    ClientOffersRepository {
    override suspend fun acceptOffer(offerId: String): Result<Boolean> {
        return clientOffersNetworkDataSource.acceptOffer(offerId).let {
            when (it) {
                is Success -> Result.Success(true)
                is Error -> Result.Error(it.message)
            }
        }
    }

    override suspend fun rejectOffer(offerId: String): Result<Boolean> {
        return clientOffersNetworkDataSource.rejectOffer(offerId).let {
            when (it) {
                is Success -> Result.Success(true)
                is Error -> Result.Error(it.message)
            }
        }
    }

    override suspend fun getOffers(): Flow<ClientWebSocketEvent> {
        return webSocketClientOffersNetworkDataSource.getOffers().map {
            when (it) {
                is ClientWebSocketEventNetwork.DriverOffer -> ClientWebSocketEvent.DriverOffer(
                    Offer(
                        offerId = it.offer.offerId,
                        rideUuid = it.offer.rideUuid,
                        driver = it.offer.driver,
                        amount = it.offer.amount,
                        createdAt = it.offer.createdAt,
                        expiresAt = it.offer.expiresAt,
                    )
                )

                is ClientWebSocketEventNetwork.Unknown -> ClientWebSocketEvent.Unknown(it.error)
                is ClientWebSocketEventNetwork.OfferAccepted -> ClientWebSocketEvent.OfferAccepted(it.ride.toDomain())
            }
        }
    }

    override suspend fun close() {
        webSocketClientOffersNetworkDataSource.close()
    }
}