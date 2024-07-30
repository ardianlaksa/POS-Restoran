package com.dnhsolution.restokabmalang.utilities

object Url {
//    const val serverBase = "http://36.89.91.155/"
    const val serverHtdoc = "pdrd/Android/AndroidJsonPOS_Dev2/"
    const val serverBase = "https://sipanji.id/"
//    const val serverBase = "http://192.168.120.93/kantor/"
    const val serverPos = "$serverBase$serverHtdoc"
    const val serverFoto = "https://sipanji.id/pdrd/"

//    const val serverHtdoc = "pdrd/Android/AndroidJsonPOS_Dev2/"
//    const val serverBase = "http://192.168.10.116/"
//    const val serverPos = "$serverBase$serverHtdoc"
//    const val serverFoto = "http://192.168.10.116/pdrd/"

//    const val serverBase = "http://192.168.43.195/"
//    const val serverPdrd = "http://devdeenha.ddns.net:8080/pdrd/"
//    const val serverPos = "http://192.168.43.195/pdrd/Android/testing/AndroidJsonPOS/"
//    const val serverPosParkir = "http://192.168.43.195/etax_dev/MobilePosParkirJson/"
//    const val serverFoto = "http://192.168.43.195/pdrd/"

    const val SESSION_ALAMAT = "alamat"
    const val SESSION_BATAS_WAKTU = "batas_waktu"
    const val SESSION_EMAIL = "email"
    const val SESSION_ID_HIBURAN_NOMOR = "idHiburanNomor"
    const val SESSION_ID_PENGGUNA = "idPengguna"
    const val SESSION_ID_TEMPAT_USAHA = "idTempatUsaha"
    const val SESSION_ISCETAK_BILLING = "isCetakBilling"
    const val SESSION_JENIS_PAJAK = "jenis_pajak"
    const val SESSION_KELENGKAPAN = "kelengkapan"
    const val SESSION_LOGO = "logo"
    const val SESSION_NAMA_TEMPAT_USAHA = "nama_tempat_usaha"
    const val SESSION_NAME = "pos"
    const val SESSION_PRINTER_BT = "printer_bt"
    const val SESSION_PAJAK_PERSEN = "pajakPersen"
    const val SESSION_STATUS_BATAS = "status_batas"
    const val SESSION_STS_LOGIN = "status"
    const val SESSION_TEMP_TEMA = "temp_tema"
    const val SESSION_SERVICE_CHARGE = "service_charge"
    const val SESSION_TELP = "telp"
    const val SESSION_TIPE_STRUK = "tipeStruk"
    const val SESSION_UUID = "UUID"
    const val SESSION_USERNAME = "username"
    const val SESSION_NAMA_PETUGAS = "namaPetugas"

    const val getConfig = "${serverPos}getConfig"
    const val setLabel = "label"
    const val setTema = "tema"
    const val setHapusProduk = "${serverPos}setHapusProduk"
    const val setKeranjangTransaksi = "${serverPos}setKeranjangTransaksi"
    const val setKeranjangTransaksiTersimpan = "${serverPos}setKeranjangTransaksiTersimpan"
    const val getProduk = "${serverPos}getProduk"
    const val setSistemMaster = "${serverPos}setSistemMaster"
    const val getRekapHarian = "${serverPos}getRekapHarian"
    const val getDRekapHarian = "${serverPos}getRekapHarianDetail"
    const val getRekapBilling = "${serverPos}getRekapBilling"
    const val getRekapBulanan = "${serverPos}getRekapBulanan"
//    const val getRekapBulanan1 = "${serverPos}getRekapBulanan1"
    const val getBatasWaktu = "${serverPos}Config"
    const val setTambahProduk = "${serverPos}TambahData"
    const val setUpdateProduk = "${serverPos}UpdateData"
    const val getDataStruk = "${serverPos}getDataStruk"
    const val setUpdateHiburanNomor = "${serverPos}setUpdateHiburanNomor"
    const val notifEmailPdfHarian = "${serverPos}notifEmailPdfHarian"

    const val getDownloadApk = "${serverBase}etax/file_etax/POS_Android.apk"
    const val getDownloadLogoMalangMakmur = "${serverBase}etax/file_etax/ic_malang_makmur_grayscale.png"

    //Nomor Seri

    const val TambahNomorSeri = "${serverPos}tambah_nomor_seri"

}