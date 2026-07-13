package com.uas202404021.stokify.domain.usecase

class ValidateAuthUseCase {

    fun validateUsername(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult(successful = false, errorMessage = "Username tidak boleh kosong")
        }
        if (username.contains(" ")) {
            return ValidationResult(successful = false, errorMessage = "Username tidak boleh mengandung spasi")
        }
        if (username.length < 4) {
            return ValidationResult(successful = false, errorMessage = "Username minimal 4 karakter")
        }
        return ValidationResult(successful = true)
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(successful = false, errorMessage = "Kata sandi tidak boleh kosong")
        }
        if (password.length < 6) {
            return ValidationResult(successful = false, errorMessage = "Kata sandi minimal 6 karakter")
        }
        return ValidationResult(successful = true)
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        if (confirmPassword.isBlank()) {
            return ValidationResult(successful = false, errorMessage = "Konfirmasi kata sandi tidak boleh kosong")
        }
        if (password != confirmPassword) {
            return ValidationResult(successful = false, errorMessage = "Kata sandi tidak cocok")
        }
        return ValidationResult(successful = true)
    }

    fun validateFullName(fullName: String): ValidationResult {
        if (fullName.isBlank()) {
            return ValidationResult(successful = false, errorMessage = "Nama lengkap tidak boleh kosong")
        }
        return ValidationResult(successful = true)
    }

    fun validateRole(role: String): ValidationResult {
        if (role.isBlank()) {
            return ValidationResult(successful = false, errorMessage = "Peran (Role) belum dipilih")
        }
        return ValidationResult(successful = true)
    }
}

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)