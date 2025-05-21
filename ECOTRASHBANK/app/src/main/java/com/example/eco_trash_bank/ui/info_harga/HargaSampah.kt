// com.example.eco_trash_bank.model.HargaSampah
package com.example.eco_trash_bank.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HargaSampah(
    val id: Int,
    val jenis: String,
    val harga_per_kg: Double,
    val poin_per_kg: Int,
    val kategori: String,
    val sub_kategori: String?,    // tambahkan jika belum ada!
    val is_active: Boolean
) : Parcelable
