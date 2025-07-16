package com.dnhsolution.restokabmalang.sistem.produk;

import android.util.Log;

import com.dnhsolution.restokabmalang.utilities.Url;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UploadData {
    public static final String UPLOAD_URL= Url.setUpdateProduk;
    public static final String TAMBAH_URL= Url.setTambahProduk;

    private int serverResponseCode;

    public String uploadDataUmum(String idPengguna,String uuid,
            String nama_barang, String keterangan, String harga, String id_barang,
            String foto_lama, String foto_baru, String slctdIspajak, String slctdTipeProduk) {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        try {

            URL url = new URL(UPLOAD_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"idPengguna\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(idPengguna);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uuid\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(uuid);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"nama_barang\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(nama_barang);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"keterangan\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(keterangan);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"isPajak\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(slctdIspajak);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"tipeProduk\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(slctdTipeProduk);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"harga\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(harga);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"id_barang\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(id_barang);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"foto_lama\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(foto_lama);
            dos.writeBytes(lineEnd);

//            if(foto_baru.equalsIgnoreCase("")){
//                dos.writeBytes(twoHyphens + boundary + lineEnd);
//                dos.writeBytes("Content-Disposition: form-data; name=\"s_foto_baru\""
//                        + lineEnd);
//                dos.writeBytes(lineEnd);
//                dos.writeBytes("0");
//                dos.writeBytes(lineEnd);
//            }else{
//                dos.writeBytes(twoHyphens + boundary + lineEnd);
//                dos.writeBytes("Content-Disposition: form-data; name=\"s_foto_baru\""
//                        + lineEnd);
//                dos.writeBytes(lineEnd);
//                dos.writeBytes("1");
//                dos.writeBytes(lineEnd);
//
////                String pat = foto_baru;
//                File sourceFile = new File(foto_baru);
//                FileInputStream fileInputStream = new FileInputStream(sourceFile);
//
//                dos.writeBytes(twoHyphens + boundary + lineEnd);
//                dos.writeBytes("Content-Disposition: form-data; name=\"foto_baru\";filename=\""+ foto_baru + "\"" + lineEnd);
//                dos.writeBytes(lineEnd);
//                bytesAvailable = fileInputStream.available();
//                Log.i("Huzza", "Initial .available : " + bytesAvailable);
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                buffer = new byte[bufferSize];
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//                while (bytesRead > 0) {
//                    dos.write(buffer, 0, bufferSize);
//                    bytesAvailable = fileInputStream.available();
//                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//                }
//                dos.writeBytes(lineEnd);
//                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//                fileInputStream.close();
//            }



            serverResponseCode = conn.getResponseCode();
            dos.flush();
            dos.close();
                //return conn.getResponseMessage();
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (serverResponseCode == 200) {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                        .getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
            } catch (IOException ioex) {
            }
            return sb.toString();
        }else {
            return "Could not upload";
        }
    }

    public String uploadDataBaru(String idPengguna,String uuid,
            String nama_barang, String keterangan, String harga, String foto, String id_tmp_usaha
            , String slctdIspajak, String slctdTipeProduk) {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        try {

            URL url = new URL(TAMBAH_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"idPengguna\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(idPengguna);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uuid\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(uuid);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"nama_barang\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(nama_barang);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"keterangan\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(keterangan);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"isPajak\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(slctdIspajak);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"tipeProduk\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(slctdTipeProduk);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"harga\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(harga);
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"id_tmp_usaha\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(id_tmp_usaha);
            dos.writeBytes(lineEnd);

//            String pat = foto;
//            File sourceFile = new File(pat);
//            FileInputStream fileInputStream = new FileInputStream(sourceFile);
//
//            dos.writeBytes(twoHyphens + boundary + lineEnd);
//            dos.writeBytes("Content-Disposition: form-data; name=\"foto\";filename=\""+ pat + "\"" + lineEnd);
//            dos.writeBytes(lineEnd);
//            bytesAvailable = fileInputStream.available();
//            Log.i("Huzza", "Initial .available : " + bytesAvailable);
//            bufferSize = Math.min(bytesAvailable, maxBufferSize);
//            buffer = new byte[bufferSize];
//            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//            while (bytesRead > 0) {
//                dos.write(buffer, 0, bufferSize);
//                bytesAvailable = fileInputStream.available();
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//            }
//            dos.writeBytes(lineEnd);
//            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//            fileInputStream.close();

            serverResponseCode = conn.getResponseCode();
            dos.flush();
            dos.close();
            //return conn.getResponseMessage();
//            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (Exception ec) {
            ec.printStackTrace();
        }

        if (serverResponseCode == 200) {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                        .getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
            } catch (IOException ioex) {
            }
            return sb.toString();
        }else {
            return "Could not upload";
        }
    }
}
