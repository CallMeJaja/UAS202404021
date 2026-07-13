package com.uas202404021.stokify.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representasi tabel produk (products) di dalam database lokal.
 * Menyimpan seluruh informasi detail barang inventaris gudang.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Kode unik identifikasi barang (Stock Keeping Unit)
    val sku: String,

    val name: String,

    // Kategori produk (diambil dari daftar Spinner tetap)
    val category: String,

    // Jumlah stok fisik saat ini
    val stock: Int,

    // Batas minimum stok aman untuk memicu alert notifikasi lokal
    val minStock: Int,

    val price: Double,

    // Path/URI foto lokal produk di dalam direktori internal privat (context.filesDir)
    val imageUri: String?,

    // Letak penempatan barang di dalam gudang, contoh: "Rak A-3"
    val location: String
)