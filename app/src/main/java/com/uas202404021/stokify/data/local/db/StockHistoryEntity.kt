package com.uas202404021.stokify.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representasi tabel riwayat stok (stock_history) di database lokal.
 * Mencatat setiap aktivitas penambahan atau pengurangan jumlah barang.
 */
@Entity(
    tableName = "stock_history",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],

            // Jika data produk dihapus dari tabel products,
            // seluruh riwayat stok produk tersebut akan terhapus otomatis secara berantai
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Mendaftarkan index pada productId untuk meningkatkan kecepatan pencarian kueri relasional
    indices = [Index(value = ["productId"])]
)
data class StockHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ID produk yang dirujuk (Foreign Key)
    val productId: Int,

    // Jumlah perubahan stok (contoh: +10 untuk barang masuk, -5 untuk barang keluar)
    val changeAmount: Int,

    // Jenis transaksi stok: "IN" atau "OUT"
    val type: String,

    // Waktu pencatatan transaksi dalam format Milliseconds (timestamp)
    val timestamp: Long
)