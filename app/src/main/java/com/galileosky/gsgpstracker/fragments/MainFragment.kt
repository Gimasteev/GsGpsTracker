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
import com.galileosky.gsgpstracker.databinding.FragmentMainBinding
import com.galileosky.gsgpstracker.utils.DialogManager
import com.galileosky.gsgpstracker.utils.checkPermission
import com.galileosky.gsgpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
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
        checkLocPermission()
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