package com.example.nasabahbanksampah.ui.riwayat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nasabahbanksampah.R

data class RiwayatItem(
    val jenis: String,
    val deskripsi: String,
    val tanggal: String,
    val jumlah: String
)

class RiwayatAdapter(private val items: MutableList<RiwayatItem>) :
    RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.riwayatIcon)
        val deskripsi: TextView = view.findViewById(R.id.riwayatDeskripsi)
        val tanggal: TextView = view.findViewById(R.id.riwayatTanggal)
        val jumlah: TextView = view.findViewById(R.id.riwayatJumlah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.deskripsi.text = item.deskripsi
        holder.tanggal.text = item.tanggal
        holder.jumlah.text = item.jumlah

        val iconRes = when (item.jenis) {
            "poin" -> R.drawable.ic_coin
            "sampah" -> R.drawable.ic_sampah
            else -> R.drawable.ic_sampah // fallback pakai ikon sampah
        }

        holder.icon.setImageResource(iconRes)
    }

    override fun getItemCount() = items.size
    fun updateData(newItems: List<RiwayatItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}