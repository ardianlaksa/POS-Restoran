package com.dnhsolution.restokabmalang

interface ProdukOnTask {
    fun produkOnTask(result:String?)
}

interface RekapBulananOnTask {
    fun rekapBulananOnTask(result:String?)
}

interface RekapHarianOnTask {
    fun rekapHarianOnTask(result:String?)
}

interface KeranjangTransaksiOnTask {
    fun keranjangTransaksiOnTask(result:String?)
}

interface SistemMasterOnTask {
    fun sistemMasterOnTask(result:String?)
}

interface KeranjangProdukItemOnTask {
    fun keranjangProdukItemOnTask(position:Int,totalPrice:Int,qty:Int)
}

interface KeranjangProdukItemDeleteOnTask {
    fun keranjangProdukItemOnTask(position:Int,totalPrice:Int,qty:Int)
}