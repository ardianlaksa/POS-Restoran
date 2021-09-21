package com.dnhsolution.restokabmalang.database;

public class ItemProduk {
    int id;
    String id_tempat_usaha;
    String nama_produk;
    String harga;
    String keterangan;
    String foto;
    String status;

    public ItemProduk(int id, String id_tempat_usaha, String nama_produk, String harga, String keterangan, String foto, String status) {
        this.id = id;
        this.id_tempat_usaha = id_tempat_usaha;
        this.nama_produk = nama_produk;
        this.harga = harga;
        this.keterangan = keterangan;
        this.foto = foto;
        this.status = status;
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
}
