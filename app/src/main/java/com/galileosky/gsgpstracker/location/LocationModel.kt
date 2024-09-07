package com.galileosky.gsgpstracker.location

import org.osmdroid.util.GeoPoint
import java.io.Serializable

// с помощью данного класса мы будем передавать данные из сервиса в main fragment
data class LocationModel(
    val velocity: Float = 0.0f,
    val distance: Float = 0.0f,
    val geoPointsList: ArrayList<GeoPoint>
): Serializable {
// Serializable т.к. надо разбить данные для отправки
}
