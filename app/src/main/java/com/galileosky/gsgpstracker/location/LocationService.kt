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
// создаем сервис для работы в трее
class LocationService : Service() {
    // обязательный метод для всех сервисов, он используется для связывания с компонентами, которые запускают этот сервис.
    override fun onBind(intent: Intent?): IBinder? {
        // возвращаем null т.к. он не связан с компонентами другими
        return null
    }
    // вызывается, когда сервис запущен (startService)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // запускает уведомление, которое отображается в трее и уведомляет пользователя о том, что сервис работает
        startNotification()
        // устанавливает флаг, указывающий, что сервис в данный момент активен
        isRunning = true
        // Возвращает START_STICKY, что означает, что сервис должен быть воссоздан системой, если он завершен принудительно (например, при недостатке ресурсов).
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        // указываем что сервис больше не активен
        isRunning = false
    }

    // Этот метод отвечает за создание и запуск уведомления, которое будет отображаться в статусной строке, пока сервис активен.
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
            .setContentText(getString(R.string.background_running)) // Описание уведомления
            .setContentIntent(pIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Низкий приоритет
            .build()

        startForeground(99, notification)
        Log.d("MyLog", "Foreground service started")
    }

    companion object {
        const val CHANNEL_ID = "channel_1"
        var isRunning = false
        var startTime = 0L
    }
}
