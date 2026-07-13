package com.uas202404021.stokify.presentation.adapter

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

            // Set badge color to red
            val badgeBg = binding.tvWarningBadge.background
            if (badgeBg is GradientDrawable) {
                badgeBg.setColor(android.graphics.Color.parseColor("#F44336"))
            }

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
