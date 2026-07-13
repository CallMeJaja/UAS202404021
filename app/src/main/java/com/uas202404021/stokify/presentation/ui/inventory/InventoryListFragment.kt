package com.uas202404021.stokify.presentation.ui.inventory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.uas202404021.stokify.R
import com.uas202404021.stokify.data.local.db.AppDatabase
import com.uas202404021.stokify.data.local.pref.SessionManager
import com.uas202404021.stokify.data.repository.AppRepository
import com.uas202404021.stokify.databinding.FragmentInventoryListBinding
import com.uas202404021.stokify.presentation.adapter.ProductAdapter
import com.uas202404021.stokify.presentation.viewmodel.InventoryViewModel
import com.uas202404021.stokify.presentation.viewmodel.InventoryViewModelFactory
import kotlinx.coroutines.launch

class InventoryListFragment : Fragment() {

    private var _binding: FragmentInventoryListBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var productAdapter: ProductAdapter

    private val viewModel: InventoryViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext(), lifecycleScope)
        val repository = AppRepository(database.userDao(), database.productDao(), database.stockHistoryDao())
        InventoryViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val isAdmin = sessionManager.getRole() == "Admin"

        // Mengatur Visibilitas FAB berdasarkan Role
        if (isAdmin) {
            binding.fabAddProduct.visibility = View.VISIBLE
        } else {
            binding.fabAddProduct.visibility = View.GONE
        }

        setupRecyclerView(isAdmin)
        setupFilters()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView(isAdmin: Boolean) {
        productAdapter = ProductAdapter(isAdmin) { product ->
            val bundle = Bundle().apply { putInt("productId", product.id) }
            findNavController().navigate(R.id.action_navigation_inventory_to_productDetailFragment, bundle)
        }
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun setupFilters() {
        val categories = arrayOf("Semua", "Elektronik", "Pakaian", "Makanan/Minuman", "Aksesoris", "Lainnya")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        val sorts = arrayOf("Nama (A-Z)", "Nama (Z-A)", "Harga Terendah", "Harga Tertinggi", "Stok Terendah", "Stok Tertinggi")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sorts)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = sortAdapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setCategoryFilter(categories[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sortKey = when (position) {
                    0 -> "NAME_ASC"
                    1 -> "NAME_DESC"
                    2 -> "PRICE_ASC"
                    3 -> "PRICE_DESC"
                    4 -> "STOCK_ASC"
                    5 -> "STOCK_DESC"
                    else -> "NAME_ASC"
                }
                viewModel.setSortBy(sortKey)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.fabAddProduct.setOnClickListener {
            val bundle = Bundle().apply { putInt("productId", -1) }
            findNavController().navigate(R.id.action_navigation_inventory_to_addEditProductFragment, bundle)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productsFlow.collect { products ->
                productAdapter.submitList(products)
                if (products.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvProducts.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvProducts.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}