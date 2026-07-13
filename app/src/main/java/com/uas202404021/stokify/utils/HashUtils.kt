package com.uas202404021.stokify.utils

import java.security.MessageDigest

/**
 * Utilitas untuk mengamankan data penting seperti hashing kata sandi.
 */
object HashUtils {

    /**
     * Mengenkripsi teks biasa menjadi format hash SHA-256 hex string.
     */
    fun sha256(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}