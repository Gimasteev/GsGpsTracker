package com.galileosky.gsgpstracker.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.galileosky.gsgpstracker.R

class SettinsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // выбираем разметку для нашего экрана
        setPreferencesFromResource(R.xml.main_preference, rootKey)
    }
}