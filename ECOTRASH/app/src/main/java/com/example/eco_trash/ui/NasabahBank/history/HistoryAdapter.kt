package com.example.nasabahbanksampah.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eco_trash.R

data class HistoryItem(
    val jenis: String,
    val deskripsi: String,
    val tanggal: String,
    val jumlah: String
)

class HistoryAdapter(private val items: MutableList<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.historyIcon)
        val deskripsi: TextView = view.findViewById(R.id.historyDeskripsi)
        val tanggal: TextView = view.findViewById(R.id.historyTanggal)
        val jumlah: TextView = view.findViewById(R.id.historyJumlah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.deskripsi.text = item.deskripsi
        holder.tanggal.text = item.tanggal
        holder.jumlah.text = item.jumlah

        // Ganti ikon berdasarkan jenis
        val iconRes = when (item.jenis) {
            "poin" -> R.drawable.ic_coin
            "sampah" -> R.drawable.ic_sampah
            else -> R.drawable.ic_sampah // fallback pakai ikon sampah
        }

        holder.icon.setImageResource(iconRes)
    }

    override fun getItemCount() = items.size
    fun updateData(newItems: List<HistoryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}