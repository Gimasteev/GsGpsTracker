package com.galileosky.gsgpstracker.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.galileosky.gsgpstracker.databinding.FragmentMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding


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
        // добавили иницаиализацию
        initOSM()
    }

    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun initOSM() = with(binding){
        map.controller.setZoom(18.0)
        // Создается экземпляр класса GpsMyLocationProvider, который используется для получения данных о местоположении устройства с помощью GPS.
        // Этот провайдер будет передавать данные о местоположении для использования на карте.
        val mLocProvider = GpsMyLocationProvider(activity)
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


    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}