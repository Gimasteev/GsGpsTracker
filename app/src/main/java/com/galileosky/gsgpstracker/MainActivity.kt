package com.galileosky.gsgpstracker

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.galileosky.gsgpstracker.databinding.ActivityMainBinding
import com.galileosky.gsgpstracker.fragments.MainFragment
import com.galileosky.gsgpstracker.fragments.SettinsFragment
import com.galileosky.gsgpstracker.fragments.TracksFragment
import com.galileosky.gsgpstracker.utils.openFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onButtonNavClicks() // слушатель нажатий
        openFragment(MainFragment.newInstance())

    }

    private fun onButtonNavClicks() {
        binding.bNav.setOnItemSelectedListener {
            when (it.itemId) {
                // убираем тосты и меняем на смену фрагментов через кнопки
                R.id.id_home -> openFragment(MainFragment.newInstance())
                R.id.id_tracks -> openFragment(TracksFragment.newInstance())
                R.id.id_settings -> openFragment(SettinsFragment())
            }
            true
        }
    }
}