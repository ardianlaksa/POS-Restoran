package com.dnhsolution.restokabmalang.transaksi.produk_list

class ProdukListElement(val idItem: Int, val name: String, val price: String, val imageUrl: String
                        , val description: String, val status: String,val isPajak: String,val jnsProduk: String) {
    var isFavorite = false

    fun toggleFavorite() : Boolean{
        isFavorite = !isFavorite
        return isFavorite
    }
}
