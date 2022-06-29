package com.dnhsolution.restokabmalang.tersimpan;

public class ItemDetailTersimpan {
    int id;
    int no;
    String nama;
    String harga;
    String qty;
    String total;
    String id_produk;
    String isPajak;
    String keterangan;
    String seriProduk;
    String rangeTransaksiKarcisAwal;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getHarga() {
        return harga;
    }

    public void setHarga(String harga) {
        this.harga = harga;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getId_produk() {
        return id_produk;
    }

    public void setId_produk(String id_produk) {
        this.id_produk = id_produk;
    }

    public String getIsPajak() {
        return isPajak;
    }

    public void setIsPajak(String value) {
        this.isPajak = value;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String value) {
        this.keterangan = value;
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
}
