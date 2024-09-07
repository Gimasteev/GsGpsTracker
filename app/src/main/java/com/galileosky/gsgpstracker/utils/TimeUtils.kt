package com.galileosky.gsgpstracker.utils

import android.annotation.SuppressLint
import android.icu.util.Calendar
import java.text.SimpleDateFormat
import java.util.TimeZone
@SuppressLint("SimpleDateFormat")
object TimeUtils {
    // форматируем время и кладем в переменные

    private val timeFormatter = SimpleDateFormat("HH:mm:ss")
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm")

    fun getTime(timeInMillis: Long): String{
        val cv = Calendar.getInstance()
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        cv.timeInMillis = timeInMillis
        return timeFormatter.format(cv.time)
    }

    fun getDate(): String{
        val cv = Calendar.getInstance()
        return dateFormatter.format(cv.time)
    }
}