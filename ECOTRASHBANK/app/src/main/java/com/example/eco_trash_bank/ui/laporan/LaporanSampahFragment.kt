package com.example.eco_trash_bank.ui.laporan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eco_trash_bank.databinding.FragmentLaporanSampahBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class LaporanSampahFragment : Fragment() {

    private var _binding: FragmentLaporanSampahBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LaporanSampahViewModel
    private var adapter: RiwayatAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanSampahBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[LaporanSampahViewModel::class.java]

        // === Observasi Profil ===
        viewModel.username.observe(viewLifecycleOwner) {
            binding.userName.text = it
        }

        viewModel.role.observe(viewLifecycleOwner) {
            binding.userStatus.text = "• ${it.replaceFirstChar { c -> c.uppercase() }}"
        }

        // === Observasi Loading & Error ===
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.loadingIndicator.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show() }
        }

        // === Ringkasan Jenis (BarChart) ===
        viewModel.ringkasanJenis.observe(viewLifecycleOwner) { data ->
            if (data.isEmpty()) {
                // Fallback dummy jika kosong
                viewModel.loadDummyData()
                return@observe
            }

            val entries = data.mapIndexed { index, item ->
                BarEntry(index.toFloat(), item.totalKg.toFloat())
            }
            val labels = data.map { it.jenis }

            val dataSet = BarDataSet(entries, "Jenis Sampah")
            dataSet.valueTextSize = 12f
            val barData = BarData(dataSet)

            binding.barChart.apply {
                this.data = barData
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                axisRight.isEnabled = false
                description.isEnabled = false
                invalidate()
            }
        }

        // === Ringkasan Bulanan (LineChart) ===
        viewModel.ringkasanBulanan.observe(viewLifecycleOwner) { data ->
            if (data.isEmpty()) {
                // Fallback dummy jika kosong
                viewModel.loadDummyData()
                return@observe
            }

            val entries = data.mapIndexed { index, item ->
                Entry(index.toFloat(), item.total.toFloat())
            }
            val labels = data.map { it.month }

            val dataSet = LineDataSet(entries, "Setoran Bulanan (Kg)")
            dataSet.valueTextSize = 12f
            dataSet.setDrawCircles(true)

            val lineData = LineData(dataSet)

            binding.lineChart.apply {
                this.data = lineData
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                axisRight.isEnabled = false
                description.isEnabled = false
                invalidate()
            }
        }

        // === Riwayat RecyclerView ===
        viewModel.riwayatNasabah.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                // Fallback dummy jika kosong
                viewModel.loadDummyData()
                return@observe
            }

            if (adapter == null) {
                adapter = RiwayatAdapter(list.toMutableList())
                binding.riwayatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.riwayatRecyclerView.adapter = adapter
            } else {
                adapter?.updateData(list)
            }
        }

        // === Tombol PDF dari Backend ===
        binding.btnGeneratePdf.setOnClickListener {
            viewModel.exportLaporanPDF(requireContext())
        }

        // === Tombol Export Chart ke PDF ===
        binding.btnExportChartPdf.setOnClickListener {
            if (binding.barChart.width == 0 || binding.lineChart.height == 0) {
                Toast.makeText(requireContext(), "Tunggu grafik selesai dimuat", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveChartsAsPdf(binding.barChart, binding.lineChart, requireContext())
        }

        // === Fetch Data dari Backend ===
        viewModel.fetchUserProfile(requireContext())
        viewModel.fetchRingkasanJenis(requireContext())
        viewModel.fetchRingkasanBulanan(requireContext())
        viewModel.fetchRiwayatNasabah(requireContext())

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter = null
    }
}
