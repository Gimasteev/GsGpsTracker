package com.galileosky.gsgpstracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.galileosky.gsgpstracker.MainApp
import com.galileosky.gsgpstracker.MainViewModel
import com.galileosky.gsgpstracker.databinding.TracksBinding
import com.galileosky.gsgpstracker.db.TrackAdapter
import com.galileosky.gsgpstracker.db.TrackItem
import com.galileosky.gsgpstracker.utils.openFragment

class TracksFragment : Fragment(), TrackAdapter.Listener{
    private lateinit var binding: TracksBinding
    private lateinit var adapter: TrackAdapter
    private val model: MainViewModel by activityViewModels{
        MainViewModel.viewModelFactory((requireContext().applicationContext as MainApp).database)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        getTracks()
    }

    private fun getTracks(){
        model.tracks.observe(viewLifecycleOwner){
            adapter.submitList(it)
            // проверка чтобы убрать слово пусто на вкладке tracks
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    // функции для работы с разметкой
    private fun initRcView() = with(binding){
        adapter = TrackAdapter(this@TracksFragment)
        rcView.layoutManager = LinearLayoutManager(requireContext())
        rcView.adapter = adapter
    }



    companion object {
        @JvmStatic
        fun newInstance() = TracksFragment()
    }

    // фунция удаления элементов
    override fun onClick(track: TrackItem, type: TrackAdapter.ClickType) {
        when(type){
            TrackAdapter.ClickType.DELETE -> model.deleteTrack(track)
            TrackAdapter.ClickType.OPEN -> {
                // для отображения сохраненного трека
                // передаем трек
                model.currentTrack.value = track
                openFragment(ViewTrackFragment.newInstance())
            }

        }
        //Log.d("MyLog", "Type: $type")
    // для теста оставлю Log.d("MyLog", "Delete track ${track.id}")
        //model.deleteTrack(track)
    }
}