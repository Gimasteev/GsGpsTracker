package com.galileosky.gsgpstracker.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.galileosky.gsgpstracker.MainApp
import com.galileosky.gsgpstracker.MainViewModel
import com.galileosky.gsgpstracker.R
import com.galileosky.gsgpstracker.databinding.ViewTrackBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class ViewTrackFragment : Fragment() {
    private var startPoint: GeoPoint? = null
    private lateinit var binding: ViewTrackBinding
    private val model: MainViewModel by activityViewModels {
        MainViewModel.viewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm()
        binding = ViewTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // запустим ловлю трека)
        getTrack()
        // функция для центрирования экрана на начале трека
        binding.fCentr.setOnClickListener{
            if (startPoint != null)binding.map.controller.animateTo(startPoint)
        }
    }

    private fun getTrack() = with(binding){
        // ловим трек
        model.currentTrack.observe(viewLifecycleOwner){
            // заполняем текстовую часть
            val date = "${getString(R.string.date)} ${it.date}"
            val avergeSpeed = "${getString(R.string.map_a_speed)} ${it.speed} ${getString(R.string.map_kmh)}"
            val distance = "${getString(R.string.save_distance)} ${it.distance} ${getString(R.string.map_meters)}"
            tvData.text = date
            tvTime.text = it.time
            tvAverageSpeed.text = avergeSpeed
            tvDistance.text = distance
            // добавляем полилинию
            // перерабатываем точки + парсинг
            val polyline = getPolyline(it.geoPoints)
            map.overlays.add(polyline)
            setMarkers(polyline.actualPoints)
            goToStartPosition(polyline.actualPoints[0])
            startPoint = polyline.actualPoints[0]
        }
    }

    private fun goToStartPosition(startPosition: GeoPoint){
        binding.map.controller.zoomTo(16.0)
        binding.map.controller.animateTo(startPosition)
    }

    // маркеры для обзначения начальной и конечной позиции трека
    private fun setMarkers(list: List<GeoPoint>) = with(binding){
        val startMarker = Marker(map)
        val finishMarker = Marker(map)
        // Настраиваем маркеры
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        finishMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        // Добавляем иконки старта и конца трека
        startMarker.icon = getDrawable(requireContext(), R.drawable.ic_start_location_24)
        finishMarker.icon = getDrawable(requireContext(), R.drawable.ic_location_off_24)
        startMarker.position = list[0]
        finishMarker.position = list[list.size - 1]
        // накладываем как слой на карту
        map.overlays.add(startMarker)
        map.overlays.add(finishMarker)

    }

    // функция для получения полилинн для постройки трека
    private fun getPolyline(geopoints: String): Polyline{
        // создаем список геоточек
        val polyline = Polyline()
        // меняем цвет трека в зависимости от настроек - track_color_key
        polyline.outlinePaint.color = Color.parseColor(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("track_color_key", "#FF3E33BF")
        )
        val list = geopoints.split("/")
        list.forEach{
            // исключаю ошибку none последнего элемента после конечного слэша
            if (it.isEmpty()) return@forEach
            // Убираем запиятые из координат
            val points = it.split(",")
            polyline.addPoint(GeoPoint(points[0].toDouble(), points[1].toDouble()))
        }
        return polyline
    }

    private fun settingsOsm() {
        Configuration.getInstance().load(
            requireActivity(),
            requireActivity().getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    companion object {
        @JvmStatic
        fun newInstance() = ViewTrackFragment()
    }
}