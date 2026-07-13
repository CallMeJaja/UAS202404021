package com.uas202404021.stokify.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.uas202404021.stokify.data.local.pref.SessionManager
import com.uas202404021.stokify.presentation.ui.auth.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen sebelum super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Cek Sesi Login
        val sessionManager = SessionManager(this)
        
        if (sessionManager.isLoggedIn()) {
            // Jika sudah login, ke MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // Jika belum login, ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        
        // Tutup SplashActivity agar user tidak bisa back ke halaman ini
        finish()
    }
}