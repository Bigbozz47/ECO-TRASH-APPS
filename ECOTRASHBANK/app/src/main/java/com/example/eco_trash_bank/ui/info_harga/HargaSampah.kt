package com.example.eco_trash_bank.model

data class HargaSampah(
    val id: Int,
    val jenis: String,
    val harga_per_kg: Double,
    val poin_per_kg: Int,
    val kategori: String,
    val is_active: Boolean
)
