package com.dnhsolution.restokabmalang.transaksi

import android.os.Parcel
import android.os.Parcelable

class ProdukSerializable : Parcelable {

    var idItem: Int = 0
    var name: String? = null
    var price: String? = null
    var imgUrl: String? = null
    var totalPrice: Int = 0
    var qty: Int = 1
    var isPajak: String? = null
    var keterangan: String? = null

    constructor(parcel: Parcel) : this() {
        idItem = parcel.readInt()
        name = parcel.readString()
        price = parcel.readString()
        imgUrl = parcel.readString()
        totalPrice = parcel.readInt()
        qty = parcel.readInt()
        isPajak = parcel.readString()
        keterangan = parcel.readString()
    }

    constructor()

    constructor(idItem: Int, name: String, price: String, imgUrl: String, totalPrice: Int, qty: Int
                , isPajak:String, keterangan:String) {
        this.idItem = idItem
        this.name = name
        this.price = price
        this.imgUrl = imgUrl
        this.totalPrice = totalPrice
        this.qty = qty
        this.isPajak = isPajak
        this.keterangan = keterangan
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(idItem)
        parcel.writeString(name)
        parcel.writeString(price)
        parcel.writeString(imgUrl)
        parcel.writeInt(totalPrice)
        parcel.writeInt(qty)
        parcel.writeString(isPajak)
        parcel.writeString(keterangan)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProdukSerializable> {
        override fun createFromParcel(parcel: Parcel): ProdukSerializable {
            return ProdukSerializable(parcel)
        }

        override fun newArray(size: Int): Array<ProdukSerializable?> {
            return arrayOfNulls(size)
        }
    }
}
