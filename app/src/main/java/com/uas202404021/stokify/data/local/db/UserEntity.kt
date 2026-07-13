package com.uas202404021.stokify.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representasi tabel pengguna (users) di dalam database lokal.
 * Digunakan untuk menangani proses pendaftaran dan autentikasi login.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Nama unik untuk keperluan login
    val username: String,

    // Kata sandi yang sudah dienkripsi menggunakan algoritme SHA-256
    val passwordHash: String,

    // Nama lengkap pengguna/karyawan
    val fullName: String,

    // Peran pengguna di dalam sistem: "Admin" atau "Staff"
    val role: String
)