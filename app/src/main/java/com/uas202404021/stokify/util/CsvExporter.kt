package com.uas202404021.stokify.util

import android.content.Context
import com.opencsv.CSVWriter
import com.uas202404021.stokify.data.local.db.ProductEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun export(context: Context, products: List<ProductEntity>): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "stokify_report_${dateFormat.format(Date())}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)

        CSVWriter(FileWriter(file)).use { writer ->
            // Header
            writer.writeNext(arrayOf("No", "Nama Produk", "SKU", "Kategori", "Harga", "Stok", "Min Stok", "Lokasi", "Status"))

            // Data
            products.forEachIndexed { index, product ->
                val status = when {
                    product.stock == 0 -> "Habis"
                    product.stock <= product.minStock -> "Menipis"
                    else -> "Aman"
                }
                writer.writeNext(
                    arrayOf(
                        (index + 1).toString(),
                        product.name,
                        product.sku,
                        product.category,
                        product.price.toString(),
                        product.stock.toString(),
                        product.minStock.toString(),
                        product.location,
                        status
                    )
                )
            }
        }

        return file
    }
}
