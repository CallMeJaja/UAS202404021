package com.uas202404021.stokify.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
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

    private const val PAGE_WIDTH = 595  // A4 width
    private const val PAGE_HEIGHT = 842 // A4 height
    private const val MARGIN_LEFT = 40f
    private const val MARGIN_RIGHT = 40f
    private const val TABLE_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT

    fun export(context: Context, products: List<ProductEntity>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = 0f
        var pageNum = 1

        // === Paints ===
        val brandPaint = Paint().apply {
            textSize = 22f
            color = Color.parseColor("#1B5E20")
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            textSize = 14f
            color = Color.parseColor("#2E7D32")
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            textSize = 10f
            color = Color.parseColor("#757575")
            isAntiAlias = true
        }

        val summaryLabelPaint = Paint().apply {
            textSize = 9f
            color = Color.parseColor("#9E9E9E")
            isAntiAlias = true
        }

        val summaryValuePaint = Paint().apply {
            textSize = 16f
            color = Color.parseColor("#1B5E20")
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            textSize = 9f
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val cellPaint = Paint().apply {
            textSize = 8.5f
            color = Color.parseColor("#212121")
            isAntiAlias = true
        }

        val cellBoldPaint = Paint().apply {
            textSize = 8.5f
            color = Color.parseColor("#212121")
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#2E7D32")
        }

        val rowEvenPaint = Paint().apply {
            color = Color.parseColor("#F1F8E9")
        }

        val rowOddPaint = Paint().apply {
            color = Color.WHITE
        }

        val borderPaint = Paint().apply {
            color = Color.parseColor("#C8E6C9")
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 0.3f
        }

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#2E7D32")
            strokeWidth = 2f
        }

        val footerPaint = Paint().apply {
            textSize = 8f
            color = Color.parseColor("#9E9E9E")
            isAntiAlias = true
        }

        // === HEADER ===
        // Brand bar
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 6f, Paint().apply { color = Color.parseColor("#2E7D32") })

        y = 40f
        canvas.drawText("STOKIFY", MARGIN_LEFT, y, brandPaint)
        y += 18f
        canvas.drawText("Laporan Inventaris", MARGIN_LEFT, y, titlePaint)
        y += 14f

        // Divider
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, dividerPaint)
        y += 14f

        // Date & time
        val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("in", "ID"))
        canvas.drawText("Tanggal: ${dateFormat.format(Date())}", MARGIN_LEFT, y, subtitlePaint)
        y += 20f

        // === SUMMARY CARDS ===
        val totalStock = products.sumOf { it.stock }
        val totalAsset = products.sumOf { it.stock * it.price }
        val criticalCount = products.filter { it.stock <= it.minStock && it.stock > 0 }.size
        val emptyCount = products.filter { it.stock == 0 }.size

        val cardWidth = (TABLE_WIDTH - 15f) / 4f
        val cardHeight = 42f
        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#F1F8E9")
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#C8E6C9")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val summaryItems = listOf(
            Triple("Total Produk", products.size.toString(), Color.parseColor("#2E7D32")),
            Triple("Total Stok", totalStock.toString(), Color.parseColor("#1565C0")),
            Triple("Stok Habis", "${criticalCount + emptyCount}", Color.parseColor("#E65100")),
            Triple("Total Aset", formatRupiah.format(totalAsset).removePrefix("Rp"), Color.parseColor("#6A1B9A"))
        )

        summaryItems.forEachIndexed { index, (label, value, color) ->
            val cardX = MARGIN_LEFT + index * (cardWidth + 5f)
            canvas.drawRoundRect(cardX, y, cardX + cardWidth, y + cardHeight, 6f, 6f, cardBgPaint)
            canvas.drawRoundRect(cardX, y, cardX + cardWidth, y + cardHeight, 6f, 6f, cardBorderPaint)

            val labelP = Paint(summaryLabelPaint)
            canvas.drawText(label, cardX + 8f, y + 14f, labelP)

            val valueP = Paint(summaryValuePaint).apply { this.color = color; textSize = 13f }
            canvas.drawText(value, cardX + 8f, y + 32f, valueP)
        }
        y += cardHeight + 20f

        // === TABLE ===
        // Column definitions: No, Nama, SKU, Kategori, Harga, Stok, Min, Lokasi, Status
        val colWidths = floatArrayOf(25f, 130f, 55f, 65f, 70f, 35f, 30f, 55f, 50f)
        val headers = arrayOf("No", "Nama Produk", "SKU", "Kategori", "Harga", "Stok", "Min", "Lokasi", "Status")

        fun drawTableHeader() {
            canvas.drawRect(MARGIN_LEFT, y, MARGIN_LEFT + TABLE_WIDTH, y + 18f, headerBgPaint)
            var x = MARGIN_LEFT + 4f
            headers.forEachIndexed { i, header ->
                canvas.drawText(header, x, y + 12f, headerPaint)
                x += colWidths[i]
            }
            y += 18f
        }

        fun checkNewPage(neededHeight: Float = 30f): Boolean {
            if (y + neededHeight > PAGE_HEIGHT - 40f) {
                // Footer
                canvas.drawText("Stokify - Laporan Inventaris", MARGIN_LEFT, PAGE_HEIGHT - 20f, footerPaint)
                canvas.drawText("Halaman $pageNum", PAGE_WIDTH - MARGIN_RIGHT - 60f, PAGE_HEIGHT - 20f, footerPaint)

                document.finishPage(page)
                pageNum++
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 40f

                // Header on new page
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 6f, Paint().apply { color = Color.parseColor("#2E7D32") })
                y = 25f
                canvas.drawText("STOKIFY - Laporan Inventaris (Lanjutan)", MARGIN_LEFT, y, subtitlePaint)
                y += 15f
                drawTableHeader()
                return true
            }
            return false
        }

        drawTableHeader()

        // === TABLE ROWS ===
        products.forEachIndexed { index, product ->
            checkNewPage(16f)

            val bgColor = if (index % 2 == 0) rowEvenPaint else rowOddPaint
            canvas.drawRect(MARGIN_LEFT, y, MARGIN_LEFT + TABLE_WIDTH, y + 16f, bgColor)

            val status = when {
                product.stock == 0 -> "Habis"
                product.stock <= product.minStock -> "Menipis"
                else -> "Aman"
            }
            val statusColor = when (status) {
                "Habis" -> Color.parseColor("#F44336")
                "Menipis" -> Color.parseColor("#FF9800")
                else -> Color.parseColor("#4CAF50")
            }

            val rowData = arrayOf(
                "${index + 1}",
                product.name.take(20),
                product.sku,
                product.category.take(10),
                formatRupiah.format(product.price),
                product.stock.toString(),
                product.minStock.toString(),
                product.location,
                status
            )

            var x = MARGIN_LEFT + 4f
            rowData.forEachIndexed { colIndex, data ->
                val paint = if (colIndex == 1) cellBoldPaint else cellPaint
                if (colIndex == 8) {
                    // Status column with color
                    val statusPaint = Paint(cellPaint).apply { color = statusColor; typeface = Typeface.DEFAULT_BOLD }
                    canvas.drawText(data, x, y + 11f, statusPaint)
                } else {
                    canvas.drawText(data, x, y + 11f, paint)
                }
                x += colWidths[colIndex]
            }

            // Subtle row border
            canvas.drawLine(MARGIN_LEFT, y + 16f, MARGIN_LEFT + TABLE_WIDTH, y + 16f, linePaint)
            y += 16f
        }

        // Table bottom border
        canvas.drawRect(MARGIN_LEFT, y - 16f * products.size - 18f, MARGIN_LEFT + TABLE_WIDTH, y, borderPaint)

        y += 15f

        // === FOOTER ===
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, dividerPaint)
        y += 12f

        val summaryText = "Total: ${products.size} produk | ${products.sumOf { it.stock }} stok | ${formatRupiah.format(totalAsset)}"
        canvas.drawText(summaryText, MARGIN_LEFT, y, subtitlePaint)
        y += 10f
        canvas.drawText("Dicetak pada ${dateFormat.format(Date())}", MARGIN_LEFT, y, subtitlePaint)

        // Page footer
        canvas.drawText("Stokify - Laporan Inventaris", MARGIN_LEFT, PAGE_HEIGHT - 20f, footerPaint)
        canvas.drawText("Halaman $pageNum", PAGE_WIDTH - MARGIN_RIGHT - 60f, PAGE_HEIGHT - 20f, footerPaint)

        document.finishPage(page)

        // === SAVE FILE ===
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
