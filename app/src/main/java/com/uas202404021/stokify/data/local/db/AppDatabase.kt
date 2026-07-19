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
                    .fallbackToDestructiveMigration(dropAllTables = true)
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
            } ?: run {
                // INSTANCE belum di-set saat onCreate dipanggil.
                // Tunggu hingga INSTANCE tersedia lalu seed.
                scope.launch(Dispatchers.IO) {
                    repeat(10) {
                        kotlinx.coroutines.delay(300)
                        if (INSTANCE != null) return@launch
                    }
                    INSTANCE?.let { database ->
                        populateInitialData(database.userDao(), database.productDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(userDao: UserDao, productDao: ProductDao) {
            // 1. Seeding Akun Default
            val adminPasswordHash = HashUtils.sha256("admin123")
            val staffPasswordHash = HashUtils.sha256("staff123")

            userDao.registerUser(UserEntity(username = "admin", passwordHash = adminPasswordHash, fullName = "Owner Toko", role = "Admin"))
            userDao.registerUser(UserEntity(username = "staff", passwordHash = staffPasswordHash, fullName = "Staf Gudang", role = "Staff"))

            // 2. Seeding 20 Data Produk Awal (Toko Kelontong/Sembako)
            val dummyProducts = listOf(
                // Sembako
                ProductEntity(sku = "SBK-001", name = "Beras Premium 5kg", category = "Sembako", stock = 25, minStock = 10, price = 65000.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_01_rice", location = "Rak A-1"),
                ProductEntity(sku = "SBK-002", name = "Minyak Goreng Bimoli 2L", category = "Sembako", stock = 30, minStock = 10, price = 38000.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_02_oil", location = "Rak A-1"),
                ProductEntity(sku = "SBK-003", name = "Gula Pasir Gulaku 1kg", category = "Sembako", stock = 40, minStock = 15, price = 15500.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_03_sugar", location = "Rak A-2"),
                ProductEntity(sku = "SBK-004", name = "Telur Ayam 1kg", category = "Sembako", stock = 20, minStock = 8, price = 28000.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_04_egg", location = "Rak A-2"),
                ProductEntity(sku = "SBK-005", name = "Tepung Terigu Segitiga Biru 1kg", category = "Sembako", stock = 18, minStock = 6, price = 12000.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_05_flour", location = "Rak A-3"),

                // Makanan
                ProductEntity(sku = "MKN-001", name = "Indomie Goreng", category = "Makanan", stock = 80, minStock = 30, price = 3500.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_06_noodle", location = "Rak B-1"),
                ProductEntity(sku = "MKN-002", name = "Kopi Kapal Api Sachet", category = "Makanan", stock = 100, minStock = 40, price = 1800.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_07_coffee", location = "Rak B-1"),
                ProductEntity(sku = "MKN-003", name = "Kecap Bango 275ml", category = "Makanan", stock = 24, minStock = 8, price = 13000.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_08_sauce", location = "Rak B-2"),
                ProductEntity(sku = "MKN-004", name = "Saus Sambal ABC 335ml", category = "Makanan", stock = 20, minStock = 8, price = 14500.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_09_chili", location = "Rak B-2"),
                ProductEntity(sku = "MKN-005", name = "Teh Pucuk Harum 350ml", category = "Makanan", stock = 60, minStock = 20, price = 4000.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_10_tea", location = "Rak B-3"),

                // Minuman
                ProductEntity(sku = "MIN-001", name = "Aqua 600ml", category = "Minuman", stock = 72, minStock = 24, price = 4000.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_11_water", location = "Rak C-1"),
                ProductEntity(sku = "MIN-002", name = "Susu Indomilk 380g", category = "Minuman", stock = 30, minStock = 10, price = 12500.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_12_milk", location = "Rak C-1"),
                ProductEntity(sku = "MIN-003", name = "Teh Celup Sariwangi 25sx", category = "Minuman", stock = 35, minStock = 12, price = 7500.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_13_tea_bag", location = "Rak C-2"),

                // Perawatan
                ProductEntity(sku = "BRS-001", name = "Sabun Lifebuoy Batang 100g", category = "Perawatan", stock = 50, minStock = 20, price = 3500.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_14_soap", location = "Rak D-1"),
                ProductEntity(sku = "BRS-002", name = "Shampo Pantene Sachet", category = "Perawatan", stock = 100, minStock = 40, price = 1000.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_15_shampoo", location = "Rak D-1"),
                ProductEntity(sku = "BRS-003", name = "Minyak Kayu Putih Cap Lang 60ml", category = "Perawatan", stock = 15, minStock = 5, price = 18000.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_16_cajuputi", location = "Rak D-2"),

                // Rumah Tangga
                ProductEntity(sku = "BRH-001", name = "Rinso Anti Noda 760g", category = "Rumah Tangga", stock = 25, minStock = 8, price = 18500.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_17_detergent", location = "Rak E-1"),
                ProductEntity(sku = "BRH-002", name = "Tisu Paseo 250sheet", category = "Rumah Tangga", stock = 40, minStock = 15, price = 5500.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_18_tissue", location = "Rak E-1"),
                ProductEntity(sku = "BRH-003", name = "Wipol Karbol 800ml", category = "Rumah Tangga", stock = 22, minStock = 8, price = 7000.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_19_cleaner", location = "Rak E-2"),
                ProductEntity(sku = "BRH-004", name = "Gas Elpiji 3kg", category = "Rumah Tangga", stock = 10, minStock = 5, price = 22000.0, imageUri = "android.resource://com.uas202404021.stokify/drawable/ic_product_20_gas", location = "Rak E-2")
            )

            for (product in dummyProducts) {
                productDao.insertProduct(product)
            }
        }
    }
}