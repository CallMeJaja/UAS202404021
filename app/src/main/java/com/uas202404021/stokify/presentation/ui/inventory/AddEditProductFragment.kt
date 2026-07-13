package com.uas202404021.stokify.presentation.ui.inventory

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
    private var cameraPhotoUri: Uri? = null

    private val viewModel: InventoryViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext(), lifecycleScope)
        val repository = AppRepository(database.userDao(), database.productDao(), database.stockHistoryDao())
        InventoryViewModelFactory(repository)
    }

    private val validateUseCase = ValidateProductUseCase()

    // Launcher untuk galeri
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val internalUri = copyImageToInternalStorage(it)
            selectedImageUri = internalUri
            binding.ivProductImage.setImageURI(internalUri)
        }
    }

    // Launcher untuk kamera
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && cameraPhotoUri != null) {
            selectedImageUri = cameraPhotoUri
            binding.ivProductImage.setImageURI(cameraPhotoUri)
        }
    }

    // Launcher untuk permission kamera
    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
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
            showImagePickerDialog()
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

        // Clear errors
        binding.tilSku.error = null
        binding.tilName.error = null
        binding.tilCategory.error = null
        binding.tilStock.error = null
        binding.tilMinStock.error = null
        binding.tilPrice.error = null
        binding.tilLocation.error = null

        // Inline validation per field
        var hasError = false

        val skuResult = validateUseCase.validateSku(sku)
        if (!skuResult.successful) { binding.tilSku.error = skuResult.errorMessage; hasError = true }

        val nameResult = validateUseCase.validateName(name)
        if (!nameResult.successful) { binding.tilName.error = nameResult.errorMessage; hasError = true }

        val categoryResult = validateUseCase.validateCategory(category)
        if (!categoryResult.successful) { binding.tilCategory.error = categoryResult.errorMessage; hasError = true }

        if (productId == -1) {
            val stockResult = validateUseCase.validateStock(stockText)
            if (!stockResult.successful) { binding.tilStock.error = stockResult.errorMessage; hasError = true }
        }

        val minStockResult = validateUseCase.validateMinStock(minStockText)
        if (!minStockResult.successful) { binding.tilMinStock.error = minStockResult.errorMessage; hasError = true }

        val priceResult = validateUseCase.validatePrice(priceText)
        if (!priceResult.successful) { binding.tilPrice.error = priceResult.errorMessage; hasError = true }

        val locationResult = validateUseCase.validateLocation(location)
        if (!locationResult.successful) { binding.tilLocation.error = locationResult.errorMessage; hasError = true }

        if (hasError) return

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

    private fun showImagePickerDialog() {
        val options = arrayOf("Ambil Foto", "Pilih dari Galeri")
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = File(requireContext().filesDir, "${UUID.randomUUID()}.jpg")
        cameraPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(cameraPhotoUri!!)
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