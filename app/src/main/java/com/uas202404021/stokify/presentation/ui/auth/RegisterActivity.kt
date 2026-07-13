package com.uas202404021.stokify.presentation.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uas202404021.stokify.R
import com.uas202404021.stokify.data.local.db.AppDatabase
import com.uas202404021.stokify.data.repository.AppRepository
import com.uas202404021.stokify.databinding.ActivityRegisterBinding
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
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val role = binding.actRole.text.toString().trim()

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
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }
}