package com.example.eco_trash_bank.ui.laporan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eco_trash_bank.databinding.ItemLaporanDownloadBinding


data class LaporanDownloadItem(
    val id: Int,
    val admin: AdminInfo,
    val file_path: String,
    val tanggal_unduh: String
)

data class AdminInfo(
    val username: String
)

class LaporanDownloadAdapter(private val list: List<LaporanDownloadItem>) :
    RecyclerView.Adapter<LaporanDownloadAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLaporanDownloadBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LaporanDownloadItem) {
            binding.tvAdminName.text = "Admin: ${item.admin.username}"
            binding.tvTanggal.text = "Tanggal: ${item.tanggal_unduh.take(10)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLaporanDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}
