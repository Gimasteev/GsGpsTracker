package com.galileosky.gsgpstracker.utils

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.galileosky.gsgpstracker.R

// создаю функцию для фрагментов
fun Fragment.openFragment(f:Fragment) {
    (activity as AppCompatActivity).supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out) // добавим анимацию
        .replace(R.id.placeHolder, f).commit() // переключение с одного фрагмента на другой
}

// создаю функцию для активити
fun AppCompatActivity.openFragment(f:Fragment) {
    // Пример получения имени фрагмента в logcat
    //Log.d("MyLog","Fragment name ${f.javaClass}")
    if (supportFragmentManager.fragments.isNotEmpty()) // условие для того, чтобы не открывалось одно и то же активити
        if (supportFragmentManager.fragments[0].javaClass == f.javaClass) return // если в списке фрагментов есть уже фрагмент, то мы его не заменяем
    supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out) // добавим анимацию
        .replace(R.id.placeHolder, f).commit() // переключение с одного фрагмента на другой
}

// функции для тостов в фрагментах
fun Fragment.showToast(s: String) {
    Toast.makeText(activity,s, Toast.LENGTH_SHORT).show()
}

// функции для тостов в активити
fun AppCompatActivity.showToast(s: String) {
    Toast.makeText(this,s, Toast.LENGTH_SHORT).show()
}

// Функция запроса разрешения
fun Fragment.checkPermission(p: String): Boolean{
    return when(PackageManager.PERMISSION_GRANTED)
    { //PERMISSION_GRANTED == 0 это значит что разрешение получено
        ContextCompat.checkSelfPermission(activity as AppCompatActivity, p) -> true
        else -> false
    }
}