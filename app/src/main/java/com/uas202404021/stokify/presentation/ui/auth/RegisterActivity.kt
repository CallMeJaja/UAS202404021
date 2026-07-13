package com.uas202404021.stokify.presentation.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.uas202404021.stokify.R
import com.uas202404021.stokify.data.local.db.AppDatabase
import com.uas202404021.stokify.data.repository.AppRepository
import com.uas202404021.stokify.databinding.ActivityRegisterBinding
import com.uas202404021.stokify.domain.usecase.ValidateAuthUseCase
import com.uas202404021.stokify.presentation.viewmodel.AuthState
import com.uas202404021.stokify.presentation.viewmodel.AuthViewModel
import com.uas202404021.stokify.presentation.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: AuthViewModel by viewModels {
        val database = AppDatabase.getDatabase(this, lifecycleScope)
        val repository = AppRepository(database.userDao(), database.productDao(), database.stockHistoryDao())
        AuthViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets agar konten tidak nabrak status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        setupDropdown()
        setupListeners()
        observeViewModel()
    }

    private fun setupDropdown() {
        val roles = resources.getStringArray(R.array.roles_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.actRole.setAdapter(adapter)
    }

    private fun setupListeners() {
        // Clear errors saat user mulai mengetik
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.tilFullName.error = null
                binding.tilUsername.error = null
                binding.tilPassword.error = null
                binding.tilConfirmPassword.error = null
                binding.tilRole.error = null
            }
        }
        binding.etFullName.addTextChangedListener(textWatcher)
        binding.etUsername.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)
        binding.etConfirmPassword.addTextChangedListener(textWatcher)
        binding.actRole.addTextChangedListener(textWatcher)

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val role = binding.actRole.text.toString().trim()

            // Inline validation
            val validateUseCase = ValidateAuthUseCase()
            val fullNameResult = validateUseCase.validateFullName(fullName)
            val usernameResult = validateUseCase.validateUsername(username)
            val passwordResult = validateUseCase.validatePassword(password)
            val confirmResult = validateUseCase.validateConfirmPassword(password, confirmPassword)
            val roleResult = validateUseCase.validateRole(role)

            var hasError = false
            if (!fullNameResult.successful) {
                binding.tilFullName.error = fullNameResult.errorMessage
                hasError = true
            }
            if (!usernameResult.successful) {
                binding.tilUsername.error = usernameResult.errorMessage
                hasError = true
            }
            if (!passwordResult.successful) {
                binding.tilPassword.error = passwordResult.errorMessage
                hasError = true
            }
            if (!confirmResult.successful) {
                binding.tilConfirmPassword.error = confirmResult.errorMessage
                hasError = true
            }
            if (!roleResult.successful) {
                binding.tilRole.error = roleResult.errorMessage
                hasError = true
            }
            if (hasError) return@setOnClickListener

            viewModel.register(fullName, username, password, confirmPassword, role)
        }

        binding.tvLoginLink.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                    }
                    is AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnRegister.isEnabled = false
                    }
                    is AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()
                        finish() // Sukses, kembali ke Login
                    }
                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        // Tampilkan error di field username (karena validasi sudah di frontend)
                        binding.tilUsername.error = state.message
                        viewModel.resetState()
                    }
                }
            }
        }
    }
}