package com.galileosky.gsgpstracker.db
// Адаптер для всех элементов трека и добавляем их в tv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.galileosky.gsgpstracker.R
import com.galileosky.gsgpstracker.databinding.TrackItemBinding

class TrackAdapter(private val listener: Listener) : ListAdapter<TrackItem, TrackAdapter.Holder>(Comparator()) { // listener глобальный поэтому val
    class Holder(view: View, private val listener: Listener) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val binding = TrackItemBinding.bind(view) // инфлейтить разметку уже не надо. Она уже развернута, поэтому передаю просто view
        private var trackTemp: TrackItem? = null // контейнер для хранения полученных значений из функции bind
        init {
            binding.ibDelete.setOnClickListener(this)
            binding.item.setOnClickListener(this)
        }
        fun bind(track: TrackItem) = with(binding) {
            trackTemp = track
            val speed = "${itemView.context.getString(R.string.map_a_speed)} " +
                    "${track.speed} " +
                    itemView.context.getString(R.string.map_kmh)
            val time = track.time
            val distance = "${track.distance} ${itemView.context.getString(R.string.map_meters)}"
            tvData.text = track.date
            tvSpeed.text = speed
            tvTimes.text = time
            tvDistance.text = distance
        }
        // ко всему холдеру добавляем функцию слушатель нажатий
        override fun onClick(view: View) {
            val type = when(view.id){
                // оставлю старый код trackTemp?.let { listener.onClick(it, ClickType.DELETE) } // если trackTemp != null только тогда запуститься let
                R.id.ibDelete -> ClickType.DELETE
                R.id.item -> ClickType.OPEN
                else -> ClickType.OPEN
            }
            trackTemp?.let { listener.onClick(it, type) }

        }
    }
    // специальный класс компоратор, который будет следить за объектами и не давать собирать весь список по новой
    class Comparator: DiffUtil.ItemCallback<TrackItem>(){
        override fun areItemsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem.id == newItem.id // срвниваем новый и старый идентификаторы
        }

        override fun areContentsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false) // таким образом создали view
        return Holder(view, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position)) // вызываем  функцию bind для заполнения ячейки
    }

    // мост между адаптером и фрагментом
    interface Listener {
        fun onClick(track: TrackItem, type: ClickType)
    }
    // для просмотра трека
    enum class ClickType{
        DELETE,
        OPEN
    }

}