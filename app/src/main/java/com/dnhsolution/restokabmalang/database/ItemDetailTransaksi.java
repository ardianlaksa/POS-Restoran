package com.dnhsolution.restokabmalang.database;

public class ItemDetailTransaksi {
    int id;
    String id_trx;
    String id_produk;
    String nama_produk;
    String qty;
    String harga;


    public ItemDetailTransaksi(int id, String id_trx, String id_produk, String nama_produk, String qty, String harga) {
        this.id = id;
        this.id_trx = id_trx;
        this.id_produk = id_produk;
        this.nama_produk = nama_produk;
        this.qty = qty;
        this.harga = harga;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getId_trx() {
        return id_trx;
    }

    public void setId_trx(String id_trx) {
        this.id_trx = id_trx;
    }

    public String getId_produk() {
        return id_produk;
    }

    public void setId_produk(String id_produk) {
        this.id_produk = id_produk;
    }

    public String getNama_produk() {
        return nama_produk;
    }

    public void setNama_produk(String nama_produk) {
        this.nama_produk = nama_produk;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getHarga() {
        return harga;
    }

    public void setHarga(String harga) {
        this.harga = harga;
    }

}
