package com.uas202404021.stokify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.uas202404021.stokify.data.local.db.StockHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Akses data untuk riwayat mutasi stok (masuk/keluar).Plan
 */
@Dao
interface StockHistoryDao {

    @Insert
    suspend fun insertHistory(history: StockHistoryEntity): Long

    // Mengambil semua riwayat mutasi stok untuk produk tertentu (urut dari yang terbaru)
    @Query("SELECT * FROM stock_history WHERE productId = :productId ORDER BY timestamp DESC")
    fun getHistoryForProduct(productId: Int): Flow<List<StockHistoryEntity>>
}