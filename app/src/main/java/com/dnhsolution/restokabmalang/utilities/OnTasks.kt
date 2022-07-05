package com.dnhsolution.restokabmalang.utilities

import com.dnhsolution.restokabmalang.MainActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming


interface DataTersimpanLongClick {
    fun dataTersimpanLongClick(result:String?)
}

interface ProdukOnTask {
    fun produkOnTask(result:String?)
}

interface RekapBulananOnTask {
    fun rekapBulananOnTask(result:String?)
}

interface DRekapHarianOnTask {
    fun DrekapHarianOnTask(result:String?)
}

interface RekapHarianOnTask {
    fun rekapHarianOnTask(result:String?)
}

interface RekapHarianDetailOnTask {
    fun rekapHarianDetailOnTask(result:String?)
}

interface RekapHarianDetailLongClick {
    fun rekapHarianDetailLongClick(result:String?)
}

interface KeranjangTransaksiOnTask {
    fun keranjangTransaksiOnTask(result:String?)
}

interface SistemMasterOnTask {
    fun sistemMasterOnTask(result:String?)
}

interface HapusProdukMasterOnTask {
    fun hapusProdukMasterOnTask(tipe:String,posisi:Int)
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

interface DownloadFileNetworkResult {
    fun downloadFileNetworkResult(result: Any?)
}