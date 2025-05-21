package com.example.eco_trash_bank.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eco_trash_bank.R
import com.example.eco_trash_bank.model.HargaSampah

class HargaAdapter(
    private val onEditClick: (HargaSampah) -> Unit,
    private val onDeleteClick: (HargaSampah) -> Unit
) : RecyclerView.Adapter<HargaAdapter.HargaViewHolder>() {

    private var hargaList: List<HargaSampah> = listOf()

    fun submitList(list: List<HargaSampah>) {
        hargaList = list
        notifyDataSetChanged()
    }

    /**
     * Jika ingin filter dan sort dari Adapter (opsional, direkomendasikan lewat ViewModel saja),
     * gunakan fungsi ini. Tetapi lebih baik ViewModel yang filter, adapter cukup submitList!
     */
    fun filterAndSort(query: String, sortBy: String = "", aktifOnly: Boolean = false) {
        var filtered = hargaList.filter {
            it.jenis.contains(query, ignoreCase = true)
        }
        if (aktifOnly) {
            filtered = filtered.filter { it.is_active }
        }
        hargaList = when (sortBy) {
            "Nama" -> filtered.sortedBy { it.jenis.lowercase() }
            "Harga" -> filtered.sortedByDescending { it.harga_per_kg }
            "Poin" -> filtered.sortedByDescending { it.poin_per_kg }
            else -> filtered
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HargaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_harga, parent, false)
        return HargaViewHolder(view)
    }

    override fun onBindViewHolder(holder: HargaViewHolder, position: Int) {
        holder.bind(hargaList[position])
    }

    override fun getItemCount(): Int = hargaList.size

    inner class HargaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvNama: TextView = view.findViewById(R.id.tvJenis)
        private val tvHarga: TextView = view.findViewById(R.id.tvHarga)
        private val tvPoin: TextView = view.findViewById(R.id.tvPoin)
        private val btnEdit: Button = view.findViewById(R.id.btnEdit)
        private val btnDelete: Button = view.findViewById(R.id.btnDelete)

        fun bind(item: HargaSampah) {
            tvNama.text = item.jenis
            tvHarga.text = "Rp ${item.harga_per_kg.toInt()} /kg"
            tvPoin.text = "${item.poin_per_kg} poin /kg"
            btnEdit.setOnClickListener { onEditClick(item) }
            btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }
}
