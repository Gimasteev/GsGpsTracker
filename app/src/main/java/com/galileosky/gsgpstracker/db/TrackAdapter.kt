package com.galileosky.gsgpstracker.db

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.galileosky.gsgpstracker.R
import com.galileosky.gsgpstracker.databinding.TrackItemBinding

class TrackAdapter : ListAdapter<TrackItem, TrackAdapter.Holder>(Comparator()) {
    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding =
            TrackItemBinding.bind(view) // инфлейтить разметку уже не надо. Она уже развернута, поэтому передаю просто view

        fun bind(track: TrackItem) = with(binding) {
            val speed = "${itemView.context.getString(R.string.map_a_speed)} " +
                    "${track.velocity} " +
                    itemView.context.getString(R.string.map_kmh)
            val time = "${track.time}"
            val distance = "${track.distance} ${itemView.context.getString(R.string.map_meters)}"
            tvData.text = track.date
            tvSpeed.text = speed
            tvTimes.text = time
            tvDistance.text = distance
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
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position)) // вызываем  функцию bind для заполнения ячейки
    }

}