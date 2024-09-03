package com.galileosky.gsgpstracker.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.galileosky.gsgpstracker.MainActivity
import com.galileosky.gsgpstracker.R

class LocationService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MyLog", "LS_onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyLog", "LS_onDestroy")
    }

    private fun startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_DEFAULT // Лучше использовать низкую важность
            )
            val nManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(nChannel)
            Log.d("MyLog", "Notification channel created")
        }

        val nIntent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(
            this,
            10,
            nIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Иконка для уведомления
            .setContentTitle(getString(R.string.tracker_running)) // Основной текст
            .setContentText("Your application is running in the background") // Описание уведомления
            .setContentIntent(pIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Низкий приоритет
            .build()

        startForeground(99, notification)
        Log.d("MyLog", "Foreground service started")
    }

    companion object {
        const val CHANNEL_ID = "channel_1"
    }
}
