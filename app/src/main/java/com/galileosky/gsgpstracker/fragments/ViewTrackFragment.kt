package com.galileosky.gsgpstracker.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.galileosky.gsgpstracker.MainApp
import com.galileosky.gsgpstracker.MainViewModel
import com.galileosky.gsgpstracker.R
import com.galileosky.gsgpstracker.databinding.ViewTrackBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline

class ViewTrackFragment : Fragment() {
    private lateinit var binding: ViewTrackBinding
    private val model: MainViewModel by activityViewModels{
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
            goToStartPosition(polyline.actualPoints[0])
        }
    }

    private fun goToStartPosition(startPosition: GeoPoint){
        binding.map.controller.zoomTo(16.0)
        binding.map.controller.animateTo(startPosition)
    }

    // функция для получения полилинн для постройки трека
    private fun getPolyline(geopoints: String): Polyline{
        // создаем список геоточек
        var polyline = Polyline()
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