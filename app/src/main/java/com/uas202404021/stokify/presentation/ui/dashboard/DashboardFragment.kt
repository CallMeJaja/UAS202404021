package com.uas202404021.stokify.presentation.ui.dashboard

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.uas202404021.stokify.R
import com.uas202404021.stokify.data.local.db.AppDatabase
import com.uas202404021.stokify.data.local.pref.SessionManager
import com.uas202404021.stokify.data.repository.AppRepository
import com.uas202404021.stokify.databinding.FragmentDashboardBinding
import com.uas202404021.stokify.presentation.adapter.WarningProductAdapter
import com.uas202404021.stokify.presentation.viewmodel.InventoryViewModel
import com.uas202404021.stokify.presentation.viewmodel.InventoryViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var warningAdapter: WarningProductAdapter

    private val viewModel: InventoryViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext(), lifecycleScope)
        val repository = AppRepository(database.userDao(), database.productDao(), database.stockHistoryDao())
        InventoryViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        val isAdmin = sessionManager.getRole() == "Admin"

        setupGreeting()
        setupRoleControl(isAdmin)
        setupWarningRecyclerView()
        observeDashboardData()
    }

    private fun setupGreeting() {
        val fullName = sessionManager.getFullName() ?: "User"
        val role = sessionManager.getRole() ?: "Staff"
        binding.tvGreeting.text = "Halo, $fullName!"
        binding.tvRoleBadge.text = role

        // Set badge color based on role
        val badgeBg = binding.tvRoleBadge.background
        if (badgeBg is GradientDrawable) {
            val color = if (role == "Admin") "#4CAF50" else "#2196F3"
            badgeBg.setColor(android.graphics.Color.parseColor(color))
        }
    }

    private fun setupRoleControl(isAdmin: Boolean) {
        // Total Aset hanya untuk Admin
        binding.cardAsset.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    private fun setupWarningRecyclerView() {
        warningAdapter = WarningProductAdapter { product ->
            val bundle = bundleOf("productId" to product.id)
            findNavController().navigate(R.id.action_navigation_dashboard_to_productDetailFragment, bundle)
        }
        binding.rvWarningProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = warningAdapter
        }
    }

    private fun observeDashboardData() {
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productCount.collect { count ->
                binding.tvProductCount.text = count.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lowStockCount.collect { count ->
                binding.tvLowStockCount.text = count.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalAssetValue.collect { value ->
                binding.tvTotalAsset.text = formatRupiah.format(value ?: 0.0)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lowStockProducts.collect { products ->
                warningAdapter.submitList(products)
                binding.tvEmptyWarning.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
                binding.rvWarningProducts.visibility = if (products.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
