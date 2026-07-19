package com.uas202404021.stokify.presentation.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.uas202404021.stokify.R
import com.uas202404021.stokify.data.local.pref.SessionManager
import com.uas202404021.stokify.databinding.FragmentProfileBinding
import com.uas202404021.stokify.presentation.ui.auth.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        displayUserInfo()
        setupLogoutButton()
    }

    private fun displayUserInfo() {
        val fullName = sessionManager.getFullName() ?: "User"
        val username = sessionManager.getUsername() ?: "-"
        val role = sessionManager.getRole() ?: "Staff"

        // Header section
        binding.tvFullName.text = fullName
        binding.tvUsername.text = "@$username"
        binding.tvRoleBadge.text = role

        // Info card section
        binding.tvFullNameValue.text = fullName
        binding.tvUsernameValue.text = username
        binding.tvRoleValue.text = role

        // Set warna badge berdasarkan role
        val badgeColor = if (role == "Admin") "#4CAF50" else "#2196F3"
        val badgeDrawable = binding.tvRoleBadge.background
        if (badgeDrawable is android.graphics.drawable.GradientDrawable) {
            badgeDrawable.setColor(android.graphics.Color.parseColor(badgeColor))
        }
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout_confirmation, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .show()

        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            dialog.dismiss()
            performLogout()
        }
    }

    private fun performLogout() {
        // Hapus sesi
        sessionManager.logout()

        // Arahkan ke LoginActivity dan hapus backstack
        val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
