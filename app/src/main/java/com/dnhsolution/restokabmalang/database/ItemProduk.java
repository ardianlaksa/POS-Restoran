package com.dnhsolution.restokabmalang.database;

public class ItemProduk {
    int id;
    String id_tempat_usaha;
    String nama_produk;
    String harga;
    String keterangan;
    String foto;
    String status;
    private String isPajak;
    private String jenisProduk;
    String kodeProduk;
    String seriProduk;
    String rangeTransaksiKarcisAwal;
    String rangeTransaksiKarcisAkhir;
    String rangeTransaksiKarcis;

    public ItemProduk(int id, String id_tempat_usaha, String nama_produk, String harga
            , String keterangan, String foto, String status, String isPajak, String jenisProduk
            ,String kode, String seriProduk, String rangeTransaksiKarcisAwal, String rangeTransaksiKarcisAkhir
            , String rangeTransaksiKarcis) {
        this.id = id;
        this.id_tempat_usaha = id_tempat_usaha;
        this.nama_produk = nama_produk;
        this.harga = harga;
        this.keterangan = keterangan;
        this.foto = foto;
        this.status = status;
        this.isPajak = isPajak;
        this.jenisProduk = jenisProduk;
        this.kodeProduk = kode;
        this.seriProduk = seriProduk;
        this.rangeTransaksiKarcisAwal = rangeTransaksiKarcisAwal;
        this.rangeTransaksiKarcisAkhir = rangeTransaksiKarcisAkhir;
        this.rangeTransaksiKarcis = rangeTransaksiKarcis;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getId_tempat_usaha() {
        return id_tempat_usaha;
    }

    public void setId_tempat_usaha(String id_tempat_usaha) {
        this.id_tempat_usaha = id_tempat_usaha;
    }

    public String getNama_produk() {
        return nama_produk;
    }

    public void setNama_produk(String nama_produk) {
        this.nama_produk = nama_produk;
    }

    public String getHarga() {
        return harga;
    }

    public void setHarga(String harga) {
        this.harga = harga;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsPajak() {
        return isPajak;
    }

    public void setIsPajak(String value) {
        this.isPajak = value;
    }

    public String getJenisProduk() {
        return jenisProduk;
    }

    public void setJenisProduk(String value) {
        this.jenisProduk = value;
    }

    public String getKodeProduk() {
        return kodeProduk;
    }

    public void setKodeProduk(String value) {
        this.kodeProduk = value;
    }

    public String getSeriProduk() {
        return seriProduk;
    }

    public void setSeriProduk(String value) {
        this.seriProduk = value;
    }

    public void setRangeTransaksiKarcisAwal(String value) {
        this.rangeTransaksiKarcisAwal = value;
    }

    public String getRangeTransaksiKarcisAwal() {
        return rangeTransaksiKarcisAwal;
    }

    public void setRangeTransaksiKarcisAkhir(String value) {
        this.rangeTransaksiKarcisAkhir = value;
    }

    public String getRangeTransaksiKarcisAkhir() {
        return rangeTransaksiKarcisAkhir;
    }

    public void setRangeTransaksiKarcis(String value) {
        this.rangeTransaksiKarcis = value;
    }

    public String getRangeTransaksiKarcis() {
        return rangeTransaksiKarcis;
    }
}
