package com.dnhsolution.restokabmalang.home

class ProdukElement(val idItem: Int, val name: String, val price: String, val imageUrl: String
                    , val description: String) {
    var isFavorite = false

    fun toggleFavorite() {
        isFavorite = !isFavorite
    }
}
