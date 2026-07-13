package com.uas202404021.stokify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uas202404021.stokify.data.local.db.UserEntity
import com.uas202404021.stokify.data.repository.AppRepository
import com.uas202404021.stokify.domain.usecase.ValidateAuthUseCase
import com.uas202404021.stokify.utils.HashUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String, val user: UserEntity? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: AppRepository,
    private val validateAuthUseCase: ValidateAuthUseCase = ValidateAuthUseCase()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun login(username: String, passwordRaw: String) {
        val usernameResult = validateAuthUseCase.validateUsername(username)
        val passwordResult = validateAuthUseCase.validatePassword(passwordRaw)

        if (!usernameResult.successful) {
            _authState.value = AuthState.Error(usernameResult.errorMessage ?: "Username tidak valid")
            return
        }
        if (!passwordResult.successful) {
            _authState.value = AuthState.Error(passwordResult.errorMessage ?: "Password tidak valid")
            return
        }

        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                val user = repository.getUserByUsername(username)
                if (user == null) {
                    _authState.value = AuthState.Error("Akun tidak ditemukan")
                } else {
                    val passwordHash = HashUtils.sha256(passwordRaw)
                    if (user.passwordHash == passwordHash) {
                        _authState.value = AuthState.Success("Login berhasil", user)
                    } else {
                        _authState.value = AuthState.Error("Kata sandi salah")
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun register(fullName: String, username: String, passwordRaw: String, confirmPasswordRaw: String, role: String) {
        val fullNameResult = validateAuthUseCase.validateFullName(fullName)
        val usernameResult = validateAuthUseCase.validateUsername(username)
        val passwordResult = validateAuthUseCase.validatePassword(passwordRaw)
        val confirmPasswordResult = validateAuthUseCase.validateConfirmPassword(passwordRaw, confirmPasswordRaw)
        val roleResult = validateAuthUseCase.validateRole(role)

        if (!fullNameResult.successful) {
            _authState.value = AuthState.Error(fullNameResult.errorMessage ?: "Nama harus diisi")
            return
        }
        if (!usernameResult.successful) {
            _authState.value = AuthState.Error(usernameResult.errorMessage ?: "Username tidak valid")
            return
        }
        if (!passwordResult.successful) {
            _authState.value = AuthState.Error(passwordResult.errorMessage ?: "Password tidak valid")
            return
        }
        if (!confirmPasswordResult.successful) {
            _authState.value = AuthState.Error(confirmPasswordResult.errorMessage ?: "Konfirmasi kata sandi salah")
            return
        }
        if (!roleResult.successful) {
            _authState.value = AuthState.Error(roleResult.errorMessage ?: "Peran harus dipilih")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val exists = repository.isUsernameExists(username)
                if (exists) {
                    _authState.value = AuthState.Error("Username sudah terdaftar")
                } else {
                    val passwordHash = HashUtils.sha256(passwordRaw)
                    val newUser = UserEntity(
                        username = username,
                        passwordHash = passwordHash,
                        fullName = fullName,
                        role = role
                    )
                    val resultId = repository.registerUser(newUser)
                    if (resultId > 0) {
                        _authState.value = AuthState.Success("Pendaftaran berhasil, silakan login")
                    } else {
                        _authState.value = AuthState.Error("Gagal mendaftarkan akun")
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }
}

// ViewModelFactory manual karena tidak menggunakan Hilt
class AuthViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}