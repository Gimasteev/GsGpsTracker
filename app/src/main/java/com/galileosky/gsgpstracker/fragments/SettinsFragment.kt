package com.galileosky.gsgpstracker.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.galileosky.gsgpstracker.R
import com.galileosky.gsgpstracker.utils.showToast

// унаследуемся от PreferenceFragmentCompat
class SettinsFragment: PreferenceFragmentCompat() {
    // создадим переменную которая будет наследовать Preference
    private lateinit var timePref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // выбираем разметку для нашего экрана
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        init()
    }

    private fun init() {
        // нахожу данный преференс по ключу
        timePref = findPreference("update_time_key")!!
        val changeListener = onChangeListener()
        timePref.onPreferenceChangeListener = changeListener

    }
    // общая функция происходящих изменений. Возвращает элемент pref, в котором произошли изменения и само измененное значение в value
    private fun onChangeListener(): Preference.OnPreferenceChangeListener{
        return Preference.OnPreferenceChangeListener{
                pref, value ->
                val nameArray = resources.getStringArray(R.array.loc_time_update_name)
                val valueArray = resources.getStringArray(R.array.loc_time_update_value)
                val title = pref.title.toString().substringBefore(":")
                val pos = valueArray.indexOf(value)
                pref.title = "$title: ${nameArray[pos]}"

            // showToast("Изменено на $value")
            true
        }
    }
}