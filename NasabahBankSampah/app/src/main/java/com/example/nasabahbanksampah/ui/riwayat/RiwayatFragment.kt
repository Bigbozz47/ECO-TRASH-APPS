package com.example.nasabahbanksampah.ui.riwayat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nasabahbanksampah.databinding.FragmentRiwayatBinding

class RiwayatFragment : Fragment() {

    private var _binding: FragmentRiwayatBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RiwayatAdapter
    private lateinit var allItems: List<RiwayatItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        val view = binding.root

        allItems = listOf(
            RiwayatItem("poin", "Penukaran Poin", "Hari Ini", "900"),
            RiwayatItem("poin", "Penukaran Poin", "Senin, 6 Maret 2025", "850"),
            RiwayatItem("sampah", "Pengumpulan Sampah", "Rabu, 4 Februari 2025", "8 kg"),
            RiwayatItem("poin", "Penukaran Poin", "Rabu, 4 Januari 2024", "1000"),
            RiwayatItem("sampah", "Pengumpulan Sampah", "Rabu, 4 Januari 2024", "20 kg"),
            RiwayatItem("poin", "Penukaran Poin", "Senin, 5 Desember 2023", "800"),
            RiwayatItem("sampah", "Pengumpulan Sampah", "Senin, 5 Desember 2023", "10 kg")
        )

        adapter = RiwayatAdapter(allItems.toMutableList())
        binding.recyclerRiwayat.layoutManager = LinearLayoutManager(context)
        binding.recyclerRiwayat.adapter = adapter

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
