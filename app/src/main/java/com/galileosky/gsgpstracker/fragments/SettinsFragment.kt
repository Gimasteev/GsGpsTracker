package com.galileosky.gsgpstracker.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.galileosky.gsgpstracker.R

// унаследуемся от PreferenceFragmentCompat
class SettinsFragment: PreferenceFragmentCompat() {
    // создадим переменную которая будет наследовать Preference
    private lateinit var timePref: Preference
    private lateinit var colorPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // выбираем разметку для нашего экрана
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        init()
    }

    private fun init() {
        // нахожу данный преференс по ключу
        timePref = findPreference("update_time_key")!!
        colorPref = findPreference("track_color_key")!!
        val changeListener = onChangeListener()
        timePref.onPreferenceChangeListener = changeListener
        colorPref.onPreferenceChangeListener = changeListener
        initPref()
    }
    // общая функция происходящих изменений. Возвращает элемент pref, в котором произошли изменения и само измененное значение в value
    private fun onChangeListener(): Preference.OnPreferenceChangeListener{
        return Preference.OnPreferenceChangeListener{
                pref, value ->
            // расширяем onChangeListener на возможность собирать изменения как по таймауту, так и по цвету
                when(pref.key){
                    "update_time_key" ->  timePref(value.toString())
                    // для изменения цвета отдельную функцию не делаю
                    "track_color_key" ->  pref.icon?.setTint(Color.parseColor(value.toString()))
                }
            true
        }
    }
            // функция для timepref
    private fun timePref(name:String){
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title = timePref.title.toString().substringBefore(":")
        val pos = valueArray.indexOf(name)
        timePref.title = "$title: ${nameArray[pos]}"
    }



    private fun initPref() {
        val pref = timePref.preferenceManager.sharedPreferences
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title = timePref.title
        val pos = valueArray.indexOf(pref?.getString("update_time_key","3000"))
        timePref.title = "$title: ${nameArray[pos]}"
        // настраиваем цвет по умолчанию
        val trackColor = pref?.getString("track_color_key", "#3e33bf")
        colorPref.icon?.setTint(Color.parseColor(trackColor))
    }
}