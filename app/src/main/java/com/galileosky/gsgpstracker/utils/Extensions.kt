package com.galileosky.gsgpstracker.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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