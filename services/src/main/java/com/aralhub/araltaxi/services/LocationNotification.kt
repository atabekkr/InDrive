package com.aralhub.araltaxi.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.aralhub.araltaxi.services.LocationService.Companion.CHANNEL_ID
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocationNotification @Inject constructor(
    @ApplicationContext private val context: Context,
    private val manager: NotificationManager
) {

    private val builder: NotificationCompat.Builder by lazy {

        val intent = Intent("", Uri.parse(""))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.tracking_location))
//            .setContentText(context.getString(R.string.your_location, "-", "-"))
//            .setSmallIcon(com.aralhub.ui.R.drawable.ic_user)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
    }

    fun createChannel() {
        NotificationChannel(
            CHANNEL_ID,
            "Smth",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Location tracking channel"
            setShowBadge(false)
            manager.createNotificationChannel(this)
        }
    }

    fun createNotification(): Notification {
        return builder.build()
    }
}