package com.example.eco_trash_bank.ui.list_nasabah

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eco_trash_bank.databinding.ItemPenukaranBinding
import com.example.eco_trash_bank.models.Penukaran

class PenukaranAdapter(
    private val list: List<Penukaran>
) : RecyclerView.Adapter<PenukaranAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPenukaranBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Penukaran) {
            binding.tvPoin.text = item.poin
            binding.tvJenis.text = item.jenis
            binding.tvTanggal.text = item.tanggal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPenukaranBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size
}
