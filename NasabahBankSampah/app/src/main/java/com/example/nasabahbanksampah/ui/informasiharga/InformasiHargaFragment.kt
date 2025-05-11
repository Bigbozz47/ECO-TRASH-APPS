package com.example.nasabahbanksampah.ui.informasiharga

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.nasabahbanksampah.R

class InformasiHargaFragment : Fragment() {

    private lateinit var tabOrganik: TextView
    private lateinit var tabAnorganik: TextView
    private lateinit var hargaContainer: LinearLayout

    private val dummyHargaOrganik = listOf(
        Triple("Buah", "Rp 300 - Rp 1200 / kg", "1 poin / 2 kg"),
        Triple("Sayur", "Rp 200 - Rp 1000 / kg", "1 poin / 2 kg"),
        Triple("Sisa Makanan", "Rp 150 - Rp 900 / kg", "1 poin / 3 kg"),
        Triple("Kayu", "Rp 500 - Rp 2500 / kg", "1 poin / kg"),
        Triple("Daun", "Rp 100 - Rp 800 / kg", "1 poin / 3 kg"),
        Triple("Ranting", "Rp 200 - Rp 1000 / kg", "1 poin / 2 kg"),
        Triple("Ranting", "Rp 200 - Rp 1000 / kg", "1 poin / 2 kg"),
    )

    private val subKategoriAnorganik = listOf("Plastik", "Kertas", "Logam", "Elektronik", "Kaca & Minyak")
    private val dummyHargaAnorganik = mapOf(
        "Plastik" to listOf(
            Triple("Botol Plastik Bening / Pet Bening", "Rp 2000 - Rp 5000 / kg", "40 poin / kg"),
            Triple("Botol Plastik Biru Muda", "Rp 1000 - Rp 2500 / kg", "20 poin / kg"),
            Triple("Botol Plastik Warna / Pet Warna", "Rp 1000 - Rp 2000 / kg", "15 poin / kg"),
            Triple("Botol Kecap", "Rp 1000 - Rp 1500 / kg", "12 poin / kg"),
            Triple("Tutup Botol / Botol Sampo / KW1", "Rp 2000 - Rp 4500 / kg", "35 poin / kg"),
            Triple("Karah Warna", "Rp 1500 - Rp 3000 / kg", "25 poin / kg")
        ),

        "Kertas" to listOf(
            Triple("Botol Plastik Bening / Pet Bening", "Rp 2000 - Rp 5000 / kg", "40 poin / kg"),
            Triple("Botol Plastik Biru Muda", "Rp 1000 - Rp 2500 / kg", "20 poin / kg"),
            Triple("Botol Plastik Warna / Pet Warna", "Rp 1000 - Rp 2000 / kg", "15 poin / kg"),
            Triple("Botol Kecap", "Rp 1000 - Rp 1500 / kg", "12 poin / kg"),
            Triple("Tutup Botol / Botol Sampo / KW1", "Rp 2000 - Rp 4500 / kg", "35 poin / kg"),
            Triple("Karah Warna", "Rp 1500 - Rp 3000 / kg", "25 poin / kg")
        ),

        "Logam" to listOf(
            Triple("Kaleng Susu / Sarden / Obat Nyamuk", "Rp 700 - Rp 3000 / kg", "15 poin / kg"),
            Triple("Kaleng Sprite / Lasegar / dll", "Rp 10000 - Rp 17000 / kg", "130 poin / kg"),
            Triple("Panci / Antena / Kuali", "Rp 14000 - Rp 20000 / kg", "170 poin / kg"),
            Triple("Besi", "Rp 3000 - Rp 7000 / kg", "50 poin / kg"),
            Triple("Paku / Rak Piring / Kompor / Gas / dll", "Rp 2500 - Rp 4500 / kg", "35 poin / kg"),
            Triple("Seng", "Rp 1500 - Rp 3500 / kg", "25 poin / kg"),
            Triple("Rangka Baja", "Rp 1500 - Rp 3000 / kg", "20 poin / kg"),
            Triple("Tembaga", "Rp 70000 - Rp 120000 / kg", "1000 poin / kg"),
        ),

        "Elektronik" to listOf(
            Triple("TV Tabung / LED", "Rp 20000 - Rp 50000 / kg", "400 poin / unit"),
            Triple("Magic Com", "Rp 5000 - Rp 20000 / kg", "60 poin / unit"),
            Triple("Kipas Angin", "Rp 5000 - Rp 20000 / kg", "100 poin / kg"),
            Triple("AC", "Rp 100000 - Rp 250000 / kg", "2000 poin / kg"),
            Triple("Laptop", "Rp 25000 - Rp 50000 / kg", "400 poin / kg"),
            Triple("Kulkas", "Rp 70000 - Rp 120000 / kg", "900 poin / kg"),
            Triple("Mesin Cuci", "Rp 50000 - Rp 70000 / kg", "600 poin / kg"),
        ),

        "Kaca & Minyak" to listOf(
            Triple("Botol Sirup", "Rp 100 - Rp 250 / kg", "2 poin / kg"),
            Triple("Botol Bir / Kecap Bango / Kurnia", "Rp 500 - Rp 700 / kg", "5 poin / kg"),
            Triple("Jelantah", "Rp 3000 - Rp 5000 / kg", "40 poin / kg"),
        ),

    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_informasi_harga, container, false)
        tabOrganik = view.findViewById(R.id.tabOrganik)
        tabAnorganik = view.findViewById(R.id.tabAnorganik)
        hargaContainer = view.findViewById(R.id.hargaContainer)

        tabOrganik.setOnClickListener {
            updateTabUI(true)
            showHargaOrganik()
        }

        tabAnorganik.setOnClickListener {
            updateTabUI(false)
            showSubkategoriAnorganik()
        }

        showHargaOrganik() // default
        return view
    }

    private fun updateTabUI(isOrganik: Boolean) {
        tabOrganik.setBackgroundResource(if (isOrganik) R.drawable.tab_selected else R.drawable.tab_unselected)
        tabAnorganik.setBackgroundResource(if (!isOrganik) R.drawable.tab_selected else R.drawable.tab_unselected)
        tabOrganik.setTextColor(if (isOrganik) Color.WHITE else Color.parseColor("#0E564D"))
        tabAnorganik.setTextColor(if (!isOrganik) Color.WHITE else Color.parseColor("#0E564D"))
    }

    private fun showHargaOrganik() {
        hargaContainer.removeAllViews()
        dummyHargaOrganik.forEach { (jenis, harga, poin) ->
            hargaContainer.addView(createHargaCard(jenis, harga, poin))
        }
    }

    private fun showSubkategoriAnorganik() {
        hargaContainer.removeAllViews()
        subKategoriAnorganik.forEach { kategori ->
            val btn = Button(requireContext()).apply {
                text = kategori
                setOnClickListener {
                    showHargaAnorganik(kategori)
                }
            }
            hargaContainer.addView(btn)
        }
    }

    private fun showHargaAnorganik(kategori: String) {
        hargaContainer.removeAllViews()
        dummyHargaAnorganik[kategori]?.forEach { (jenis, harga, poin) ->
            hargaContainer.addView(createHargaCard(jenis, harga, poin))
        }
    }

    private fun createHargaCard(jenis: String, harga: String, poin: String): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.drawable.card_darkblue)
            val jenisView = TextView(context).apply {
                text = jenis
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                textSize = 16f
            }
            val hargaView = TextView(context).apply {
                text = harga
                setTextColor(Color.WHITE)
            }
            val poinView = TextView(context).apply {
                text = poin
                setTextColor(Color.WHITE)
            }
            addView(jenisView)
            addView(hargaView)
            addView(poinView)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            layoutParams = params
        }
        return layout
    }
}