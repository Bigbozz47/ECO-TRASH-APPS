package com.example.eco_trash_bank.ui.list_nasabah

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eco_trash_bank.databinding.ItemKontribusiBinding
import com.example.eco_trash_bank.models.Kontribusi

class KontribusiAdapter(
    private val list: List<Kontribusi>
) : RecyclerView.Adapter<KontribusiAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemKontribusiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Kontribusi) {
            binding.tvJenis.text = item.jenis
            binding.tvBerat.text = "${item.berat} kg"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKontribusiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size
}
