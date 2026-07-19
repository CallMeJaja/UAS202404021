package com.uas202404021.stokify.presentation.ui.inventory

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.uas202404021.stokify.R
import com.uas202404021.stokify.data.local.db.AppDatabase
import com.uas202404021.stokify.data.local.db.ProductEntity
import com.uas202404021.stokify.data.local.pref.SessionManager
import com.uas202404021.stokify.data.repository.AppRepository
import com.uas202404021.stokify.databinding.FragmentProductDetailBinding
import com.uas202404021.stokify.presentation.adapter.HistoryAdapter
import com.uas202404021.stokify.presentation.viewmodel.InventoryResult
import com.uas202404021.stokify.presentation.viewmodel.InventoryViewModel
import com.uas202404021.stokify.presentation.viewmodel.InventoryViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.util.Locale

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var historyAdapter: HistoryAdapter
    private var currentProduct: ProductEntity? = null

    private val viewModel: InventoryViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext(), lifecycleScope)
        val repository = AppRepository(database.userDao(), database.productDao(), database.stockHistoryDao())
        InventoryViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        val isAdmin = sessionManager.getRole() == "Admin"

        val productId = arguments?.getInt("productId", -1) ?: -1
        if (productId == -1) {
            findNavController().popBackStack()
            return
        }

        setupHistoryRecyclerView()
        setupButtons(isAdmin)
        observeProduct(productId)
        observeHistory(productId)
        observeStockUpdateResult()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupHistoryRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun setupButtons(isAdmin: Boolean) {
        // Role control
        if (!isAdmin) {
            binding.btnEdit.visibility = View.GONE
            binding.btnDelete.visibility = View.GONE
            binding.btnIncrease.visibility = View.GONE
            binding.btnDecrease.visibility = View.GONE
        }

        binding.btnIncrease.setOnClickListener {
            currentProduct?.let { showStockDialog(it, "IN") }
        }

        binding.btnDecrease.setOnClickListener {
            currentProduct?.let { showStockDialog(it, "OUT") }
        }

        binding.btnEdit.setOnClickListener {
            currentProduct?.let { product ->
                val bundle = Bundle().apply { putInt("productId", product.id) }
                findNavController().navigate(R.id.action_productDetailFragment_to_addEditProductFragment, bundle)
            }
        }

        binding.btnDelete.setOnClickListener {
            currentProduct?.let { showDeleteConfirmation(it) }
        }
    }

    private fun observeProduct(productId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getProductById(productId).collect { product ->
                product?.let {
                    currentProduct = it
                    bindProductData(it)
                }
            }
        }
    }

    private fun bindProductData(product: ProductEntity) {
        binding.tvProductName.text = product.name
        binding.tvProductSku.text = "SKU: ${product.sku}"
        binding.tvProductCategory.text = "Kategori: ${product.category}"

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        binding.tvProductPrice.text = formatRupiah.format(product.price)

        binding.tvStockCount.text = product.stock.toString()
        binding.tvMinStock.text = "Min. Stok: ${product.minStock}"
        binding.tvLocation.text = "Lokasi: ${product.location}"

        // Status badge
        val badgeColor = when {
            product.stock > product.minStock -> {
                binding.tvStockStatus.text = "Aman"
                Color.parseColor("#4CAF50")
            }
            product.stock > 0 -> {
                binding.tvStockStatus.text = "Menipis"
                Color.parseColor("#FF9800")
            }
            else -> {
                binding.tvStockStatus.text = "Habis"
                Color.parseColor("#F44336")
            }
        }
        val badgeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f * resources.displayMetrics.density
            setColor(badgeColor)
        }
        binding.tvStockStatus.background = badgeDrawable

        // Load image
        val cornerRadius = 20f * resources.displayMetrics.density
        val shapeModel = binding.ivProductImage.shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(cornerRadius)
            .build()
        binding.ivProductImage.shapeAppearanceModel = shapeModel

        if (!product.imageUri.isNullOrEmpty()) {
            val uri = Uri.parse(product.imageUri)
            when (uri.scheme) {
                "android.resource" -> {
                    // Resource URI: android.resource://package/drawable/name
                    val resName = uri.lastPathSegment ?: ""
                    val resId = resources.getIdentifier(resName, "drawable", requireContext().packageName)
                    if (resId != 0) {
                        binding.ivProductImage.setImageResource(resId)
                    } else {
                        binding.ivProductImage.setImageResource(R.drawable.ic_inventory)
                    }
                }
                "file", "content" -> {
                    binding.ivProductImage.setImageURI(uri)
                }
                else -> {
                    // Try as file path
                    val path = uri.path
                    if (path != null && File(path).exists()) {
                        binding.ivProductImage.setImageURI(uri)
                    } else {
                        binding.ivProductImage.setImageResource(R.drawable.ic_inventory)
                    }
                }
            }
        } else {
            binding.ivProductImage.setImageResource(R.drawable.ic_inventory)
        }
    }

    private fun observeHistory(productId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getHistoryForProduct(productId).collect { historyList ->
                historyAdapter.submitList(historyList)
                binding.tvEmptyHistory.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
                binding.rvHistory.visibility = if (historyList.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun observeStockUpdateResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stockUpdateResult.collect { result ->
                when (result) {
                    is InventoryResult.Success -> {
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        viewModel.clearStockUpdateResult()
                    }
                    is InventoryResult.Error -> {
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                        viewModel.clearStockUpdateResult()
                    }
                    null -> {}
                }
            }
        }
    }

    private fun showStockDialog(product: ProductEntity, type: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_stock_update, null)

        val ivIcon = dialogView.findViewById<android.widget.ImageView>(R.id.ivStockDialogIcon)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvStockDialogTitle)
        val tvDesc = dialogView.findViewById<TextView>(R.id.tvStockDialogDesc)
        val tilAmount = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilStockAmount)
        val etAmount = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etStockAmount)
        val btnConfirm = dialogView.findViewById<MaterialButton>(R.id.btnStockDialogConfirm)

        val isIn = type == "IN"
        val accentColor = if (isIn) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")

        ivIcon.setImageResource(if (isIn) R.drawable.ic_add else R.drawable.ic_minus)
        ivIcon.setColorFilter(accentColor)
        tvTitle.text = if (isIn) "Stok Masuk" else "Stok Keluar"
        tvDesc.text = "Stok saat ini: ${product.stock}"
        tilAmount.helperText = if (isIn) "Jumlah yang akan ditambahkan" else "Jumlah yang akan dikurangi"
        btnConfirm.backgroundTintList = android.content.res.ColorStateList.valueOf(accentColor)
        btnConfirm.setTextColor(Color.WHITE)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .show()

        dialogView.findViewById<MaterialButton>(R.id.btnStockDialogCancel).setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val amountText = etAmount.text.toString()
            val amount = amountText.toIntOrNull()
            if (amount == null || amount <= 0) {
                tilAmount.error = "Masukkan jumlah yang valid"
            } else {
                tilAmount.error = null
                viewModel.quickStockUpdate(product, amount, type)
                dialog.dismiss()
            }
        }
    }

    private fun showDeleteConfirmation(product: ProductEntity) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_confirmation, null)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDeleteMessage)
        tvMessage.text = getString(R.string.dialog_confirm_delete_message, product.name)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .show()

        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
            viewModel.deleteProduct(product)
            Toast.makeText(requireContext(), getString(R.string.success_product_deleted), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
