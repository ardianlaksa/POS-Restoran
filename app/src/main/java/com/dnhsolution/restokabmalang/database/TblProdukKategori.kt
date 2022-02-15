package com.dnhsolution.restokabmalang.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TblProdukKategori(
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        val nama: String,
        val idTempatUsaha: String,
        val idPengguna: String
        )