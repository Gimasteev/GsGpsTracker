package com.galileosky.gsgpstracker.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
//import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
//import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.galileosky.gsgpstracker.MainActivity
//import com.galileosky.gsgpstracker.Manifest
import com.galileosky.gsgpstracker.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import org.osmdroid.util.GeoPoint


// создаем сервис для работы в трее
class LocationService : Service() {
    // переменная для передаччи данных в LocationModel
    private lateinit var geoPointsList: ArrayList<GeoPoint>
    // переменная, куда запишем дистанцию
    private var distance = 0.0f
    // переменная для хранения пердыдущего значения для опредления дистанции
    private var lastLocation: Location? = null
    // создаем переменную для провайдера
    private lateinit var locProvider: FusedLocationProviderClient
    // объект, представляющий запрос на обновление местоположения. Включает такие параметры,
    // как интервал обновления и приоритет
    private lateinit var locRequest: LocationRequest
    // булеан переменная для скорости - тест эмулятора
    // поставь true если тестишь при скорости 0 km/h (на эмуляторе)
    private var isDebug = false
    

    // обязательный метод для всех сервисов, он используется для связывания с компонентами, которые запускают этот сервис.
    override fun onBind(intent: Intent?): IBinder? {
        // возвращаем null т.к. он не связан с компонентами другими
        return null
    }
    // вызывается, когда сервис запущен (startService)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // запускает уведомление, которое отображается в трее и уведомляет пользователя о том, что сервис работает
        startNotification()
        // получаем значения местоположения
        startLocationUpdates()
        // устанавливает флаг, указывающий, что сервис в данный момент активен
        isRunning = true
        // Возвращает START_STICKY, что означает, что сервис должен быть воссоздан системой, если он завершен принудительно (например, при недостатке ресурсов).
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        // создали пустой массив
        geoPointsList = ArrayList()
        initLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        // указываем что сервис больше не активен
        isRunning = false
        locProvider.removeLocationUpdates(locCallBack)
    }

    private val locCallBack = object : LocationCallback() {
        override fun onLocationResult(lResult: LocationResult) {
            super.onLocationResult(lResult)
            // получаем текущую позицию
            val currentLocation = lResult.lastLocation
            // проверям что получили местоположение
            if (lastLocation != null && currentLocation != null) {
                // проверям скорость чтобы не добавлять левые метры при остановке
                if (currentLocation.speed > 0.4 || isDebug) {
                    // добавляем к дистанции расстояние
                    distance += lastLocation?.distanceTo(currentLocation)!!
                    // добавляем геоточки в список
                    geoPointsList.add(GeoPoint(currentLocation.latitude, currentLocation.longitude))
                }
                val locModel = LocationModel(
                    currentLocation.speed,
                    distance,
                    geoPointsList
                )
                // как только геопоинт >2 отправляем параметры местоположения
                sendLocData(locModel)
            }
            lastLocation = currentLocation
        }
    }

    // отправляем данные в mainFragment через интент
    private fun sendLocData(locModel: LocationModel){
        // создаем интент и даем ему название
        val i = Intent(LOC_MODEL_INTENT)
        // помещаем наш data класс для отправки
        i.putExtra(LOC_MODEL_INTENT, locModel)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i)
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

    private fun initLocation(){
        // подключчаем интервалы из настроек
        val updateInterval = PreferenceManager.getDefaultSharedPreferences(
            this
        ).getString("update_time_key", "3000")?.toLong() ?: 3000 // забираем то что под ключем. По умолчанию 3 сек
        locRequest = LocationRequest.create()
        // интервал передачи сообщений
        locRequest.interval = updateInterval // примерное ограничение
        locRequest.fastestInterval = updateInterval // жесткое ограничение в 5 сек
        locRequest.priority = PRIORITY_HIGH_ACCURACY
        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)
        Log.d("MyLog", "Interval = $updateInterval")
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        locProvider.requestLocationUpdates(
            locRequest,
            locCallBack,
            Looper.myLooper()
        )
    }


    companion object {
        const val LOC_MODEL_INTENT = "loc_intent"
        const val CHANNEL_ID = "channel_1"
        var isRunning = false
        var startTime = 0L
    }
}
