package com.uas202404021.stokify.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.uas202404021.stokify.data.local.db.ProductEntity
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    private val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    fun export(context: Context, products: List<ProductEntity>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var yPosition = 40f

        val titlePaint = Paint().apply {
            textSize = 24f
            color = Color.parseColor("#006E1C")
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            textSize = 12f
            color = Color.GRAY
        }

        val headerPaint = Paint().apply {
            textSize = 10f
            color = Color.WHITE
            isFakeBoldText = true
        }

        val cellPaint = Paint().apply {
            textSize = 9f
            color = Color.BLACK
        }

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#006E1C")
        }

        val rowBgPaint = Paint().apply {
            color = Color.parseColor("#F5F5F5")
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 0.5f
        }

        // Title
        canvas.drawText("Laporan Inventaris Stokify", 40f, yPosition, titlePaint)
        yPosition += 25f

        // Date
        val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("in", "ID"))
        canvas.drawText("Tanggal: ${dateFormat.format(Date())}", 40f, yPosition, subtitlePaint)
        yPosition += 15f

        // Summary
        val totalStock = products.sumOf { it.stock }
        val totalAsset = products.sumOf { it.stock * it.price }
        canvas.drawText("Total Produk: ${products.size} | Total Stok: $totalStock | Total Aset: ${formatRupiah.format(totalAsset)}", 40f, yPosition, subtitlePaint)
        yPosition += 30f

        // Table Header
        val colWidths = floatArrayOf(30f, 120f, 60f, 70f, 70f, 45f, 45f, 70f, 50f)
        val headers = arrayOf("No", "Nama Produk", "SKU", "Kategori", "Harga", "Stok", "Min", "Lokasi", "Status")
        val tableLeft = 30f

        // Draw header background
        canvas.drawRect(tableLeft, yPosition, tableLeft + colWidths.sum(), yPosition + 20f, headerBgPaint)

        // Draw header text
        var xPosition = tableLeft + 5f
        headers.forEachIndexed { index, header ->
            canvas.drawText(header, xPosition, yPosition + 14f, headerPaint)
            xPosition += colWidths[index]
        }
        yPosition += 20f

        // Draw rows
        products.forEachIndexed { index, product ->
            // Check if we need a new page
            if (yPosition > 800f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 40f

                // Redraw header on new page
                canvas.drawRect(tableLeft, yPosition, tableLeft + colWidths.sum(), yPosition + 20f, headerBgPaint)
                xPosition = tableLeft + 5f
                headers.forEachIndexed { hIndex, header ->
                    canvas.drawText(header, xPosition, yPosition + 14f, headerPaint)
                    xPosition += colWidths[hIndex]
                }
                yPosition += 20f
            }

            // Alternate row background
            if (index % 2 == 0) {
                canvas.drawRect(tableLeft, yPosition, tableLeft + colWidths.sum(), yPosition + 18f, rowBgPaint)
            }

            val status = when {
                product.stock == 0 -> "Habis"
                product.stock <= product.minStock -> "Menipis"
                else -> "Aman"
            }

            val rowData = arrayOf(
                (index + 1).toString(),
                product.name.take(18),
                product.sku,
                product.category,
                formatRupiah.format(product.price),
                product.stock.toString(),
                product.minStock.toString(),
                product.location,
                status
            )

            xPosition = tableLeft + 5f
            rowData.forEachIndexed { colIndex, data ->
                canvas.drawText(data, xPosition, yPosition + 13f, cellPaint)
                xPosition += colWidths[colIndex]
            }

            // Draw bottom line
            canvas.drawLine(tableLeft, yPosition + 18f, tableLeft + colWidths.sum(), yPosition + 18f, linePaint)
            yPosition += 18f
        }

        document.finishPage(page)

        val dateFormat2 = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "stokify_report_${dateFormat2.format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }

        document.close()
        return file
    }
}
