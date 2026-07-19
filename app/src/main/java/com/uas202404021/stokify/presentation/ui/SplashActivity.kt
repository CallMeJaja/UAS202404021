package com.uas202404021.stokify.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.uas202404021.stokify.R
import com.uas202404021.stokify.data.local.pref.SessionManager
import com.uas202404021.stokify.presentation.ui.auth.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay 1.5 detik lalu pindah ke halaman berikutnya
        Handler(Looper.getMainLooper()).postDelayed({
            val sessionManager = SessionManager(this)

            if (sessionManager.isLoggedIn()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish()
        }, 1500)
    }
}
