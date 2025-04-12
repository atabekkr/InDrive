package com.aralhub.network

import com.aralhub.network.utils.ClientWebSocketEventRideMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface ClientRideNetworkDataSource {

    suspend fun getRide(): Flow<ClientWebSocketEventRideMessage>

    suspend fun close()
}

