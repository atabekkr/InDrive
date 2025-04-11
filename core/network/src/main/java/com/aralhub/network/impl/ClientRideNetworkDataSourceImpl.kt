package com.aralhub.network.impl

import android.util.Log
import com.aralhub.network.ClientRideNetworkDataSource
import com.aralhub.network.models.ClientWebSocketServerResponse
import com.aralhub.network.models.ClientWebSocketServerResponseUpdate
import com.aralhub.network.models.ride.NetworkRideCompleted
import com.aralhub.network.utils.ClientWebSocketEventRideMessage
import com.aralhub.network.utils.NetworkDriverWaitingClientMessage
import com.aralhub.network.utils.NetworkRideStartedMessage
import com.aralhub.network.utils.NetworkRideStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

class ClientRideNetworkDataSourceImpl(
    private val client: HttpClient
) :
    ClientRideNetworkDataSource {
    private var session: WebSocketSession? = null
    private val _rideStatusFlow = MutableSharedFlow<ClientWebSocketEventRideMessage>(replay = 1)
    private val rideStatusFlow = _rideStatusFlow.asSharedFlow()

    // Coroutine scope for the collection
    private val networkScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var isInitialized = false

    override suspend fun getRide(): SharedFlow<ClientWebSocketEventRideMessage> {
        if (!isInitialized) {
            networkScope.launch {
                try {
                    session = client.webSocketSession { url(RIDE_SOCKET_URL) }
                    isInitialized = true

                    session?.let { webSocketSession ->
                        webSocketSession.incoming
                            .consumeAsFlow()
                            .filterIsInstance<Frame.Text>()
                            .collect { frameText ->
                                val text = frameText.readText()
                                Log.i("WebSocketLogClient", "Received text: $text")
                                try {
                                    val data = Gson().fromJson(
                                        text,
                                        ClientWebSocketServerResponse::class.java
                                    )
                                    val event = when (data.type) {
                                        RIDE_STATUS_UPDATE -> {
                                            when (data.data.status) {
                                                NetworkRideStatus.DRIVER_ON_THE_WAY.status -> {
                                                    val message =
                                                        Gson().fromJson<ClientWebSocketServerResponseUpdate<String>>(
                                                            text,
                                                            object :
                                                                TypeToken<ClientWebSocketServerResponseUpdate<String>>() {}.type
                                                        )
                                                    ClientWebSocketEventRideMessage.DriverOnTheWay(
                                                        message.data.message
                                                    )
                                                }

                                                NetworkRideStatus.DRIVER_WAITING_CLIENT.status -> {
                                                    val message =
                                                        Gson().fromJson<ClientWebSocketServerResponseUpdate<NetworkDriverWaitingClientMessage>>(
                                                            text,
                                                            object :
                                                                TypeToken<ClientWebSocketServerResponseUpdate<NetworkDriverWaitingClientMessage>>() {}.type
                                                        )
                                                    ClientWebSocketEventRideMessage.DriverWaitingClientMessage(
                                                        message.data.message
                                                    )
                                                }

                                                NetworkRideStatus.PAID_WAITING_STARTED.status -> {
                                                    val message =
                                                        Gson().fromJson<ClientWebSocketServerResponseUpdate<String>>(
                                                            text,
                                                            object :
                                                                TypeToken<ClientWebSocketServerResponseUpdate<String>>() {}.type
                                                        )
                                                    ClientWebSocketEventRideMessage.PaidWaitingStarted(
                                                        message.data.message
                                                    )
                                                }

                                                NetworkRideStatus.PAID_WAITING.status -> {
                                                    val message =
                                                        Gson().fromJson<ClientWebSocketServerResponseUpdate<String>>(
                                                            text,
                                                            object :
                                                                TypeToken<ClientWebSocketServerResponseUpdate<String>>() {}.type
                                                        )
                                                    ClientWebSocketEventRideMessage.PaidWaiting(
                                                        message.data.message
                                                    )
                                                }

                                                NetworkRideStatus.RIDE_STARTED.status -> {
                                                    val message =
                                                        Gson().fromJson<ClientWebSocketServerResponseUpdate<NetworkRideStartedMessage>>(
                                                            text,
                                                            object :
                                                                TypeToken<ClientWebSocketServerResponseUpdate<NetworkRideStartedMessage>>() {}.type
                                                        )
                                                    ClientWebSocketEventRideMessage.RideStarted(
                                                        message.data.message
                                                    )
                                                }

                                                NetworkRideStatus.RIDE_COMPLETED.status -> {
                                                    val message =
                                                        Gson().fromJson<ClientWebSocketServerResponseUpdate<NetworkRideCompleted>>(
                                                            text,
                                                            object :
                                                                TypeToken<ClientWebSocketServerResponseUpdate<NetworkRideCompleted>>() {}.type
                                                        )
                                                    ClientWebSocketEventRideMessage.RideCompleted(
                                                        message.data.message.status
                                                    )
                                                }

                                                NetworkRideStatus.CANCELED_BY_DRIVER.status -> {
                                                    val message =
                                                        Gson().fromJson<ClientWebSocketServerResponseUpdate<String>>(
                                                            text,
                                                            object :
                                                                TypeToken<ClientWebSocketServerResponseUpdate<String>>() {}.type
                                                        )
                                                    ClientWebSocketEventRideMessage.CancelledByDriver(
                                                        message.data.message
                                                    )
                                                }

                                                else -> ClientWebSocketEventRideMessage.Unknown("Unknown status: ${data.data.status}")
                                            }
                                        }

                                        else -> ClientWebSocketEventRideMessage.Unknown("Unknown type: ${data.type}")
                                    }
                                    Log.i("WebSocketLogClient", "$event")
                                    _rideStatusFlow.emit(event)
                                } catch (e: Exception) {
                                    _rideStatusFlow.emit(ClientWebSocketEventRideMessage.Unknown("Parsing error: ${e.message}"))
                                }
                            }
                    } ?: run {
                        _rideStatusFlow.emit(ClientWebSocketEventRideMessage.Unknown("Session initialization failed"))
                    }
                } catch (e: Exception) {
                    _rideStatusFlow.emit(ClientWebSocketEventRideMessage.Unknown("WebSocket error: ${e.message}"))
                    Log.e("WebSocketLogClient", "Error initializing WebSocket: ${e.message}", e)
                }
            }
        }

        return rideStatusFlow
    }

    override suspend fun close() {
        session?.close()
        session = null
        isInitialized = false
    }

    companion object {
        private const val RIDE_STATUS_UPDATE = "ride_status_update"
        private const val RIDE_SOCKET_URL = "ws://araltaxi.aralhub.uz/ride/wb"
    }
}