package com.uas202404021.stokify.presentation.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uas202404021.stokify.R
import com.uas202404021.stokify.data.local.db.ProductEntity
import com.uas202404021.stokify.databinding.ItemProductBinding
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val isAdmin: Boolean,
    private val onItemClick: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, ProductAdapter.ProductViewHolder>(DiffCallback) {

    inner class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductEntity) {
            binding.tvProductName.text = product.name
            binding.tvProductSkuCategory.text = "${product.sku} • ${product.category}"
            binding.tvProductLocation.text = product.location
            
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvProductPrice.text = formatRupiah.format(product.price)

            // Mengatur Status Stok dan Warna
            binding.tvStockStatus.text = "Stok: ${product.stock}"
            val badgeColor = when {
                product.stock > product.minStock -> Color.parseColor("#4CAF50")
                product.stock > 0 -> Color.parseColor("#FF9800")
                else -> Color.parseColor("#F44336")
            }
            val badgeDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f * binding.root.resources.displayMetrics.density
                setColor(badgeColor)
            }
            binding.tvStockStatus.background = badgeDrawable

            // Memuat gambar (jika ada)
            if (!product.imageUri.isNullOrEmpty()) {
                try {
                    binding.ivProductImage.setImageURI(Uri.parse(product.imageUri))
                } catch (e: Exception) {
                    binding.ivProductImage.setImageResource(R.drawable.ic_inventory)
                }
            } else {
                binding.ivProductImage.setImageResource(R.drawable.ic_inventory)
            }

            // Click listener
            binding.root.setOnClickListener {
                onItemClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ProductEntity>() {
            override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}