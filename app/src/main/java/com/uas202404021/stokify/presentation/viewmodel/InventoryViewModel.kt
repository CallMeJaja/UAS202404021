package com.uas202404021.stokify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uas202404021.stokify.data.local.db.ProductEntity
import com.uas202404021.stokify.data.local.db.StockHistoryEntity
import com.uas202404021.stokify.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _categoryFilter = MutableStateFlow("Semua")
    private val _sortBy = MutableStateFlow("NAME_ASC")

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategoryFilter(category: String) { _categoryFilter.value = category }
    fun setSortBy(sort: String) { _sortBy.value = sort }

    @OptIn(ExperimentalCoroutinesApi::class)
    val productsFlow: StateFlow<List<ProductEntity>> = kotlinx.coroutines.flow.combine(
        _searchQuery,
        _categoryFilter,
        _sortBy
    ) { query, category, sort ->
        Triple(query, category, sort)
    }.flatMapLatest { (query, category, sort) ->
        repository.getProducts(query, category, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getProductById(id: Int): StateFlow<ProductEntity?> {
        return repository.getProductById(id).stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

    fun insertProduct(product: ProductEntity) = viewModelScope.launch {
        repository.insertProduct(product)
    }

    fun updateProduct(product: ProductEntity) = viewModelScope.launch {
        repository.updateProduct(product)
    }

    fun deleteProduct(product: ProductEntity) = viewModelScope.launch {
        repository.deleteProduct(product)
    }
}

class InventoryViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}