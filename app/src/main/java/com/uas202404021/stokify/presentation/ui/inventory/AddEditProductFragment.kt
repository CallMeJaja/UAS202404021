package com.uas202404021.stokify.presentation.ui.inventory

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.uas202404021.stokify.R
import com.uas202404021.stokify.data.local.db.AppDatabase
import com.uas202404021.stokify.data.local.db.ProductEntity
import com.uas202404021.stokify.data.repository.AppRepository
import com.uas202404021.stokify.databinding.FragmentAddEditProductBinding
import com.uas202404021.stokify.domain.usecase.ValidateProductUseCase
import com.uas202404021.stokify.presentation.viewmodel.InventoryViewModel
import com.uas202404021.stokify.presentation.viewmodel.InventoryViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class AddEditProductFragment : Fragment() {

    private var _binding: FragmentAddEditProductBinding? = null
    private val binding get() = _binding!!

    private var productId: Int = -1
    private var selectedImageUri: Uri? = null

    private val viewModel: InventoryViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext(), lifecycleScope)
        val repository = AppRepository(database.userDao(), database.productDao(), database.stockHistoryDao())
        InventoryViewModelFactory(repository)
    }

    private val validateUseCase = ValidateProductUseCase()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val internalUri = copyImageToInternalStorage(it)
            selectedImageUri = internalUri
            binding.ivProductImage.setImageURI(internalUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            productId = it.getInt("productId", -1)
        }

        setupDropdown()
        setupListeners()

        if (productId != -1) {
            binding.tvTitle.text = "Edit Produk"
            binding.tilStock.isEnabled = false // Stok tidak bisa diedit langsung via form
            binding.tilStock.helperText = "Gunakan fitur pembaruan stok cepat di halaman detail"
            loadProductData()
        }
    }

    private fun setupDropdown() {
        val categories = arrayOf("Elektronik", "Pakaian", "Makanan/Minuman", "Aksesoris", "Lainnya")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actCategory.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.cvImagePicker.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveProduct()
        }
    }

    private fun loadProductData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getProductById(productId).collect { product ->
                product?.let {
                    binding.etSku.setText(it.sku)
                    binding.etName.setText(it.name)
                    binding.actCategory.setText(it.category, false)
                    binding.etStock.setText(it.stock.toString())
                    binding.etMinStock.setText(it.minStock.toString())
                    binding.etPrice.setText(it.price.toString())
                    binding.etLocation.setText(it.location)

                    if (!it.imageUri.isNullOrEmpty()) {
                        selectedImageUri = Uri.parse(it.imageUri)
                        binding.ivProductImage.setImageURI(selectedImageUri)
                    }
                }
            }
        }
    }

    private fun saveProduct() {
        val sku = binding.etSku.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val category = binding.actCategory.text.toString().trim()
        val stockText = binding.etStock.text.toString().trim()
        val minStockText = binding.etMinStock.text.toString().trim()
        val priceText = binding.etPrice.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        if (!validateUseCase.validateSku(sku).successful ||
            !validateUseCase.validateName(name).successful ||
            !validateUseCase.validateCategory(category).successful ||
            (!validateUseCase.validateStock(stockText).successful && productId == -1) ||
            !validateUseCase.validateMinStock(minStockText).successful ||
            !validateUseCase.validatePrice(priceText).successful ||
            !validateUseCase.validateLocation(location).successful
        ) {
            Toast.makeText(requireContext(), "Harap periksa kembali semua inputan", Toast.LENGTH_SHORT).show()
            return
        }

        val product = ProductEntity(
            id = if (productId == -1) 0 else productId,
            sku = sku,
            name = name,
            category = category,
            stock = if (productId == -1) stockText.toInt() else binding.etStock.text.toString().toInt(),
            minStock = minStockText.toInt(),
            price = priceText.toDouble(),
            imageUri = selectedImageUri?.toString(),
            location = location
        )

        if (productId == -1) {
            viewModel.insertProduct(product)
            Toast.makeText(requireContext(), "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.updateProduct(product)
            Toast.makeText(requireContext(), "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show()
        }

        findNavController().popBackStack()
    }

    private fun copyImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val fileName = "${UUID.randomUUID()}.jpg"
            val file = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(file)
            
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}