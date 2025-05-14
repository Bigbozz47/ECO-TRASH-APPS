package com.example.eco_trash_bank.ui.laporan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eco_trash_bank.databinding.ItemRiwayatBinding

class RiwayatAdapter(private val list: MutableList<RiwayatItem>) :
    RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRiwayatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RiwayatItem) {
            binding.namaTextView.text = item.nama
            binding.totalSetoranTextView.text = "Total Setoran: ${item.totalSetoran} Kg"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    /** Fungsi untuk mengupdate data */
    fun updateData(newList: List<RiwayatItem>) {
        (list as MutableList).clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

}
