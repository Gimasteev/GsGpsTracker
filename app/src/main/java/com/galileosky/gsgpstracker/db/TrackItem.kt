package com.galileosky.gsgpstracker.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// создаем таблицу
@Entity(tableName = "Track")
data class TrackItem(
    // аннотация для ключей
    // чтобы при появленнии нового элемента его уникальный номер был +1
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    @ColumnInfo(name = "time")
    val time: String,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "distance")
    val distance: String,
    @ColumnInfo(name = "velocity")
    val velocity: String,
    @ColumnInfo(name = "geo_points")
    val geoPoints: String
)
