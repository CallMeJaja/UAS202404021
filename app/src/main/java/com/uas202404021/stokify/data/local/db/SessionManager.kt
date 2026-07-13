package com.uas202404021.stokify.data.local.pref

import android.content.Context
import android.content.SharedPreferences

/**
 * Pengelola sesi pengguna menggunakan SharedPreferences.
 * Menyimpan status login, username, nama lengkap, dan peran (role) pengguna secara persisten.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREF_NAME = "stokify_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_ROLE = "role"
    }

    /**
     * Membuat sesi login baru dan menyimpan rincian informasi pengguna.
     */
    fun createLoginSession(username: String, fullName: String, role: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_ROLE, role)
            apply()
        }
    }

    /**
     * Memeriksa apakah pengguna sedang dalam status aktif login.
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Mengambil nama lengkap pengguna yang sedang aktif.
     */
    fun getFullName(): String? {
        return prefs.getString(KEY_FULL_NAME, null)
    }

    /**
     * Mengambil peran (role) pengguna yang sedang aktif (Admin/Staff).
     */
    fun getRole(): String? {
        return prefs.getString(KEY_ROLE, null)
    }

    /**
     * Mengambil username pengguna yang sedang aktif.
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Menghapus seluruh data sesi aktif (dipanggil saat pengguna Logout).
     */
    fun logout() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }
}