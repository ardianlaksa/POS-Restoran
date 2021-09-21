package com.dnhsolution.restokabmalang.sistem.produk;

/**
 * Created by KHAN on 08/04/2018.
 */

import android.util.Log;
import com.dnhsolution.restokabmalang.utilities.Url;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UploadData {
    public static final String UPLOAD_URL= Url.serverPos +"UpdateData";
    public static final String TAMBAH_URL= Url.serverPos +"TambahData";

    private int serverResponseCode;

    public String uploadDataUmum(
            String nama_barang, String keterangan, String harga, String id_barang,
            String foto_lama, String foto_baru) {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;


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

            if(foto_baru.equalsIgnoreCase("")){
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"s_foto_baru\""
                        + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("0");
                dos.writeBytes(lineEnd);
            }else{
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"s_foto_baru\""
                        + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("1");
                dos.writeBytes(lineEnd);

                String pat = foto_baru;
                File sourceFile = new File(pat);
                FileInputStream fileInputStream = new FileInputStream(sourceFile);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"foto_baru\";filename=\""+ pat + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                bytesAvailable = fileInputStream.available();
                Log.i("Huzza", "Initial .available : " + bytesAvailable);
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                fileInputStream.close();
            }



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

    public String uploadDataBaru(
            String nama_barang, String keterangan, String harga, String foto, String id_tmp_usaha) {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;


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

            String pat = foto;
            File sourceFile = new File(pat);
            FileInputStream fileInputStream = new FileInputStream(sourceFile);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"foto\";filename=\""+ pat + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            bytesAvailable = fileInputStream.available();
            Log.i("Huzza", "Initial .available : " + bytesAvailable);
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            fileInputStream.close();

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

//    public String uploadFile(String file, String bphtb_id) {
//
//        String fileName = file;
//
//        HttpURLConnection conn = null;
//        DataOutputStream dos = null;
//        String lineEnd = "\r\n";
//        String twoHyphens = "--";
//        String boundary = "*****";
//        int bytesRead, bytesAvailable, bufferSize;
//        byte[] buffer;
//        int maxBufferSize = 1 * 1024 * 1024;
//
//        File sourceFile = new File(file);
//        if (!sourceFile.isFile()) {
//            Log.e("Huzza", "Source File Does not exist");
//            return null;
//        }
//
//        try {
//            FileInputStream fileInputStream = new FileInputStream(sourceFile);
//
//            URL url = new URL(UPLOAD_URL_FILE);
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
//            conn.setUseCaches(false);
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Connection", "Keep-Alive");
//            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//            //conn.setRequestProperty("myFile", fileName);
//            //conn.setRequestProperty("myFileImage", fileNameImage);
//            dos = new DataOutputStream(conn.getOutputStream());
//
//            dos.writeBytes(twoHyphens + boundary + lineEnd);
//            dos.writeBytes("Content-Disposition: form-data; name=\"bphtb_id\""
//                    + lineEnd);
//            dos.writeBytes(lineEnd);
//            dos.writeBytes(bphtb_id);
//            dos.writeBytes(lineEnd);
//
//            //Audio
//
//            dos.writeBytes(twoHyphens + boundary + lineEnd);
//            dos.writeBytes("Content-Disposition: form-data; name=\"bphtb_foto\";filename=\"" + fileName + "\"" + lineEnd);
//            dos.writeBytes(lineEnd);
//
//            bytesAvailable = fileInputStream.available();
//            Log.i("Huzza", "Initial .available : " + bytesAvailable);
//
//            bufferSize = Math.min(bytesAvailable, maxBufferSize);
//            buffer = new byte[bufferSize];
//
//            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//            while (bytesRead > 0) {
//                dos.write(buffer, 0, bufferSize);
//                bytesAvailable = fileInputStream.available();
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//            }
//
//            dos.writeBytes(lineEnd);
//            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//
//
//            serverResponseCode = conn.getResponseCode();
//
//            fileInputStream.close();
//            dos.flush();
//            dos.close();
//        } catch (MalformedURLException ex) {
//            ex.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (serverResponseCode == 200) {
//            StringBuilder sb = new StringBuilder();
//            try {
//                BufferedReader rd = new BufferedReader(new InputStreamReader(conn
//                        .getInputStream()));
//                String line;
//                while ((line = rd.readLine()) != null) {
//                    sb.append(line);
//                }
//                rd.close();
//            } catch (IOException ioex) {
//            }
//            return sb.toString();
//        }else {
//            return "Could not upload";
//        }
//    }

}
