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
    private boolean status;


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

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }
}
