package com.dnhsolution.restokabmalang.utilities

object Url {
    const val serverPdrd = "http://devdeenha.ddns.net:8080/pdrd/"
    const val serverPos = "http://devdeenha.ddns.net:8000/android/pos/androidjson/"
    const val serverFoto = "http://devdeenha.ddns.net:8000/android/pos/"
    const val SESSION_USERNAME = "username"
    const val SESSION_PASSWORD = "password"
    const val SESSION_ID_PENGGUNA = "idPengguna"
    const val SESSION_ID_TEMPAT_USAHA = "idTempatUsaha"
    const val SESSION_NAMA_TEMPAT_USAHA = "nama_tempat_usaha"
    const val SESSION_STS_LOGIN = "status"
    const val SESSION_NAME = "pos"
    const val setLabel = "label"
    const val setTema = "tema"
    const val setKeranjangTransaksi = "${serverPos}setKeranjangTransaksi"
    const val getProduk = "${serverPos}getProduk"
    const val setSistemMaster = "${serverPos}setSistemMaster"
    const val getRekapHarian = "${serverPos}getRekapHarian"
    const val getRekapBulanan = "${serverPos}getRekapBulanan"
}