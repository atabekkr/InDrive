package com.aralhub.araltaxi.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.PermissionChecker
import com.aralhub.araltaxi.core.common.sharedpreference.DriverSharedPreference
import com.aralhub.indrive.core.data.model.offer.toDomain
import com.aralhub.indrive.core.data.util.WebSocketEvent
import com.aralhub.indrive.core.data.util.closeActiveOrdersWebSocket
import com.aralhub.indrive.core.data.util.webSocketEvent
import com.aralhub.network.models.WebSocketServerResponse
import com.aralhub.network.models.driver.NetworkActiveRideByDriverResponse
import com.aralhub.network.models.location.NetworkSendLocationRequest
import com.aralhub.network.models.offer.NetworkActiveOfferResponse
import com.aralhub.network.models.offer.NetworkOfferCancelResponse
import com.aralhub.network.models.offer.NetworkOfferRejectedResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var client: HttpClient

    private var session: WebSocketSession? = null
    private var rideStatusSession: WebSocketSession? = null

    @Inject
    lateinit var notification: LocationNotification

    @Inject
    lateinit var driverSharedPreference: DriverSharedPreference

    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    private var locationManager: LocationManager? = null
    private var notificationManager: NotificationManager? = null
    private val locationListener = LocationListener { location ->
        val latitude = location.latitude
        val longitude = location.longitude
        scope.launch {
            val distance = driverSharedPreference.distance
            sendLocation(
                NetworkSendLocationRequest(
                    latitude = latitude,
                    longitude = longitude,
                    distance = distance
                )
            )
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        initObservers()
        initSoundPool()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            INTERVAL,
            SMALLEST_DISTANCE,
            locationListener
        )
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        soundId = soundPool.load(this, R.raw.sound_new_order, 1)
    }

    private fun playNotificationSound() {
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground()
        return super.onStartCommand(intent, flags, startId)

    }

    private fun startForeground() {
        val locationPermission =
            PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (locationPermission != PermissionChecker.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        notification.createChannel()

        scope.launch { getActiveOrders() }

        ServiceCompat.startForeground(
            /* service = */ this,
            /* id = */ ID, // Cannot be 0
            /* notification = */ notification.createNotification(),
            /* foregroundServiceType = */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                0
            }
        )
    }

    private suspend fun getActiveOrders() {

        closeStartedRideStatusSocket()

            Log.d("WebSocketLog", "While")
            try {
                session = client.webSocketSession {
                    url("ws://araltaxi.aralhub.uz/websocket/wb/driver")
                }
                if (session?.isActive == true) {
                    Log.d("WebSocketLog", "Connected")

//                    keepConnectionAlive()

                    for (frame in session!!.incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val jsonString = frame.readText()
                                Log.d("WebSocketLog", jsonString)
                                val event = try {
                                    val baseResponse =
                                        Gson().fromJson(jsonString, WebSocketServerResponse::class.java)

                                    when (baseResponse.type) {
                                        RIDE_CANCELED -> {
                                            val rideCancelData =
                                                Gson().fromJson<WebSocketServerResponse<NetworkOfferCancelResponse>>(
                                                    jsonString,
                                                    object :
                                                        TypeToken<WebSocketServerResponse<NetworkOfferCancelResponse>>() {}.type
                                                )
                                            WebSocketEvent.RideCancel(rideCancelData.data.rideId)
                                        }

                                        OFFER_REJECTED -> {
                                            val rideCancelData =
                                                Gson().fromJson<WebSocketServerResponse<NetworkOfferRejectedResponse>>(
                                                    jsonString,
                                                    object :
                                                        TypeToken<WebSocketServerResponse<NetworkOfferRejectedResponse>>() {}.type
                                                )
                                            WebSocketEvent.OfferReject(rideCancelData.data.rideUUID)
                                        }

                                        NEW_RIDE_REQUEST -> {
                                            val offerData =
                                                Gson().fromJson<WebSocketServerResponse<NetworkActiveOfferResponse>>(
                                                    jsonString,
                                                    object :
                                                        TypeToken<WebSocketServerResponse<NetworkActiveOfferResponse>>() {}.type
                                                )
                                            playNotificationSound()
                                            WebSocketEvent.ActiveOffer(
                                                offerData.toDomain()
                                            )
                                        }

                                        OFFER_ACCEPTED -> {
                                            val offerData =
                                                Gson().fromJson<WebSocketServerResponse<NetworkActiveRideByDriverResponse>>(
                                                    jsonString,
                                                    object :
                                                        TypeToken<WebSocketServerResponse<NetworkActiveRideByDriverResponse>>() {}.type
                                                )
                                            WebSocketEvent.OfferAccepted(
                                                offerData.data.toDomain()
                                            )
                                        }

                                        else -> {
                                            WebSocketEvent.Unknown(jsonString)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("WebSocketLog", "Parsing error: ${e.message}")
                                    WebSocketEvent.Unknown(jsonString)
                                }
                                webSocketEvent.emit(event)
                            }

                            is Frame.Ping ->{
                                session?.send(Frame.Pong(frame.data))
                            }

                            else -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                //handle web socket error here
            }

    }

    private suspend fun sendLocation(data: NetworkSendLocationRequest) {
        session?.outgoing?.send(
            Frame.Text(Gson().toJson(data))
        )
        Log.d("WebSocketLog", "location sent")
    }

    private suspend fun close() {
        session?.close(
            CloseReason(
                CloseReason.Codes.NORMAL, "Closing Session"
            )
        )
        session = null
        Log.d("WebSocketLog", "Session Closed")
    }

    private suspend fun keepConnectionAlive() {
        while (true) {
            delay(30_000) // Каждые 30 секунд
            try {
                session?.send(Frame.Ping(ByteArray(0))) // Пинг для поддержания соединения
                Log.d("WebSocketLog", "Ping sent")
            } catch (e: Exception) {
                Log.e("WebSocketLog", "Ping failed: ${e.message}")
            }
        }
    }


    private suspend fun getStartedRideStatus() {
        try {
            rideStatusSession = client.webSocketSession {
                url("ws://araltaxi.aralhub.uz/ride/wb")
            }
            if (rideStatusSession?.isActive == true) {
                Log.d("WebSocketLog", "Started ride web socket Connected")
                for (frame in rideStatusSession!!.incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val jsonString = frame.readText()
                            Log.d("WebSocketLog", jsonString)
                            val event = try {
                                val baseResponse =
                                    Gson().fromJson(jsonString, WebSocketServerResponse::class.java)

                                when (baseResponse.type) {
                                    RIDE_CANCELED_BY_PASSENGER -> {
                                        WebSocketEvent.RideCancelledByPassenger
                                    }

                                    else -> {
                                        WebSocketEvent.Unknown(jsonString)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("WebSocketLog", "Parsing error: ${e.message}")
                                WebSocketEvent.Unknown(jsonString)
                            }
                            webSocketEvent.emit(event)
                        }

                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //handle web socket error here
        }
    }

    private suspend fun closeStartedRideStatusSocket() {
        rideStatusSession?.close(
            CloseReason(
                CloseReason.Codes.NORMAL, "Closing RideStatus Session"
            )
        )
        rideStatusSession = null
        Log.d("WebSocketLog", "RideStatus Session Closed")
    }

    private fun initObservers() {
        closeActiveOrdersWebSocket.onEach {
            close()
            getStartedRideStatus()
        }.launchIn(scope)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(locationListener)
    }

    companion object {
        const val INTERVAL: Long = 1 * 1000
        const val SMALLEST_DISTANCE: Float = 1f
        const val CHANNEL_ID = "location_channel"
        const val ID = 100

        const val NEW_RIDE_REQUEST = "new_ride_request"
        const val DRIVER_OFFER = "driver_offer"
        const val OFFER_ACCEPTED = "offer_accepted"
        const val OFFER_REJECTED = "offer_rejected"
        const val RIDE_STATUS_UPDATE = "ride_status_update"
        const val LOCATION_UPDATE = "location_update"
        const val RIDE_ACCEPTED = "ride_accepted"
        const val RIDE_CANCELED = "ride_cancel"
        const val RIDE_CANCELED_BY_PASSENGER = "cancelled_by_passenger"
        const val RIDE_DELETED = "ride_deleted"
        const val RIDE_AMOUNT_UPDATED = "ride_amount_updated"
        const val ERROR = "error"
    }
}