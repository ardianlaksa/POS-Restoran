package com.dnhsolution.restokabmalang.cetak;

public class ItemProduk {
    public int no;
    public String id_produk;
    public String nama_produk;
    public String qty;
    public String harga;
    public String total_harga;
    private String isPajak;
    private String nomor_karcis;
    private String kodeProduk;
    private String seriProduk;
    private String rangeTransaksiKarcisAwal;
    private String rangeTransaksiKarcisAkhir;
    String serviceChargeRp;

    public String nomor_seri;

    public ItemProduk() {
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
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

    public String getIsPajak() {
        return isPajak;
    }

    public void setIsPajak(String value) {
        this.isPajak = value;
    }

    public String getHarga() {
        return harga;
    }

    public void setHarga(String harga) {
        this.harga = harga;
    }

    public String getTotal_harga() {
        return total_harga;
    }

    public void setTotal_harga(String total_harga) {
        this.total_harga = total_harga;
    }

    public String getNomorKarcis() {
        return nomor_karcis;
    }

    public void setNomorKarcis(String value) {
        this.nomor_karcis = value;
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

    public String getServiceChargeRp() {
        return serviceChargeRp;
    }

    public void setServiceChargeRp(String value) {
        this.serviceChargeRp = value;
    }

    public String getNomorSeri() {
        return nomor_seri;
    }

    public void setNomorSeri(String value) {
        this.nomor_seri = value;
    }

}
