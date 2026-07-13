package com.uas202404021.stokify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uas202404021.stokify.data.local.db.UserEntity

/**
 * Akses data untuk pengelolaan pengguna (Autentikasi).
 */
@Dao
interface UserDao {

    // Mendaftarkan user baru, abaikan jika terjadi konflik username
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registerUser(user: UserEntity): Long

    // Mencari user berdasarkan username untuk verifikasi login
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    // Memeriksa apakah username sudah terdaftar di database
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun isUsernameExists(username: String): Boolean
}