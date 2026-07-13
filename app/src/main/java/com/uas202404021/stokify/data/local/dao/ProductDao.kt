package com.uas202404021.stokify.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.uas202404021.stokify.data.local.db.ProductEntity
import com.uas202404021.stokify.data.local.db.StockHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Akses data utama untuk manajemen inventaris barang.
 */
@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // Digunakan oleh transaksi atomik pembaruan stok di Repository
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockHistory(history: StockHistoryEntity): Long

    // Mendapatkan satu produk berdasarkan ID untuk halaman detail
    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    fun getProductById(productId: Int): Flow<ProductEntity?>

    // Kueri pencarian, filter kategori, dan sortir secara dinamis menggunakan parameter SQLite
    @Query("""
        SELECT * FROM products 
        WHERE (name LIKE '%' || :searchQuery || '%' OR sku LIKE '%' || :searchQuery || '%')
        AND (:category = 'Semua' OR category = :category)
        ORDER BY 
            CASE WHEN :sortBy = 'NAME_ASC' THEN name END ASC,
            CASE WHEN :sortBy = 'NAME_DESC' THEN name END DESC,
            CASE WHEN :sortBy = 'PRICE_ASC' THEN price END ASC,
            CASE WHEN :sortBy = 'PRICE_DESC' THEN price END DESC
    """)
    fun getProducts(
        searchQuery: String,
        category: String,
        sortBy: String
    ): Flow<List<ProductEntity>>

    // Mendapatkan statistik total nilai aset (hanya untuk Admin)
    @Query("SELECT SUM(stock * price) FROM products")
    fun getTotalAssetValue(): Flow<Double?>

    // Mendapatkan jumlah varian produk unik
    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    // Mendapatkan jumlah barang yang stoknya kritis (di bawah minStock)
    @Query("SELECT COUNT(*) FROM products WHERE stock <= minStock")
    fun getLowStockCount(): Flow<Int>

    // Mendapatkan daftar produk yang stoknya kritis (untuk peringatan dini)
    @Query("SELECT * FROM products WHERE stock <= minStock ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>
}