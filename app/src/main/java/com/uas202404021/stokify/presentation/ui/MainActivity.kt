package com.uas202404021.stokify.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.uas202404021.stokify.R
import com.uas202404021.stokify.data.local.pref.SessionManager
import com.uas202404021.stokify.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val isAdmin = sessionManager.getRole() == "Admin"

        // Handle window insets untuk BottomNavigationView
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }

        // Handle window insets untuk NavHostFragment
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top)
            insets
        }

        // Setup Controller Navigasi
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController

        // Hubungkan Bottom Navigation View dengan Nav Controller
        binding.bottomNavView.setupWithNavController(navController)

        // Sembunyikan tab Laporan untuk Staff
        if (!isAdmin) {
            binding.bottomNavView.menu.findItem(R.id.navigation_report)?.isVisible = false
        }

        // Sembunyikan Bottom Nav di halaman detail
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.productDetailFragment,
                R.id.addEditProductFragment -> {
                    binding.bottomNavView.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavView.visibility = View.VISIBLE
                }
            }
        }
    }
}
