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

    //kolom transaksi
    public final String col_tanggal_trx="tanggal_trx";
    public final String col_disc="disc";
    public final String col_omzet="omzet";
    public final String col_id_pengguna="id_pengguna";
    public final String col_disc_rp="disc_rp";

    //kolom detail_transaksi
    public final String col_id_trx= "id_trx";
    public final String col_id_produk = "id_id_produk";
    public final String col_qty = "qty";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // CREATE TABLE
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABEL_PRODUK = "CREATE TABLE " + TABLE_PRODUK + "("
                + col_id + " INTEGER PRIMARY KEY AUTOINCREMENT," + col_id_tempat_usaha + " TEXT," + col_nama_produk + " TEXT,"+
                col_harga + " TEXT,"+ col_keterangan + " TEXT,"+ col_foto + " TEXT,"+ col_status + " TEXT"+")";

        String CREATE_TABEL_TRANSAKSI = "CREATE TABLE " + TABLE_TRANSAKSI + "("
                + col_id + " INTEGER PRIMARY KEY AUTOINCREMENT," + col_tanggal_trx + " TEXT," + col_disc + " TEXT,"+
                col_omzet + " TEXT,"+ col_id_pengguna + " TEXT,"+ col_id_tempat_usaha + " TEXT,"+ col_disc_rp + " TEXT,"+ col_status + " TEXT"+")";

        String CREATE_TABEL_DETAIL_TRANSAKSI = "CREATE TABLE " + TABLE_DETAIL_TRANSAKSI + "("
                + col_id + " INTEGER PRIMARY KEY AUTOINCREMENT," + col_id_trx + " TEXT," + col_id_produk + " TEXT,"+
                col_nama_produk + " TEXT,"+ col_qty + " TEXT,"+ col_harga + " TEXT"+")";

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

        values.put(col_id_tempat_usaha, ip.getId_tempat_usaha());
        values.put(col_nama_produk, ip.getNama_produk());
        values.put(col_harga, ip.getHarga());
        values.put(col_keterangan, ip.getKeterangan());
        values.put(col_foto, ip.getFoto());
        values.put(col_status, ip.getStatus());

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

        // memasukkan data
        db.insert(TABLE_DETAIL_TRANSAKSI, null, values);
        db.close(); // Menutup koneksi database
    }

    //DELETE DATA
    public void delete_transaksi(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_TRANSAKSI);
        db.close();
    }

    public void delete_detail_transaksi(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_TRANSAKSI);
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

    //COUNT DATA
    public int CountDataTersimpan() {
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

    //GET DATA
    public ArrayList getDataTersimpan() {
        ArrayList list = new ArrayList();


        String selectQuery = "SELECT "+col_id+","+col_tanggal_trx+","+col_disc+
                ","+col_omzet+","+col_disc_rp+","+col_status+" FROM " + TABLE_TRANSAKSI +
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
                ","+col_omzet+","+col_disc_rp+","+col_status+" FROM " + TABLE_TRANSAKSI +" WHERE "+col_status+"=='0'"+
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

                // Menambahkan data ke dalam list
                list.add(it);
            } while (cursor.moveToNext());
        }
        // return list
        return list;
    }

    public ArrayList getDetailTersimpan(String idTrx) {
        ArrayList list = new ArrayList();

        String selectQuery = "SELECT "+col_id+","+col_nama_produk+","+col_qty+","+col_harga+","+col_id_produk+
                " FROM " + TABLE_DETAIL_TRANSAKSI +" WHERE "+col_id_trx+"=='"+idTrx+"'";

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
                ","+col_harga+","+col_keterangan+","+col_foto+","+col_status+" FROM " + TABLE_PRODUK;

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

    //get from bhs.kotlin transaksi
    public List<com.dnhsolution.restokabmalang.sistem.produk.ItemProduk> getDataProduk2() {
        List<com.dnhsolution.restokabmalang.sistem.produk.ItemProduk> list = new ArrayList<>();


        String selectQuery = "SELECT "+col_id+","+col_id_tempat_usaha+","+col_nama_produk+
                ","+col_harga+","+col_keterangan+","+col_foto+","+col_status+" FROM " + TABLE_PRODUK;

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

        return db.update(TABLE_PRODUK, values, col_id + " = ?",
                new String[] { String.valueOf(ip.getId()) });
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
