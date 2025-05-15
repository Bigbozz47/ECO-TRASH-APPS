package com.example.nasabahbanksampah.ui.inputsampah

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nasabahbanksampah.databinding.ItemJenisSampahBinding

class JenisSampahAdapter(private val items: List<String>) :
    RecyclerView.Adapter<JenisSampahAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<String>()

    inner class ViewHolder(val binding: ItemJenisSampahBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.checkbox.text = item
            binding.checkbox.isChecked = selectedItems.contains(item)
            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedItems.add(item)
                else selectedItems.remove(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJenisSampahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun getSelectedItems(): List<String> = selectedItems.toList()
}
