package com.aralhub.network.impl

import android.util.Log
import com.aralhub.network.WebSocketClientOffersNetworkDataSource
import com.aralhub.network.models.ClientWebSocketServerResponse
import com.aralhub.network.models.ClientWebSocketServerResponseOffers
import com.aralhub.network.models.offer.NetworkOffer
import com.aralhub.network.models.ride.NetworkRideActive
import com.aralhub.network.utils.ClientWebSocketEventNetwork
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class WebSocketClientOffersNetworkDataSourceImpl(private val client: HttpClient): WebSocketClientOffersNetworkDataSource {
    private var session: WebSocketSession? = null

    override suspend fun getOffers(): Flow<ClientWebSocketEventNetwork> {
        session = client.webSocketSession { url("ws://araltaxi.aralhub.uz/websocket/wb/passenger") }

        return session?.let { webSocketSession ->
            Log.i("WebSocketLogClient", "Session is not null")
            webSocketSession.incoming
                .consumeAsFlow()
                .onEach { frame ->
                    Log.i("WebSocketLogClient", "Received a frame of type: ${frame::class.java.simpleName}")
                }
                .filterIsInstance<Frame.Text>()
                .onEach { frame ->
                    Log.i("WebSocketLogClient", "Processing Text frame")
                }
                .let { textFramesFlow ->
                    flow {
                        try {
                            textFramesFlow.collect { frameText ->
                                val text = frameText.readText()
                                Log.i("WebSocketLogClient", "Received raw frame: $text")

                                // Rest of your processing logic
                                try {
                                    val data = Gson().fromJson(text, ClientWebSocketServerResponse::class.java)
                                    Log.i("WebSocketLogClient", "Parsed type: ${data.type}")

                                    val event = when (data.type) {
                                        DRIVER_OFFER -> {
                                            val driverOffer = Gson().fromJson<ClientWebSocketServerResponseOffers<NetworkOffer>>(
                                                text,
                                                object : TypeToken<ClientWebSocketServerResponseOffers<NetworkOffer>>() {}.type
                                            )
                                            ClientWebSocketEventNetwork.DriverOffer(driverOffer.data)
                                        }
                                        OFFER_ACCEPTED  -> {
                                            val ride = Gson().fromJson<ClientWebSocketServerResponseOffers<NetworkRideActive>>(
                                                text,
                                                object: TypeToken<ClientWebSocketServerResponseOffers<NetworkRideActive>>() {}.type
                                            )
                                            ClientWebSocketEventNetwork.OfferAccepted(ride.data)
                                        }
                                        else -> ClientWebSocketEventNetwork.Unknown("Unknown type: ${data.type}")
                                    }

                                    emit(event)
                                } catch (e: Exception) {
                                    Log.e("WebSocketLogClient", "Error parsing frame: ${e.message}", e)
                                    emit(ClientWebSocketEventNetwork.Unknown("Parsing error: ${e.message}"))
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("WebSocketLogClient", "WebSocket error: ${e.message}", e)
                            emit(ClientWebSocketEventNetwork.Unknown("WebSocket error: ${e.message}"))
                        }
                    }
                }
        } ?: flow {
            Log.i("WebSocketLogClient", "Session is null")
            emit(ClientWebSocketEventNetwork.Unknown("Session initialization failed"))
        }
    }

    override suspend fun close() {
        session?.close()
        session = null
    }

    companion object {
        private const val DRIVER_OFFER = "driver_offer"
        private const val OFFER_ACCEPTED = "ride_accepted"
    }
}