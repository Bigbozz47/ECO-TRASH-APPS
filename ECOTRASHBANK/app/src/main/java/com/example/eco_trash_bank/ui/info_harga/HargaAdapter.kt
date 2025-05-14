package com.example.eco_trash_bank.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eco_trash_bank.R
import com.example.eco_trash_bank.model.HargaSampah

class HargaAdapter : RecyclerView.Adapter<HargaAdapter.HargaViewHolder>() {

    private val hargaList = mutableListOf<HargaSampah>()

    fun submitList(list: List<HargaSampah>) {
        hargaList.clear()
        hargaList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HargaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_harga_sampah, parent, false)
        return HargaViewHolder(view)
    }

    override fun onBindViewHolder(holder: HargaViewHolder, position: Int) {
        val item = hargaList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = hargaList.size

    inner class HargaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        private val tvHarga: TextView = itemView.findViewById(R.id.tvHarga)
        private val tvPoin: TextView = itemView.findViewById(R.id.tvPoin)

        fun bind(data: HargaSampah) {
            tvNama.text = data.jenis
            tvHarga.text = "Rp ${data.harga_per_kg.toInt()} /kg"
            tvPoin.text = "${data.poin_per_kg} poin /kg"
        }
    }
}
