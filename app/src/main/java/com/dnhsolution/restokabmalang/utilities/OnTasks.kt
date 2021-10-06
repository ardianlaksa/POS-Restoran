package com.dnhsolution.restokabmalang.utilities

interface ProdukOnTask {
    fun produkOnTask(result:String?)
}

interface RekapBulananOnTask {
    fun rekapBulananOnTask(result:String?)
}

interface RekapHarianOnTask {
    fun rekapHarianOnTask(result:String?)
}

interface DRekapHarianOnTask {
    fun DrekapHarianOnTask(result:String?)
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

internal interface OnDataFetched {
    fun showProgressBar()
    fun hideProgressBar()
    fun setDataInPageWithResult(result: Any?)
}