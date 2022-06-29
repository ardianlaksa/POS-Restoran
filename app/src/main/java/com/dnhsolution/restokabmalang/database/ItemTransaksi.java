package com.dnhsolution.restokabmalang.database;

public class ItemTransaksi {
    int id;
    String tanggal_trx;
    String disc;
    String omzet;
    String id_pengguna;
    String id_tempat_usaha;
    String disc_rp;
    String status;
    String pajakRp;
    String bayar;
    String pajakpersen;
    String idHiburanNomor;
    String serviceChargeRp;

    public ItemTransaksi(int id, String tanggal_trx, String disc, String omzet
            , String id_pengguna, String id_tempat_usaha, String disc_rp, String status,String pajakRp
            ,String bayar,String pajakPersen ,String idHiburanNomor,String serviceChargeRp) {
        this.id = id;
        this.tanggal_trx = tanggal_trx;
        this.disc = disc;
        this.omzet = omzet;
        this.id_pengguna = id_pengguna;
        this.id_tempat_usaha = id_tempat_usaha;
        this.disc_rp = disc_rp;
        this.status = status;
        this.pajakRp = pajakRp;
        this.bayar = bayar;
        this.pajakpersen = pajakPersen;
        this.idHiburanNomor = idHiburanNomor;
        this.serviceChargeRp = serviceChargeRp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTanggal_trx() {
        return tanggal_trx;
    }

    public void setTanggal_trx(String tanggal_trx) {
        this.tanggal_trx = tanggal_trx;
    }

    public String getDisc() {
        return disc;
    }

    public void setDisc(String disc) {
        this.disc = disc;
    }

    public String getOmzet() {
        return omzet;
    }

    public void setOmzet(String omzet) {
        this.omzet = omzet;
    }

    public String getId_pengguna() {
        return id_pengguna;
    }

    public void setId_pengguna(String id_pengguna) {
        this.id_pengguna = id_pengguna;
    }

    public String getId_tempat_usaha() {
        return id_tempat_usaha;
    }

    public void setId_tempat_usaha(String id_tempat_usaha) {
        this.id_tempat_usaha = id_tempat_usaha;
    }

    public String getDisc_rp() {
        return disc_rp;
    }

    public void setDisc_rp(String disc_rp) {
        this.disc_rp = disc_rp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPajakRp() {
        return pajakRp;
    }

    public void setPajakRp(String value) {
        this.pajakRp = value;
    }

    public String getBayar() {
        return bayar;
    }

    public void setBayar(String value) {
        this.bayar = value;
    }

    public String getPajakPersen() {
        return pajakpersen;
    }

    public void setPajakPersen(String value) {
        this.pajakpersen = value;
    }

    public String getIdHiburanNomor() {
        return idHiburanNomor;
    }

    public void setIdHiburanNomor(String value) {
        this.idHiburanNomor = value;
    }

    public String getServiceChargeRp() {
        return serviceChargeRp;
    }

    public void setServiceChargeRp(String value) {
        this.serviceChargeRp = value;
    }
}
