package com.uas202404021.stokify.presentation.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uas202404021.stokify.data.local.db.StockHistoryEntity
import com.uas202404021.stokify.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<StockHistoryEntity, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(history: StockHistoryEntity) {
            val isIncrease = history.type == "IN"

            // Badge tipe
            binding.tvHistoryType.text = history.type
            val badgeBg = binding.tvHistoryType.background as GradientDrawable
            badgeBg.setColor(if (isIncrease) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))

            // Teks jumlah
            val prefix = if (isIncrease) "+" else "-"
            binding.tvHistoryChange.text = "$prefix${history.changeAmount}"
            binding.tvHistoryChange.setTextColor(if (isIncrease) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))

            // Waktu
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
            binding.tvHistoryTimestamp.text = sdf.format(Date(history.timestamp))

            // Catatan
            binding.tvHistoryNote.text = if (isIncrease) "Stok masuk" else "Stok keluar"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<StockHistoryEntity>() {
        override fun areItemsTheSame(oldItem: StockHistoryEntity, newItem: StockHistoryEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StockHistoryEntity, newItem: StockHistoryEntity) = oldItem == newItem
    }
}
