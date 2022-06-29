package com.dnhsolution.restokabmalang.sistem.produk;

/**
 * Created by sawrusdino on 09/04/2018.
 */

public class ItemProduk {

    private String id_barang;
    private String nama_barang;
    private String url_image;
    private String harga;
    private String keterangan;
    private String isPajak;
    private String jenisProduk;
    private boolean status;
    private String kodeProduk;
    private String seriProduk;
    private String rangeTransaksiKarcisAwal;
    private String rangeTransaksiKarcisAkhir;
    private String rangeTransaksiKarcis;


    public ItemProduk() {
    }

    public ItemProduk(String nama_barang, String url_image){
        this.nama_barang = nama_barang;
        this.url_image = url_image;
    }

    public String getId_barang() {
        return id_barang;
    }

    public void setId_barang(String id_barang) {
        this.id_barang = id_barang;
    }

    public String getNama_barang() {
        return nama_barang;
    }

    public void setNama_barang(String nama_barang) {
        this.nama_barang = nama_barang;
    }

    public String getUrl_image() {
        return url_image;
    }

    public void setUrl_image(String url_image) {
        this.url_image = url_image;
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

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
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
