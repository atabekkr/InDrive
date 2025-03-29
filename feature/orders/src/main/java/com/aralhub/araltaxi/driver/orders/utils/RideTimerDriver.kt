package com.aralhub.araltaxi.driver.orders.utils

import android.graphics.Color
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RideTimerDriver {
    private var job: Job? = null
    private var isRideAccepted = false
    private var isPaidWaiting = false

    fun startTimer(endFreeTime: Long, textView: TextView, waitingStatusTitle: (String) -> Unit) {
        job?.cancel() // Cancel any existing timer
        job = CoroutineScope(Dispatchers.Main).launch {

            while (System.currentTimeMillis() < endFreeTime) {
                val timeLeftMillis = endFreeTime - System.currentTimeMillis()
                if (timeLeftMillis >= 0) {
                    textView.text = formatTime(timeLeftMillis)
                }
                delay(1000) // Update every second
            }


            // Платное ожидание
            if (!isRideAccepted) {
                waitingStatusTitle("Tólemli kútiw waqtı baslandı")
                isPaidWaiting = true
                while (!isRideAccepted) {
                    val currentTimeMillis = System.currentTimeMillis()
                    val elapsedPaidMillis = currentTimeMillis - endFreeTime

                    val secondsElapsed = (elapsedPaidMillis / 1000).toInt()
                    val minutes = secondsElapsed / 60
                    val seconds = secondsElapsed % 60

                    textView.text = formatTime(elapsedPaidMillis)
                    textView.setTextColor(Color.RED) // Цвет для платного ожидания
                    println("Paid time: $minutes:${seconds.toString().padStart(2, '0')}")

                    delay(1000) // Update every second
                }
            }
        }
    }

    fun stopTimer() {
        job?.cancel()
        isRideAccepted = false
    }
}