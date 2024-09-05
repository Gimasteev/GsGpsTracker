package com.galileosky.gsgpstracker.utils

import android.annotation.SuppressLint
import android.icu.util.Calendar
import java.text.SimpleDateFormat
import java.util.TimeZone

object TimeUtils {
    // форматируем время и кладем в переменные
    @SuppressLint("SimpleDateFormat")
    private val timeFormatter = SimpleDateFormat("HH:mm:ss")

    fun getTime(timeInMillis: Long): String{
        val cv = Calendar.getInstance()
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        cv.timeInMillis = timeInMillis
        return timeFormatter.format(cv.time)
    }
}