package com.uas202404021.stokify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uas202404021.stokify.data.local.db.ProductEntity
import com.uas202404021.stokify.data.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class CategoryCount(
    val category: String,
    val count: Int
)

data class StockStatus(
    val safe: Int,
    val low: Int,
    val empty: Int
)

class ReportViewModel(
    private val repository: AppRepository
) : ViewModel() {

    // Dashboard Statistics
    val productCount: StateFlow<Int> = repository.getProductCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val lowStockCount: StateFlow<Int> = repository.getLowStockCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalAssetValue: StateFlow<Double?> = repository.getTotalAssetValue()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.getProducts("", "Semua", "NAME_ASC")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getStockStatus(products: List<ProductEntity>): StockStatus {
        var safe = 0
        var low = 0
        var empty = 0

        products.forEach { product ->
            when {
                product.stock == 0 -> empty++
                product.stock <= product.minStock -> low++
                else -> safe++
            }
        }

        return StockStatus(safe, low, empty)
    }

    fun getCategoryDistribution(products: List<ProductEntity>): List<CategoryCount> {
        return products.groupBy { it.category }
            .map { (category, products) -> CategoryCount(category, products.size) }
            .sortedByDescending { it.count }
    }

    fun getTopLowStockProducts(products: List<ProductEntity>, limit: Int = 5): List<ProductEntity> {
        return products.sortedBy { it.stock }
            .take(limit)
    }

    fun getTotalStock(products: List<ProductEntity>): Int {
        return products.sumOf { it.stock }
    }
}

class ReportViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
