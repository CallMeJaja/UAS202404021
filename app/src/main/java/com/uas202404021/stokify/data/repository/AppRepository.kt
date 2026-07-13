package com.uas202404021.stokify.data.repository

import com.uas202404021.stokify.data.local.dao.ProductDao
import com.uas202404021.stokify.data.local.dao.StockHistoryDao
import com.uas202404021.stokify.data.local.dao.UserDao
import com.uas202404021.stokify.data.local.db.ProductEntity
import com.uas202404021.stokify.data.local.db.StockHistoryEntity
import com.uas202404021.stokify.data.local.db.UserEntity
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val userDao: UserDao,
    private val productDao: ProductDao,
    private val stockHistoryDao: StockHistoryDao
) {
    // Auth Operations
    suspend fun registerUser(user: UserEntity): Long {
        return userDao.registerUser(user)
    }

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun isUsernameExists(username: String): Boolean {
        return userDao.isUsernameExists(username)
    }

    // Product Operations
    fun getProducts(searchQuery: String, category: String, sortBy: String): Flow<List<ProductEntity>> {
        return productDao.getProducts(searchQuery, category, sortBy)
    }

    fun getProductById(productId: Int): Flow<ProductEntity?> {
        return productDao.getProductById(productId)
    }

    suspend fun insertProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    // Stock History Operations
    fun getHistoryForProduct(productId: Int): Flow<List<StockHistoryEntity>> {
        return stockHistoryDao.getHistoryForProduct(productId)
    }

    suspend fun updateStockWithHistory(product: ProductEntity, changeAmount: Int, type: String) {
        val newStock = if (type == "IN") product.stock + changeAmount else product.stock - changeAmount
        val updatedProduct = product.copy(stock = newStock)
        productDao.updateProduct(updatedProduct)
        productDao.insertStockHistory(
            StockHistoryEntity(
                productId = product.id,
                changeAmount = changeAmount,
                type = type,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}