package com.dnhsolution.restokabmalang

import java.io.Serializable

class ProdukSerializable : Serializable {

    var idItem: Int = 0
    var name: String? = null
    var price: String? = null
    var imgResource: Int = 0
    var imgUrl: String? = null
    var totalPrice: Int = 0

    constructor() {

    }

    constructor(idItem: Int, name: String, price: String, imgResource: Int, imgUrl: String, totalPrice: Int) {
        this.idItem = idItem
        this.name = name
        this.price = price
        this.imgResource = imgResource
        this.imgUrl = imgUrl
        this.totalPrice = totalPrice
    }
}
