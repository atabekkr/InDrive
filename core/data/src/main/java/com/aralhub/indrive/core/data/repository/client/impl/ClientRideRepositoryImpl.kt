package com.aralhub.indrive.core.data.repository.client.impl

import android.util.Log
import com.aralhub.indrive.core.data.model.ride.RideStatus
import com.aralhub.indrive.core.data.repository.client.ClientRideRepository
import com.aralhub.network.ClientRideNetworkDataSource
import com.aralhub.network.utils.ClientWebSocketEventRideMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ClientRideRepositoryImpl @Inject constructor(private val clientRideNetworkDataSource: ClientRideNetworkDataSource): ClientRideRepository {
    private val _rideStatusFlow = MutableSharedFlow<RideStatus>(replay = 1)
    private val rideStatusFlow = _rideStatusFlow.asSharedFlow()

    // Coroutine scope for the collection
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override suspend fun getRideStatus(): Flow<RideStatus> = callbackFlow {
        val job = repositoryScope.launch {
            clientRideNetworkDataSource.getRide().collect { event ->
                val status = when(event) {
                    is ClientWebSocketEventRideMessage.DriverOnTheWay -> RideStatus.DriverOnTheWay(event.message)
                    is ClientWebSocketEventRideMessage.DriverWaitingClientMessage -> RideStatus.DriverWaitingClient(
                        event.message.waitPricePerMinute,
                        event.message.startFreeTime,
                        event.message.endFreeTime,
                        event.message.message
                    )
                    is ClientWebSocketEventRideMessage.PaidWaiting -> RideStatus.PaidWaiting(event.message)
                    is ClientWebSocketEventRideMessage.PaidWaitingStarted -> RideStatus.PaidWaitingStarted(event.message)
                    is ClientWebSocketEventRideMessage.RideCompleted -> {
                        clientRideNetworkDataSource.close()
                        RideStatus.RideCompleted(event.message).also {
                            trySend(it)
                            close() // Закрываем поток
                            return@collect
                        }
                    }
                    is ClientWebSocketEventRideMessage.RideStarted -> RideStatus.RideStarted(event.message.message)
                    is ClientWebSocketEventRideMessage.Unknown -> RideStatus.Unknown(event.error)
                    is ClientWebSocketEventRideMessage.CancelledByDriver -> RideStatus.CanceledByDriver(event.message)
                }

                trySend(status).isSuccess
            }
        }

        awaitClose {
            job.cancel()
            repositoryScope.launch {
                clientRideNetworkDataSource.close()
            }
        }

    }

    override suspend fun close() {
        clientRideNetworkDataSource.close()
    }

}