package com.galileosky.gsgpstracker

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.galileosky.gsgpstracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}