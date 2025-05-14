package com.example.eco_trash_bank.ui.validation.adapter

import SetoranModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eco_trash_bank.R

class ValidasiAdapter(
    private var list: List<SetoranModel>,
    private val onValidasi: (SetoranModel) -> Unit,
    private val onTransfer: (SetoranModel) -> Unit
) : RecyclerView.Adapter<ValidasiAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nama = view.findViewById<TextView>(R.id.tvNasabahName)
        private val jumlah = view.findViewById<TextView>(R.id.tvJumlahSampah)
        private val kategori = view.findViewById<TextView>(R.id.tvKategori)
        private val poin = view.findViewById<TextView>(R.id.tvPoin)
        private val btnValidasi = view.findViewById<Button>(R.id.btnValidasi)
        private val btnTransfer = view.findViewById<Button>(R.id.btnTransfer)

        fun bind(setoran: SetoranModel) {
            nama.text = setoran.nama_nasabah
            jumlah.text = "Jumlah Sampah: ${setoran.jumlah_sampah} kg"
            kategori.text = "Kategori: ${setoran.kategori}"
            poin.text = "Poin: ${setoran.poin}"

            btnValidasi.setOnClickListener { onValidasi(setoran) }
            btnTransfer.setOnClickListener { onTransfer(setoran) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_validasi_setoran, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    // Tambahkan fungsi ini agar bisa dipanggil dari Fragment
    fun updateData(newList: List<SetoranModel>) {
        list = newList
        notifyDataSetChanged()
    }
}
