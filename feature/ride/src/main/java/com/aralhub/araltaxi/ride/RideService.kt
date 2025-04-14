package com.aralhub.araltaxi.ride

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.aralhub.araltaxi.client.ride.R
import com.aralhub.araltaxi.core.domain.client.GetClientRideStatusUseCase
import com.aralhub.indrive.core.data.model.ride.RideStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RideService : Service() {
    @Inject
    lateinit var useCase: GetClientRideStatusUseCase
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var notificationManager: NotificationManager
    private var isServiceStarted = false

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()

        // Start service with initial notification
        val initialNotification = buildNotification("Preparing your ride...")
        startForegroundService(initialNotification)

        // Now observe states for updates
        observeStates()
    }

    private fun startForegroundService(notification: Notification) {
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            UNSPECIFIED_FOREGROUND_SERVICE_TYPE
        }

        ServiceCompat.startForeground(this, ID, notification, foregroundServiceType)
        isServiceStarted = true
    }

    private fun observeStates() {
        scope.launch {
            useCase().collect { rideStatus ->
                val (message, shouldPlaySound) = when (rideStatus) {
                    is RideStatus.DriverOnTheWay -> Pair(rideStatus.message, false)
                    is RideStatus.DriverWaitingClient -> Pair(
                        rideStatus.message,
                        true
                    ) // Play sound when driver is waiting
                    is RideStatus.PaidWaiting -> Pair(rideStatus.message, false)
                    is RideStatus.PaidWaitingStarted -> Pair(
                        rideStatus.message,
                        true
                    ) // Play sound for paid waiting
                    is RideStatus.RideCompleted -> {
                        Pair(
                            rideStatus.message,
                            true
                        )
                    } // Play sound for ride completion
                    is RideStatus.RideStarted -> Pair(rideStatus.message, false)
                    is RideStatus.Unknown -> Pair(rideStatus.error, false)
                    is RideStatus.CanceledByDriver -> Pair(rideStatus.message, false)
                }

                Log.i("RideService", "Status update: $message")

                if (!isServiceStarted) {
                    // First time - start the foreground service
                    val notification = buildNotification(message, shouldPlaySound)
                    startForegroundService(notification)
                } else {
                    // Update the existing notification
                    updateNotification(message, shouldPlaySound)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("RideService", "onStartCommand")
        return START_STICKY
    }

    private fun updateNotification(text: String, playSound: Boolean = false) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ride Status")
            .setContentText(text)
            .setSmallIcon(R.drawable.notification_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)

        // Add sound only when playSound is true
        if (playSound) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(soundUri)
        }

        val notification = builder.build()
        notificationManager.notify(ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            // Configure channel to allow sound
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            // Allow vibration
            enableVibration(true)
            // Allow lights
            enableLights(true)
        }

        notificationManager.createNotificationChannel(channel)

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        const val CHANNEL_ID = "ride_status_channel"
        const val CHANNEL_NAME = "Ride Status Updates"
        const val UNSPECIFIED_FOREGROUND_SERVICE_TYPE = 0
        const val ID = 100
    }

    private fun buildNotification(text: String, playSound: Boolean = false): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ride Status")
            .setContentText(text)
            .setSmallIcon(R.drawable.notification_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)

        if (playSound) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(soundUri)
        }

        return builder.build()
    }
}