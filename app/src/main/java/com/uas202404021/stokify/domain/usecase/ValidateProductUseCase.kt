package com.uas202404021.stokify.domain.usecase

class ValidateProductUseCase {

    fun validateSku(sku: String): ValidationResult {
        if (sku.isBlank()) {
            return ValidationResult(successful = false, errorMessage = "SKU tidak boleh kosong")
        }
        return ValidationResult(successful = true)
    }

    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(successful = false, errorMessage = "Nama produk tidak boleh kosong")
        }
        if (name.length < 3) {
            return ValidationResult(successful = false, errorMessage = "Nama minimal 3 karakter")
        }
        return ValidationResult(successful = true)
    }

    fun validateCategory(category: String): ValidationResult {
        if (category.isBlank() || category == "Pilih Kategori") {
            return ValidationResult(successful = false, errorMessage = "Pilih kategori yang valid")
        }
        return ValidationResult(successful = true)
    }

    fun validateStock(stockText: String): ValidationResult {
        val stock = stockText.toIntOrNull()
        if (stock == null) {
            return ValidationResult(successful = false, errorMessage = "Stok harus berupa angka")
        }
        if (stock < 0) {
            return ValidationResult(successful = false, errorMessage = "Stok tidak boleh negatif")
        }
        return ValidationResult(successful = true)
    }

    fun validateMinStock(minStockText: String): ValidationResult {
        val minStock = minStockText.toIntOrNull()
        if (minStock == null) {
            return ValidationResult(successful = false, errorMessage = "Min. Stok harus berupa angka")
        }
        if (minStock < 0) {
            return ValidationResult(successful = false, errorMessage = "Min. Stok tidak boleh negatif")
        }
        return ValidationResult(successful = true)
    }

    fun validatePrice(priceText: String): ValidationResult {
        val price = priceText.toDoubleOrNull()
        if (price == null) {
            return ValidationResult(successful = false, errorMessage = "Harga harus berupa angka")
        }
        if (price < 0.0) {
            return ValidationResult(successful = false, errorMessage = "Harga tidak boleh negatif")
        }
        return ValidationResult(successful = true)
    }

    fun validateLocation(location: String): ValidationResult {
        if (location.isBlank()) {
            return ValidationResult(successful = false, errorMessage = "Lokasi rak tidak boleh kosong")
        }
        return ValidationResult(successful = true)
    }
}