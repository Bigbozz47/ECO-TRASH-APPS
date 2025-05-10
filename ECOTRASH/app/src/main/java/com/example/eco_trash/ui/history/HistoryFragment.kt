package com.example.eco_trash.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eco_trash.databinding.FragmentHistoryBinding
import com.example.nasabahbanksampah.ui.history.HistoryAdapter
import com.example.nasabahbanksampah.ui.history.HistoryItem

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HistoryAdapter
    private lateinit var allItems: List<HistoryItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val view = binding.root

        allItems = listOf(
            HistoryItem("poin", "Penukaran Poin", "Hari Ini", "900"),
            HistoryItem("poin", "Penukaran Poin", "Senin, 6 Maret 2025", "850"),
            HistoryItem("sampah", "Pengumpulan Sampah", "Rabu, 4 Februari 2025", "8 kg"),
            HistoryItem("poin", "Penukaran Poin", "Rabu, 4 Januari 2024", "1000"),
            HistoryItem("sampah", "Pengumpulan Sampah", "Rabu, 4 Januari 2024", "20 kg"),
            HistoryItem("poin", "Penukaran Poin", "Senin, 5 Desember 2023", "800"),
            HistoryItem("sampah", "Pengumpulan Sampah", "Senin, 5 Desember 2023", "10 kg")
        )

        adapter = HistoryAdapter(allItems.toMutableList())
        binding.recyclerHistory.layoutManager = LinearLayoutManager(context)
        binding.recyclerHistory.adapter = adapter

        // Tombol filter
        binding.btnSemua.setOnClickListener {
            adapter.updateData(allItems.toMutableList())
        }

        binding.btnPoin.setOnClickListener {
            val filtered = allItems.filter { it.jenis == "poin" }
            adapter.updateData(filtered.toMutableList())
        }

        binding.btnSampah.setOnClickListener {
            val filtered = allItems.filter { it.jenis == "sampah" }
            adapter.updateData(filtered.toMutableList())
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}