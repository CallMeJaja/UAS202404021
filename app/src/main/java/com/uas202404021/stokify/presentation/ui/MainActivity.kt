package com.uas202404021.stokify.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.uas202404021.stokify.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Menggunakan ViewBinding untuk inisialisasi layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Controller Navigasi
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController

        // Hubungkan Bottom Navigation View dengan Nav Controller
        binding.bottomNavView.setupWithNavController(navController)
    }
}
