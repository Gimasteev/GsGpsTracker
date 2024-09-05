package com.galileosky.gsgpstracker.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.galileosky.gsgpstracker.R
import com.galileosky.gsgpstracker.databinding.FragmentMainBinding
import com.galileosky.gsgpstracker.location.LocationService
import com.galileosky.gsgpstracker.utils.DialogManager
import com.galileosky.gsgpstracker.utils.TimeUtils
import com.galileosky.gsgpstracker.utils.checkPermission
import com.galileosky.gsgpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    // переменная для проверки работы сервиса
    private var isServiceRunning = false
    // переменная для таймера
    private var timer: Timer? = null
    // переменная для хранения времени старта маршрута
    private var startTime = 0L
    // переменная
    private val timeData = MutableLiveData<String>()
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
    }

    private fun setOnClicks() = with(binding){
        val listener = onClicks()
        fStartStop.setOnClickListener(listener)
    }

    // Слушатель нажатий для любой кнопки в mainfragment
    private fun onClicks(): View.OnClickListener{
        return View.OnClickListener {
            when(it.id){
                R.id.fStartStop -> startStopService()
            }
        }
    }

    private fun updateTime(){
        timeData.observe(viewLifecycleOwner){
            binding.tvTime.text = it
        }
    }

    // функция для таймера
    private fun startTimer(){
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime
        timer?.schedule(object: TimerTask(){
            override fun run() {
                activity?.runOnUiThread {
                    timeData.value = getCurrentTime()
                }
            }
        }, 1000, 1000)
    }

    private fun getCurrentTime(): String{
        // добавил код чтобы отображалось слово Таймер/Timer
        return "${getString(R.string.timer)}: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
    }



    private fun startStopService(){
        // если сервис не запущен, то запускаем функцию
        if (!isServiceRunning){
            startLocService()
        } else {
            activity?.stopService(Intent(activity,LocationService::class.java))
            binding.fStartStop.setImageResource(R.drawable.ic_play)
            timer?.cancel()
        }
        // принемаем обратное значение
        isServiceRunning = !isServiceRunning
    }

    // Проверяем состояние сервиса и если он работает,
    // то меняем кнопку на стоп
    private fun checkServiceState(){
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning == true){
            binding.fStartStop.setImageResource(R.drawable.ic_stop)
            startTimer()
        }
    }


    private fun startLocService(){
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

    private fun initOSM() = with(binding){
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
        }
        //map.controller.animateTo(GeoPoint(58.00171, 56.295304))
        //58.00171° 56.295304°
    }

    // запуск лаунчера для окна разрешения
    private fun registerPermissions(){
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
                if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true){ // приравниваем к true, чтобы исключить null
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
            checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
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

    private fun checkLocationEnabled(){
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled){
            DialogManager.showLocEnableDialog(
                activity as AppCompatActivity, object : DialogManager.Listener {
                    override fun onClick() {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
            )
        } else {
            showToast("Location enabled") }
    }

    // Показываем диалог или уведомление с объяснением
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showBackgroundLocationPermissionRationale() {
        // Затем запустить pLauncher для запроса разрешения на фоновое местоположение
        pLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
    }





    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}

/* OLD VERSION

// для разных версий андроид требуется различные разрешения. Для андроид 10 и ранее одно разрешение, а для старше 10 - 2
private fun checkLocPermission(){
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        checkPermissonAfter10()
    } else {
        checkPermissonBefore10()
    }
}

// проверяем разрешения при версии 10 и выше
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissonAfter10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            initOSM() // если есть оба разрешения, то все ОК
        } else { // если нет, то спрашиваем оба разрешения
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }

    // проверяем разрешения до версии 10 и выше
    private fun checkPermissonBefore10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION))
        {
            initOSM() // если есть разрешение, то все ОК
        } else { // если нет, то спрашиваем разрешение
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }


private fun checkLocationEnabled(){
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled){
            showToast("GPS ERR")
        } else {
            showToast("GPS OK")
        }
    }

 */