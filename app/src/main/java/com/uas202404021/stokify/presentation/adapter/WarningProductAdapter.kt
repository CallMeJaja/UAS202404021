package com.uas202404021.stokify.presentation.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uas202404021.stokify.data.local.db.ProductEntity
import com.uas202404021.stokify.databinding.ItemProductWarningBinding

class WarningProductAdapter(
    private val onItemClick: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, WarningProductAdapter.WarningViewHolder>(DiffCallback) {

    inner class WarningViewHolder(private val binding: ItemProductWarningBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductEntity) {
            binding.tvWarningName.text = product.name
            binding.tvWarningSku.text = product.sku
            binding.tvWarningStock.text = "Stok: ${product.stock} / Min: ${product.minStock}"

            // Set badge text and color based on stock level
            val (badgeText, badgeColor) = when {
                product.stock == 0 -> "Habis" to Color.parseColor("#F44336")
                else -> "Menipis" to Color.parseColor("#FF9800")
            }
            binding.tvWarningBadge.text = badgeText

            val badgeDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f * binding.root.resources.displayMetrics.density
                setColor(badgeColor)
            }
            binding.tvWarningBadge.background = badgeDrawable

            // Set card stroke color based on stock level
            val cardStrokeColor = if (product.stock == 0) Color.parseColor("#F44336") else Color.parseColor("#FF9800")
            binding.root.strokeColor = cardStrokeColor

            // Set stock text color based on stock level
            binding.tvWarningStock.setTextColor(badgeColor)

            binding.root.setOnClickListener { onItemClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarningViewHolder {
        val binding = ItemProductWarningBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WarningViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WarningViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity) = oldItem == newItem
    }
}
