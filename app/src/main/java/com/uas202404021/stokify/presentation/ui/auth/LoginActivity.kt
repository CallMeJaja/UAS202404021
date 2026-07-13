package com.uas202404021.stokify.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.uas202404021.stokify.data.local.db.AppDatabase
import com.uas202404021.stokify.data.local.pref.SessionManager
import com.uas202404021.stokify.data.repository.AppRepository
import com.uas202404021.stokify.databinding.ActivityLoginBinding
import com.uas202404021.stokify.domain.usecase.ValidateAuthUseCase
import com.uas202404021.stokify.presentation.ui.MainActivity
import com.uas202404021.stokify.presentation.viewmodel.AuthState
import com.uas202404021.stokify.presentation.viewmodel.AuthViewModel
import com.uas202404021.stokify.presentation.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    private val viewModel: AuthViewModel by viewModels {
        val database = AppDatabase.getDatabase(this, lifecycleScope)
        val repository = AppRepository(database.userDao(), database.productDao(), database.stockHistoryDao())
        AuthViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets agar konten tidak nabrak status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Clear errors saat user mulai mengetik
        binding.etUsername.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilUsername.error = null
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.etPassword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilPassword.error = null
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Inline validation
            val validateUseCase = ValidateAuthUseCase()
            val usernameResult = validateUseCase.validateUsername(username)
            val passwordResult = validateUseCase.validatePassword(password)

            var hasError = false
            if (!usernameResult.successful) {
                binding.tilUsername.error = usernameResult.errorMessage
                hasError = true
            }
            if (!passwordResult.successful) {
                binding.tilPassword.error = passwordResult.errorMessage
                hasError = true
            }
            if (hasError) return@setOnClickListener

            viewModel.login(username, password)
        }

        binding.tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                    is AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnLogin.isEnabled = false
                    }
                    is AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        
                        // Simpan Sesi
                        state.user?.let {
                            sessionManager.createLoginSession(
                                username = it.username,
                                fullName = it.fullName,
                                role = it.role
                            )
                        }

                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                        
                        // Pindah ke MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        // Tampilkan error di field password (karena username sudah tervalidasi)
                        binding.tilPassword.error = state.message
                        viewModel.resetState()
                    }
                }
            }
        }
    }
}