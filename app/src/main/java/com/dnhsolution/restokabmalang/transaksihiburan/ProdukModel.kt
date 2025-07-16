package com.dnhsolution.restokabmalang.transaksihiburan

data class ProdukModel(
    var id: Int,
    var nama: String,
    var nominal: Int,
    var ispajak: Int,
    var qty: Int = 0,
    var ischecked: Boolean = false
)
