package com.uas202404021.stokify.presentation.ui.report

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.uas202404021.stokify.R
import com.uas202404021.stokify.data.local.db.AppDatabase
import com.uas202404021.stokify.data.local.db.ProductEntity
import com.uas202404021.stokify.data.repository.AppRepository
import com.uas202404021.stokify.databinding.FragmentReportBinding
import com.uas202404021.stokify.presentation.viewmodel.ReportViewModel
import com.uas202404021.stokify.presentation.viewmodel.ReportViewModelFactory
import com.uas202404021.stokify.util.CsvExporter
import com.uas202404021.stokify.util.PdfGenerator
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var horizontalBarChart: HorizontalBarChart

    private val viewModel: ReportViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext(), lifecycleScope)
        val repository = AppRepository(database.userDao(), database.productDao(), database.stockHistoryDao())
        ReportViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCharts()
        setupExportButtons()
        observeData()
    }

    private fun setupCharts() {
        pieChart = binding.pieChartStockStatus
        barChart = binding.barChartCategory
        horizontalBarChart = binding.horizontalBarChartLowStock

        val nightModeFlags = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isDarkMode = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val labelColor = if (isDarkMode) Color.parseColor("#E5E1E7") else Color.parseColor("#1C1B1F")
        val gridColor = if (isDarkMode) Color.parseColor("#363539") else Color.parseColor("#E0E0E0")

        // Pie Chart Configuration
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(labelColor)
            centerText = "Status Stok"
            setCenterTextSize(16f)
            setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            setCenterTextColor(labelColor)
            isDrawHoleEnabled = true
            holeRadius = 40f
            transparentCircleRadius = 45f
            setDrawEntryLabels(true)
            legend.isEnabled = true
            legend.textSize = 12f
            legend.textColor = labelColor
            setNoDataText("Belum ada data")
            setNoDataTextColor(labelColor)
        }

        // Bar Chart Configuration
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setFitBars(true)
            legend.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.textColor = labelColor
            axisLeft.textColor = labelColor
            axisLeft.gridColor = gridColor
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            setNoDataText("Belum ada data")
            setNoDataTextColor(labelColor)
        }

        // Horizontal Bar Chart Configuration
        horizontalBarChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            legend.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.textColor = labelColor
            axisLeft.textColor = labelColor
            axisLeft.gridColor = gridColor
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            setNoDataText("Belum ada data")
            setNoDataTextColor(labelColor)
        }
    }

    private fun setupExportButtons() {
        binding.btnExportPdf.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val products = viewModel.allProducts.value
                    if (products.isEmpty()) {
                        Toast.makeText(requireContext(), "Tidak ada data untuk diexport", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val file = PdfGenerator.export(requireContext(), products)
                    shareFile(file, "application/pdf")
                    Toast.makeText(requireContext(), getString(R.string.export_success), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "${getString(R.string.export_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnExportCsv.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val products = viewModel.allProducts.value
                    if (products.isEmpty()) {
                        Toast.makeText(requireContext(), "Tidak ada data untuk diexport", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val file = CsvExporter.export(requireContext(), products)
                    shareFile(file, "text/csv")
                    Toast.makeText(requireContext(), getString(R.string.export_success), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "${getString(R.string.export_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun shareFile(file: java.io.File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = mimeType
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(android.content.Intent.createChooser(shareIntent, getString(R.string.share_report)))
    }

    private fun observeData() {
        // Set report date
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("in", "ID"))
        binding.tvReportDate.text = getString(R.string.report_date, dateFormat.format(Date()))

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allProducts.collect { products ->
                // Update summary cards
                binding.tvTotalProducts.text = products.size.toString()
                binding.tvTotalStock.text = viewModel.getTotalStock(products).toString()

                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                binding.tvTotalAsset.text = formatRupiah.format(products.sumOf { it.stock * it.price })

                // Update critical stock
                val lowStockProducts = products.filter { it.stock <= it.minStock }
                binding.tvCriticalStock.text = lowStockProducts.size.toString()

                // Update Pie Chart
                updatePieChart(products)

                // Update Bar Chart
                updateBarChart(products)

                // Update Horizontal Bar Chart
                updateHorizontalBarChart(products)
            }
        }
    }

    private fun updatePieChart(products: List<com.uas202404021.stokify.data.local.db.ProductEntity>) {
        val status = viewModel.getStockStatus(products)

        val entries = mutableListOf<PieEntry>()
        if (status.safe > 0) entries.add(PieEntry(status.safe.toFloat(), "Aman"))
        if (status.low > 0) entries.add(PieEntry(status.low.toFloat(), "Menipis"))
        if (status.empty > 0) entries.add(PieEntry(status.empty.toFloat(), "Habis"))

        if (entries.isEmpty()) {
            pieChart.clear()
            return
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#4CAF50"), // Green - Safe
                Color.parseColor("#FF9800"), // Orange - Low
                Color.parseColor("#F44336")  // Red - Empty
            )
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            valueFormatter = com.github.mikephil.charting.formatter.PercentFormatter(pieChart)
        }

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }

    private fun updateBarChart(products: List<com.uas202404021.stokify.data.local.db.ProductEntity>) {
        val categories = viewModel.getCategoryDistribution(products)

        if (categories.isEmpty()) {
            barChart.clear()
            return
        }

        val entries = categories.mapIndexed { index, category ->
            BarEntry(index.toFloat(), category.count.toFloat())
        }

        val labels = categories.map { it.category }

        val dataSet = BarDataSet(entries, "Kategori").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
        }

        barChart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            xAxis.labelRotationAngle = -45f
            animateY(1000)
            invalidate()
        }
    }

    private fun updateHorizontalBarChart(products: List<com.uas202404021.stokify.data.local.db.ProductEntity>) {
        val lowStockProducts = viewModel.getTopLowStockProducts(products)

        if (lowStockProducts.isEmpty()) {
            horizontalBarChart.clear()
            return
        }

        val entries = lowStockProducts.mapIndexed { index, product ->
            BarEntry(index.toFloat(), product.stock.toFloat())
        }

        val labels = lowStockProducts.map { it.name }

        val dataSet = BarDataSet(entries, "Stok").apply {
            colors = lowStockProducts.map { product ->
                when {
                    product.stock == 0 -> Color.parseColor("#F44336") // Red
                    product.stock <= product.minStock -> Color.parseColor("#FF9800") // Orange
                    else -> Color.parseColor("#4CAF50") // Green
                }
            }
            valueTextSize = 12f
        }

        horizontalBarChart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
