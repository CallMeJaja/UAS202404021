package com.uas202404021.stokify.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uas202404021.stokify.data.local.dao.ProductDao
import com.uas202404021.stokify.data.local.dao.StockHistoryDao
import com.uas202404021.stokify.data.local.dao.UserDao
import com.uas202404021.stokify.utils.HashUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Konfigurasi database lokal Room untuk aplikasi Stokify.
 */
@Database(
    entities = [UserEntity::class, ProductEntity::class, StockHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun stockHistoryDao(): StockHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stokify_database"
                )
                    // Memungkinkan penghapusan data secara destruktif jika skema berubah selama pengembangan
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback untuk mengisi data awal (seeding) saat database pertama kali dibuat.
     */
    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.userDao(), database.productDao())
                }
            }
        }

        suspend fun populateInitialData(userDao: UserDao, productDao: ProductDao) {
            // 1. Seeding Akun Default
            val adminPasswordHash = HashUtils.sha256("admin123")
            val staffPasswordHash = HashUtils.sha256("staff123")

            userDao.registerUser(UserEntity(username = "admin", passwordHash = adminPasswordHash, fullName = "Owner Toko", role = "Admin"))
            userDao.registerUser(UserEntity(username = "staff", passwordHash = staffPasswordHash, fullName = "Staf Gudang", role = "Staff"))

            // 2. Seeding 20 Data Produk Awal
            val dummyProducts = listOf(
                // Kategori: Elektronik
                ProductEntity(sku = "ELK-001", name = "Mouse Wireless Logitech", category = "Elektronik", stock = 15, minStock = 5, price = 150000.0, imageUri = null, location = "Rak A-1"),
                ProductEntity(sku = "ELK-002", name = "Keyboard Mechanical Rexus", category = "Elektronik", stock = 8, minStock = 3, price = 350000.0, imageUri = null, location = "Rak A-1"),
                ProductEntity(sku = "ELK-003", name = "Headset Gaming Fantech", category = "Elektronik", stock = 20, minStock = 5, price = 250000.0, imageUri = null, location = "Rak A-2"),
                ProductEntity(sku = "ELK-004", name = "Kabel HDMI 2 Meter", category = "Elektronik", stock = 40, minStock = 10, price = 450000.0, imageUri = null, location = "Rak A-2"),

                // Kategori: Pakaian
                ProductEntity(sku = "PKN-001", name = "Kaos Polos Hitam XL", category = "Pakaian", stock = 25, minStock = 8, price = 75000.0, imageUri = null, location = "Rak B-1"),
                ProductEntity(sku = "PKN-002", name = "Kemeja Flanel Kotak", category = "Pakaian", stock = 12, minStock = 4, price = 150000.0, imageUri = null, location = "Rak B-1"),
                ProductEntity(sku = "PKN-003", name = "Jaket Hoodie Navy M", category = "Pakaian", stock = 5, minStock = 3, price = 200000.0, imageUri = null, location = "Rak B-2"),
                ProductEntity(sku = "PKN-004", name = "Celana Chino Cream L", category = "Pakaian", stock = 18, minStock = 5, price = 180000.0, imageUri = null, location = "Rak B-2"),

                // Kategori: Makanan/Minuman
                ProductEntity(sku = "MKN-001", name = "Kopi Bubuk Arabika 250g", category = "Makanan/Minuman", stock = 30, minStock = 10, price = 45000.0, imageUri = null, location = "Rak C-1"),
                ProductEntity(sku = "MKN-002", name = "Teh Hijau Celup Kotak", category = "Makanan/Minuman", stock = 22, minStock = 6, price = 15000.0, imageUri = null, location = "Rak C-1"),
                ProductEntity(sku = "MKN-003", name = "Cokelat Batang SilverQueen", category = "Makanan/Minuman", stock = 50, minStock = 15, price = 20000.0, imageUri = null, location = "Rak C-2"),
                ProductEntity(sku = "MKN-004", name = "Biskuit Kaleng Roma", category = "Makanan/Minuman", stock = 3, minStock = 5, price = 35000.0, imageUri = null, location = "Rak C-2"), // Kondisi Kritis

                // Kategori: Aksesoris
                ProductEntity(sku = "AKS-001", name = "Gantungan Kunci Kulit", category = "Aksesoris", stock = 60, minStock = 10, price = 12000.0, imageUri = null, location = "Rak D-1"),
                ProductEntity(sku = "AKS-002", name = "Kacamata Hitam Sun", category = "Aksesoris", stock = 14, minStock = 4, price = 85000.0, imageUri = null, location = "Rak D-1"),
                ProductEntity(sku = "AKS-003", name = "Topi Baseball Polos", category = "Aksesoris", stock = 2, minStock = 4, price = 50000.0, imageUri = null, location = "Rak D-2"), // Kondisi Kritis
                ProductEntity(sku = "AKS-004", name = "Dompet Kartu Slim", category = "Aksesoris", stock = 16, minStock = 5, price = 95000.0, imageUri = null, location = "Rak D-2"),

                // Kategori: Lainnya
                ProductEntity(sku = "LNY-001", name = "Pena Gel Hitam 0.5 (Pack)", category = "Lainnya", stock = 35, minStock = 5, price = 30000.0, imageUri = null, location = "Rak E-1"),
                ProductEntity(sku = "LNY-002", name = "Buku Catatan Grid A5", category = "Lainnya", stock = 20, minStock = 5, price = 25000.0, imageUri = null, location = "Rak E-1"),
                ProductEntity(sku = "LNY-003", name = "Plester Luka Medis (Box)", category = "Lainnya", stock = 40, minStock = 10, price = 15000.0, imageUri = null, location = "Rak E-2"),
                ProductEntity(sku = "LNY-004", name = "Payung Lipat Otomatis", category = "Lainnya", stock = 1, minStock = 3, price = 75000.0, imageUri = null, location = "Rak E-2") // Kondisi Kritis
            )

            for (product in dummyProducts) {
                productDao.insertProduct(product)
            }
        }
    }
}