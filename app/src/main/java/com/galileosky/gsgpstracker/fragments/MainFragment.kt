package com.galileosky.gsgpstracker.fragments
import java.util.Locale
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.galileosky.gsgpstracker.MainApp
import com.galileosky.gsgpstracker.MainViewModel
import com.galileosky.gsgpstracker.R
import com.galileosky.gsgpstracker.databinding.FragmentMainBinding
import com.galileosky.gsgpstracker.db.TrackItem
import com.galileosky.gsgpstracker.location.LocationModel
import com.galileosky.gsgpstracker.location.LocationService
import com.galileosky.gsgpstracker.utils.DialogManager
import com.galileosky.gsgpstracker.utils.TimeUtils
import com.galileosky.gsgpstracker.utils.checkPermission
import com.galileosky.gsgpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding

    private var locationModel: LocationModel? = null
    // переменная для отображения первого старта
    private var firstStart: Boolean = true
    // переменная для полилинии
    private var pl: Polyline? = null
    // инициализируем ViewModel класс
    private val model: MainViewModel by activityViewModels{
        MainViewModel.viewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    // переменная для проверки работы сервиса
    private var isServiceRunning = false

    // переменная для таймера
    private var timer: Timer? = null

    // переменная для хранения времени старта маршрута
    private var startTime = 0L

    // переменная для разршений, работающая с массивом строк
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm()
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermissions()
        setOnClicks()
        checkServiceState()
        updateTime()
        regisetrLocReciever() // регистрируем ресивер
        locationUpdates()
        model.tracks.observe(viewLifecycleOwner){
            Log.d("MyLog","List size: ${it.size}")
        }
    }

    private fun setOnClicks() = with(binding) {
        val listener = onClicks()
        fStartStop.setOnClickListener(listener)
    }

    // Слушатель нажатий для любой кнопки в mainfragment
    // Слушатель нажатий для любой кнопки в mainfragment
    private fun onClicks(): View.OnClickListener {
        return View.OnClickListener {
            when (it.id) {
                R.id.fStartStop -> startStopService()
            }
        }
    }

    private fun locationUpdates() = with(binding) {
        model.locationUpdates.observe(viewLifecycleOwner) {
            val distance = "${getString(R.string.map_distance)}: ${String.format(Locale.getDefault(), "%.1f", it.distance)} ${getString(R.string.map_meters)}"
            val velocity = "${getString(R.string.map_speed)}: ${String.format(Locale.getDefault(), "%.1f", 3.6f * it.velocity)} ${getString(R.string.map_kmh)}"
            val averageVelocity = "${getString(R.string.map_a_speed)}: ${getAverageSpeed(it.distance)} ${getString(R.string.map_kmh)}"
            tvDistance.text = distance
            tvSpeed.text = velocity
            tvAverageSpeed.text = averageVelocity
            locationModel = it
            updatePolyline(it.geoPointsList)
        }
    }
    /* Старый код

    private fun locationUpdates() = with(binding){
        // функция будет запускаться когда в locationUpdates
        // передадим новое значение
        model.locationUpdates.observe(viewLifecycleOwner){
            val distance = "${getString(R.string.map_distance)}: ${String.format("%.1f", it.distance)} ${getString(R.string.map_meters)}"
            val velosity = "${getString(R.string.map_speed)}: ${String.format("%.1f", 3.6f * it.velocity)} ${getString(R.string.map_kmh)}"
            val Avelosity = "${getString(R.string.map_a_speed)}: ${getAverageSpeed(it.distance)} ${getString(R.string.map_kmh)}"
            tvDistance.text = distance
            tvSpeed.text = velosity
            tvAverageSpeed.text = Avelosity
        }
    }

    trackItem = TrackItem(
                null,
                getCurrentTime(),
                TimeUtils.getDate(),
                String.format("%.1f", it.distance),
                getAverageSpeed(it.distance),
                geopointsToString(it.geoPointsList)
            )
     */


    // обновляем время на экране карты
    private fun updateTime() {
        model.timeData.observe(viewLifecycleOwner) {
            binding.tvTime.text = it
        }
    }

    // функция для таймера
    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    model.timeData.value = getCurrentTime()
                }
            }
        }, 1000, 1000)
    }

    // функция получения средней скорости
    private fun getAverageSpeed(distance: Float): String {
        return String.format(
            Locale.getDefault(),
            "%.1f",
            3.6f * (distance / ((System.currentTimeMillis() - startTime) / 1000.0f))
        )
    }

    private fun getCurrentTime(): String {
        // добавил код чтобы отображалось слово Таймер/Timer
        return "${getString(R.string.timer)}: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
    }

    private fun geopointsToString(list: List<GeoPoint>): String{
        // 45.898998, -8.727272/45.892398, -8.7274572
        val sb = StringBuilder() // создаем стринг билдер
        list.forEach{
            sb.append("${it.latitude}, ${it.longitude}/")
        }
        Log.d("MyLog","Points: $sb")
        return sb.toString()
    }


    private fun startStopService() {
        // если сервис не запущен, то запускаем функцию
        if (!isServiceRunning) {
            startLocService()
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.fStartStop.setImageResource(R.drawable.ic_play)
            timer?.cancel()
            val track = getTrackItem()
            // сохранение маршрута
            DialogManager.showSaveDialog(requireContext(),
                track,
                object : DialogManager.Listener{
                override fun onClick() {
                    showToast("Track Saved")
                    model.insertTrack(track)
                }
            })
        }
        // принемаем обратное значение
        isServiceRunning = !isServiceRunning
    }

    private fun getTrackItem(): TrackItem{
        return TrackItem(
            null,
            getCurrentTime(),
            TimeUtils.getDate(),
            String.format(Locale.US, "%.1f", locationModel?.distance), // locationModel?.distance?.div(1000) ?:0), безопасное деление на ноль
            getAverageSpeed(locationModel?.distance ?: 0.0f),
            geopointsToString(locationModel?.geoPointsList ?: listOf())
        )
    }

    // Проверяем состояние сервиса и если он работает,
    // то меняем кнопку на стоп
    private fun checkServiceState() {
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning == true) {
            binding.fStartStop.setImageResource(R.drawable.ic_stop)
            startTimer()
        }
    }


    private fun startLocService() {
        // запускаем сервис в зависимости от версии
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }
        // меняем значек сервиса при изменении
        binding.fStartStop.setImageResource(R.drawable.ic_stop)
        LocationService.startTime = System.currentTimeMillis()
        startTimer()
    }

    override fun onResume() {
        super.onResume()
        checkLocPermission()
    }

    private fun settingsOsm() {
        Configuration.getInstance().load(
            requireActivity(),
            requireActivity().getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun initOSM() = with(binding) {
        pl = Polyline() // инициализируем полилинию
        pl?.outlinePaint?.color = Color.BLUE
        map.controller.setZoom(18.0)
        // Создается экземпляр класса GpsMyLocationProvider, который используется для получения данных о местоположении устройства с помощью GPS.
        // Этот провайдер будет передавать данные о местоположении для использования на карте.
        val mLocProvider = GpsMyLocationProvider(requireContext())
        /// Создается объект MyLocationNewOverlay, который отвечает за отображение текущего местоположения пользователя на карте.
        // Он использует mLocProvider для получения данных о местоположении и связывается с картой map.
        val myLocOverLay = MyLocationNewOverlay(mLocProvider, map)
        // Эта строка активирует отображение текущего местоположения пользователя на карте.
        // Если GPS или другие источники местоположения включены и предоставляют данные, на карте будет отображаться маркер, указывающий текущее положение пользователя.
        myLocOverLay.enableMyLocation()
        myLocOverLay.enableFollowLocation()
        myLocOverLay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(myLocOverLay)
            map.overlays.add(pl) // добавили слой полилинии
        }
        //map.controller.animateTo(GeoPoint(58.00171, 56.295304))
        //58.00171° 56.295304°
    }

    // запуск лаунчера для окна разрешения
    private fun registerPermissions() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) { // приравниваем к true, чтобы исключить null
                checkLocationEnabled()
                initOSM()
            } else {
                showToast("Вы не дали разрешения на использование местоположения")
            }
        }
    }

    // основная проверка разрешений
    private fun checkLocPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Build.VERSION.SDK_INT проверяет версию разрешения и если она выше A11(R),то используем функцию...
            checkPermissionForRAndAbove()
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // иначе если версия Q то используем функцию...
            checkPermissionForQ()
        } else {
            // иначе просто все что ниже 10ой версии, то...
            checkPermissionBeforeQ()
        }
    }

    // Для Android 11 (R) и выше
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionForRAndAbove() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                checkLocationEnabled()
                initOSM()
            } else {
                // Объясни пользователю, зачем нужно разрешение на фоновое местоположение, затем запрашивай его
                showBackgroundLocationPermissionRationale()
            }
        } else {
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    // Для Android 10 (Q)
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionForQ() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            checkLocationEnabled()
            initOSM()

        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }

    // Для Android 9 и ниже
    private fun checkPermissionBeforeQ() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            checkLocationEnabled()
            initOSM()

        } else {
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun checkLocationEnabled() {
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled) {
            DialogManager.showLocEnableDialog(
                activity as AppCompatActivity, object : DialogManager.Listener {
                    override fun onClick() {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
            )
        } else {
            showToast("Location enabled")
        }
    }

    // Показываем диалог или уведомление с объяснением
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showBackgroundLocationPermissionRationale() {
        // Затем запустить pLauncher для запроса разрешения на фоновое местоположение
        pLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
    }



    // забираем данные от LocationService
    // поправил эту часть кода из за DEPRECATION getSerializableExtra
    private val receiever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, i: Intent?) {
            // фильтр ресивера для принятия только нужных данных
            if (i?.action == LocationService.LOC_MODEL_INTENT) {
                val locModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    i.getSerializableExtra(LocationService.LOC_MODEL_INTENT, LocationModel::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    i.getSerializableExtra(LocationService.LOC_MODEL_INTENT) as? LocationModel
                }
                locModel?.let {
                    model.locationUpdates.value = locModel
                }
            }
        }

    }

    // Старый код
    /*
    private val receiever = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, i: Intent?) {
        if (i?.action == LocationService.LOC_MODEL_INTENT) {
            val locModel = i.getSerializableExtra(LocationService.LOC_MODEL_INTENT) as LocationModel
            model.locationUpdates.value = locModel
        }
    }
}
     */

    // регистрируем ресивер
    private fun regisetrLocReciever(){
        // создаем внутри его фильтр
        val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .registerReceiver(receiever, locFilter)
    }

    // функция добавления точек polyline
    private fun addPoint(list: List<GeoPoint>){
        pl?.addPoint(list[list.size - 1]) // добавляем последнюю точку
    }

    // функция заполнения точек polyline должна запуститься
    // только один раз
    private fun fillPolyLine(list: List<GeoPoint>){
        list.forEach{
            pl?.addPoint(it)
        }
    }

    // тут уду решать что делать - или восстанавливать историю полилини
    // или сразу строить на карте
    private fun updatePolyline(list: List<GeoPoint>){
        if (list.size > 1 && firstStart){
        // тогда выгружаем точки
            fillPolyLine(list)
            firstStart = false
        } else {
            addPoint(list)
        }


    }

    override fun onDetach() {
        super.onDetach()
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .unregisterReceiver(receiever)
    }


    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}

