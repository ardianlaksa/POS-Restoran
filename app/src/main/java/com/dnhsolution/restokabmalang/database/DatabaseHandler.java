package com.dnhsolution.restokabmalang.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dnhsolution.restokabmalang.tersimpan.ItemDetailTersimpan;
import com.dnhsolution.restokabmalang.tersimpan.ItemTersimpan;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "pos";

    // Contacts table name
    public final String TABLE_PRODUK = "produk";
    public final String TABLE_TRANSAKSI = "transaksi";
    public final String TABLE_DETAIL_TRANSAKSI = "detail_transaksi";

    public final String col_id="id";
    public final String col_id_tempat_usaha="id_tempat_usaha";
    public final String col_nama_produk = "nama_produk";
    public final String col_harga = "harga";
    public final String col_status = "status";

    //kolom produk
    public final String col_keterangan = "keterangan";
    public final String col_foto = "foto";
    public final String col_ispajak = "ispajak";
    public final String col_jns_produk = "jns_produk";
    public final String col_kode_produk = "kode_produk";
    public final String col_seri_produk = "seri_produk";
    public final String col_range_transaksi_karcis = "range_transaksi_karcis";
    public final String col_range_transaksi_karcis_awal = "range_transaksi_karcis_awal";
    public final String col_range_transaksi_karcis_akhir = "range_transaksi_karcis_akhir";

    //kolom transaksi
    public final String col_tanggal_trx="tanggal_trx";
    public final String col_disc="disc";
    public final String col_omzet="omzet";
    public final String col_id_pengguna="id_pengguna";
    public final String col_disc_rp="disc_rp";
    public final String col_pajak_rp="pajak_rp";
    public final String col_bayar="bayar";
    public final String col_pajak_persen="col_pajak_persen";
    public final String col_id_hiburan_nomor="col_id_hiburan_nomor";
    public final String col_service_charge_rp ="col_service_charge_rp";

    //kolom detail_transaksi
    public final String col_id_trx= "id_trx";
    public final String col_id_produk = "id_produk";
    public final String col_qty = "qty";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // CREATE TABLE
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABEL_PRODUK = "CREATE TABLE " + TABLE_PRODUK + "("
                + col_id + " INTEGER PRIMARY KEY AUTOINCREMENT," + col_id_tempat_usaha + " TEXT,"
                + col_nama_produk + " TEXT,"+
                col_harga + " TEXT,"+ col_keterangan + " TEXT,"+ col_foto + " TEXT,"
                + col_status + " TEXT,"+ col_ispajak + " TEXT,"+ col_jns_produk + " TEXT,"
                + col_range_transaksi_karcis + " TEXT,"+ col_kode_produk + " TEXT,"
                + col_range_transaksi_karcis_awal + " TEXT,"+ col_range_transaksi_karcis_akhir
                + " TEXT,"+ col_seri_produk + " TEXT"+")";

        String CREATE_TABEL_TRANSAKSI = "CREATE TABLE " + TABLE_TRANSAKSI + "("
                + col_id + " INTEGER PRIMARY KEY AUTOINCREMENT," + col_tanggal_trx + " TEXT," + col_disc + " TEXT,"+
                col_omzet + " TEXT,"+ col_id_pengguna + " TEXT,"+ col_id_tempat_usaha + " TEXT,"
                + col_disc_rp + " TEXT,"+ col_pajak_rp + " TEXT,"+ col_status
                + " TEXT,"+col_bayar + " TEXT,"+col_pajak_persen + " TEXT,"+col_id_hiburan_nomor
                + " TEXT,"+ col_service_charge_rp + " TEXT"+")";

        String CREATE_TABEL_DETAIL_TRANSAKSI = "CREATE TABLE " + TABLE_DETAIL_TRANSAKSI + "("
                + col_id + " INTEGER PRIMARY KEY AUTOINCREMENT," + col_id_trx + " TEXT," + col_id_produk + " TEXT,"+
                col_nama_produk + " TEXT,"+ col_qty + " TEXT,"+ col_harga + " TEXT,"+ col_keterangan + " TEXT"+")";

        db.execSQL(CREATE_TABEL_PRODUK);
        db.execSQL(CREATE_TABEL_TRANSAKSI);
        db.execSQL(CREATE_TABEL_DETAIL_TRANSAKSI);
    }

    // Perbarui database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tabel lama jika sudah ada
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSAKSI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETAIL_TRANSAKSI);

        // Create tables again
        onCreate(db);
    }

    //INSERT DATA
    public void insert_produk(ItemProduk ip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(col_id, ip.getId());
        values.put(col_id_tempat_usaha, ip.getId_tempat_usaha());
        values.put(col_nama_produk, ip.getNama_produk());
        values.put(col_harga, ip.getHarga());
        values.put(col_keterangan, ip.getKeterangan());
        values.put(col_foto, ip.getFoto());
        values.put(col_status, ip.getStatus());
        values.put(col_ispajak, ip.getIsPajak());
        values.put(col_jns_produk, ip.getJenisProduk());
        values.put(col_kode_produk, ip.getKodeProduk());
        values.put(col_seri_produk, ip.getSeriProduk());
        values.put(col_range_transaksi_karcis_awal, ip.getRangeTransaksiKarcisAwal());
        values.put(col_range_transaksi_karcis_akhir, ip.getRangeTransaksiKarcisAkhir());
        values.put(col_range_transaksi_karcis, ip.getRangeTransaksiKarcis());

        System.out.println("c :"+ip.getIsPajak()+" "+ip.getJenisProduk());
        // memasukkan data
        db.insert(TABLE_PRODUK, null, values);
        db.close(); // Menutup koneksi database
    }

    public void insert_transaksi(ItemTransaksi it) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(col_tanggal_trx, it.getTanggal_trx());
        values.put(col_disc, it.getDisc());
        values.put(col_omzet, it.getOmzet());
        values.put(col_id_pengguna, it.getId_pengguna());
        values.put(col_id_tempat_usaha, it.getId_tempat_usaha());
        values.put(col_disc_rp, it.getDisc_rp());
        values.put(col_status, it.getStatus());
        values.put(col_pajak_rp, it.getPajakRp());
        values.put(col_bayar, it.getBayar());
        values.put(col_pajak_persen, it.getPajakPersen());
        values.put(col_id_hiburan_nomor, it.getIdHiburanNomor());
        values.put(col_service_charge_rp, it.getServiceChargeRp());

        // memasukkan data
        db.insert(TABLE_TRANSAKSI, null, values);
        db.close(); // Menutup koneksi database
    }

    public void insert_detail_transaksi(ItemDetailTransaksi idt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(col_id_trx, idt.getId_trx());
        values.put(col_id_produk, idt.getId_produk());
        values.put(col_nama_produk, idt.getNama_produk());
        values.put(col_qty, idt.getQty());
        values.put(col_harga, idt.getHarga());
        values.put(col_keterangan, idt.getKeterangan());

        // memasukkan data
        db.insert(TABLE_DETAIL_TRANSAKSI, null, values);
        db.close(); // Menutup koneksi database
    }

    //DELETE DATA
    public void deleteAllTable(){
        delete_detail_transaksi();
        delete_transaksi();
        delete_produk();
    }

    public void delete_produk(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PRODUK);
        db.execSQL("VACUUM");
        db.close();
    }

    public void delete_transaksi(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_TRANSAKSI);
        db.execSQL("VACUUM");
        db.close();
    }

    public void delete_detail_transaksi(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_DETAIL_TRANSAKSI);
        db.execSQL("VACUUM");
        db.close();
    }

    public void delete_by_id_trx(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_TRANSAKSI + " WHERE " + col_id +"=="+id);
        db.close();
    }

    public void delete_by_id_detail_trx(int id_trx){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_DETAIL_TRANSAKSI + " WHERE " + col_id_trx +"=="+id_trx);
        db.close();
    }

    public void hapusByIdProduk(String idProduk){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PRODUK + " WHERE " + col_id +"=="+idProduk);
        db.close();
    }

    //COUNT DATA
    public int CountDataTersimpanUpload() {
        String countQuery = "SELECT  * FROM "+ TABLE_TRANSAKSI+" WHERE "+col_status+"=='0'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int CountDataTersimpan2() {
        String countQuery = "SELECT  * FROM "+ TABLE_TRANSAKSI;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int CountProdukTersimpan() {
        String countQuery = "SELECT  * FROM "+ TABLE_PRODUK+" WHERE "+col_status+"=='0'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }


    public int CountMaxIdTrx() {
        String countQuery = "SELECT MAX("+col_id+") FROM "+ TABLE_TRANSAKSI;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int result = Integer.parseInt(cursor.getString(0));
        cursor.close();
        return result;
    }

    public int CountDataProduk() {
        String countQuery = "SELECT * FROM "+ TABLE_PRODUK;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int CountDataProdukId(int value) {
        String countQuery = "SELECT * FROM "+ TABLE_PRODUK + " WHERE "+col_id+"="+value;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    //GET DATA
    public ArrayList getDataTersimpan() {
        ArrayList list = new ArrayList();

        String selectQuery = "SELECT "+col_id+","+col_tanggal_trx+","+col_disc+
                ","+col_omzet+","+col_disc_rp+","+col_status+","+col_pajak_rp+" FROM " + TABLE_TRANSAKSI +
                " ORDER BY "+col_tanggal_trx+" DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Perulangan semua data untuk dimasukkan kedalam list
        if (cursor.moveToFirst()) {
            do {
                ItemTersimpan it = new ItemTersimpan();
                it.setId(Integer.parseInt(cursor.getString(0)));
                it.setTanggal_trx(cursor.getString(1));
                it.setDisc(cursor.getString(2));
                it.setOmzet(cursor.getString(3));
                it.setDisc_rp(cursor.getString(4));
                it.setStatus(cursor.getString(5));
                it.setPajakRp(cursor.getString(6));

                // Menambahkan data ke dalam list
                list.add(it);
            } while (cursor.moveToNext());
        }
        // return list
        return list;
    }

    public ArrayList getDataTersimpanUpload() {
        ArrayList list = new ArrayList();
        String selectQuery = "SELECT "+col_id+","+col_tanggal_trx+","+col_disc+
                ","+col_omzet+","+col_disc_rp+","+col_status+","+col_pajak_rp+
                ","+col_bayar+","+col_id_hiburan_nomor+","+ col_service_charge_rp +
                " FROM " + TABLE_TRANSAKSI +" WHERE "+col_status+"=='0'"+
                " ORDER BY "+col_tanggal_trx+" DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Perulangan semua data untuk dimasukkan kedalam list
        if (cursor.moveToFirst()) {
            do {
                ItemTersimpan it = new ItemTersimpan();
                it.setId(Integer.parseInt(cursor.getString(0)));
                it.setTanggal_trx(cursor.getString(1));
                it.setDisc(cursor.getString(2));
                it.setOmzet(cursor.getString(3));
                it.setDisc_rp(cursor.getString(4));
                it.setStatus(cursor.getString(5));
                it.setPajakRp(cursor.getString(6));
                it.setBayar(cursor.getString(7));
                it.setIdHiburanNomor(cursor.getString(8));
                it.setNominalServiceCharge(cursor.getString(9));

                // Menambahkan data ke dalam list
                list.add(it);
            } while (cursor.moveToNext());
            cursor.close();
        }
        // return list
        return list;
    }

    public ArrayList getDetailTersimpan(String idTrx) {
        ArrayList list = new ArrayList();

        String selectQuery = "SELECT P."+col_id+",P."+col_nama_produk+","+col_qty+",P."+col_harga+","+
                col_id_produk+","+col_ispajak+",P."+col_keterangan+",P."+col_seri_produk+",P."+
                col_range_transaksi_karcis_awal+" FROM " + TABLE_DETAIL_TRANSAKSI +
                " DT LEFT JOIN "+TABLE_PRODUK+" P ON P."+col_id+" = DT."+col_id_produk+
                " WHERE "+col_id_trx+"=='"+idTrx+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Perulangan semua data untuk dimasukkan kedalam list
        if (cursor.moveToFirst()) {
            do {
                ItemDetailTersimpan it = new ItemDetailTersimpan();
                it.setId(Integer.parseInt(cursor.getString(0)));
                it.setNama(cursor.getString(1));
                it.setQty(cursor.getString(2));
                it.setHarga(cursor.getString(3));
                it.setId_produk(cursor.getString(4));
                it.setIsPajak(cursor.getString(5));
                it.setKeterangan(cursor.getString(6));
                it.setSeriProduk(cursor.getString(7));
                it.setRangeTransaksiKarcisAwal(cursor.getString(8));

                // Menambahkan data ke dalam list
                list.add(it);
            } while (cursor.moveToNext());
        }
        // return list
        return list;
    }

    public ArrayList getDataProduk() {
        ArrayList list = new ArrayList();


        String selectQuery = "SELECT "+col_id+","+col_id_tempat_usaha+","+col_nama_produk+
                ","+col_harga+","+col_keterangan+","+col_foto+","+col_status+","+col_ispajak+","+col_jns_produk+" FROM " + TABLE_PRODUK;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Perulangan semua data untuk dimasukkan kedalam list
        if (cursor.moveToFirst()) {
            do {
                com.dnhsolution.restokabmalang.sistem.produk.ItemProduk it = new com.dnhsolution.restokabmalang.sistem.produk.ItemProduk();
                it.setId_barang(cursor.getString(0));
                it.setNama_barang(cursor.getString(2));
                it.setHarga(cursor.getString(3));
                it.setKeterangan(cursor.getString(4));
                it.setUrl_image(cursor.getString(5));
                it.setIsPajak(cursor.getString(7));
                it.setJenisProduk(cursor.getString(8));

                // Menambahkan data ke dalam list
                list.add(it);
            } while (cursor.moveToNext());
        }
        // return list
        return list;
    }

    //get from bhs.kotlin transaksi
    public List<com.dnhsolution.restokabmalang.sistem.produk.ItemProduk> getDataProduk2(String argument) {
        List<com.dnhsolution.restokabmalang.sistem.produk.ItemProduk> list = new ArrayList<>();

        String selectQuery = "SELECT "+col_id+","+col_id_tempat_usaha+","+col_nama_produk+
                ","+col_harga+","+col_keterangan+","+col_foto+","+col_status+
                ","+col_ispajak+","+col_jns_produk+","+col_kode_produk+","+col_seri_produk+
                ","+col_range_transaksi_karcis_awal+","+col_range_transaksi_karcis_akhir+","+col_range_transaksi_karcis+" FROM " + TABLE_PRODUK +
                " WHERE " + col_jns_produk + " = " + argument;

        if(argument.equalsIgnoreCase("0"))
            selectQuery = "SELECT "+col_id+","+col_id_tempat_usaha+","+col_nama_produk+
                    ","+col_harga+","+col_keterangan+","+col_foto+","+col_status+
                    ","+col_ispajak+","+col_jns_produk+","+col_kode_produk+","+col_seri_produk+
                    ","+col_range_transaksi_karcis_awal+","+col_range_transaksi_karcis_akhir+","+col_range_transaksi_karcis+" FROM " + TABLE_PRODUK;

        System.out.println(selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Perulangan semua data untuk dimasukkan kedalam list
        if (cursor.moveToFirst()) {
            do {
                com.dnhsolution.restokabmalang.sistem.produk.ItemProduk it = new com.dnhsolution.restokabmalang.sistem.produk.ItemProduk();
                it.setId_barang(cursor.getString(0));
                it.setNama_barang(cursor.getString(2));
                it.setHarga(cursor.getString(3));
                it.setKeterangan(cursor.getString(4));
                it.setUrl_image(cursor.getString(5));
                it.setIsPajak(cursor.getString(7));
                it.setJenisProduk(cursor.getString(8));
                it.setKodeProduk(cursor.getString(9));
                it.setSeriProduk(cursor.getString(10));
                it.setRangeTransaksiKarcisAwal(cursor.getString(11));
                it.setRangeTransaksiKarcisAkhir(cursor.getString(12));
                it.setRangeTransaksiKarcis(cursor.getString(13));

                // Menambahkan data ke dalam list
                list.add(it);
            } while (cursor.moveToNext());
            cursor.close();
        }
        // return list
        return list;
    }

    public ArrayList getProdukById(String id) {
        ArrayList list = new ArrayList();

        String selectQuery = "SELECT * FROM " + TABLE_PRODUK +" WHERE "+col_id +"=="+id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Perulangan semua data untuk dimasukkan kedalam list
        if (cursor.moveToFirst()) {
            do {
                com.dnhsolution.restokabmalang.sistem.produk.ItemProduk it = new com.dnhsolution.restokabmalang.sistem.produk.ItemProduk();
                it.setId_barang(cursor.getString(0));
                it.setNama_barang(cursor.getString(2));
                it.setHarga(cursor.getString(3));
                it.setKeterangan(cursor.getString(4));
                it.setUrl_image(cursor.getString(5));
                // Menambahkan data ke dalam list
                list.add(it);
            } while (cursor.moveToNext());
        }

        // return list
        return list;
    }

    //UPDATE DATA
    public int update_produk(ItemProduk ip) {
        SQLiteDatabase db = this.getWritableDatabase();
        //String today = getCurrentDate();

        ContentValues values = new ContentValues();
        values.put(col_nama_produk, ip.getNama_produk());
        values.put(col_harga, ip.getHarga());
        values.put(col_keterangan, ip.getKeterangan());
        values.put(col_status, ip.getStatus());
        values.put(col_foto, ip.getFoto());
        values.put(col_ispajak, ip.getIsPajak());
        values.put(col_jns_produk, ip.getJenisProduk());

        return db.update(TABLE_PRODUK, values, col_id + " = ?",
                new String[] { String.valueOf(ip.getId()) });
    }

    public int updateNomorTerakhirKarcisProduk(String id, String nomorTerakhir, String seriProduk) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(col_range_transaksi_karcis_awal, nomorTerakhir);
        values.put(col_seri_produk, seriProduk);

        return db.update(TABLE_PRODUK, values, col_id + " = ?",
                new String[] { id });
    }

    public int updateDataTersimpan(ItemTersimpan it) {
        SQLiteDatabase db = this.getWritableDatabase();
        //String today = getCurrentDate();

        ContentValues values = new ContentValues();


        values.put(col_status, it.getStatus());

        return db.update(TABLE_TRANSAKSI, values, col_id + " = ?",
                new String[] { String.valueOf(it.getId()) });
    }

}
