package com.dnhsolution.restokabmalang.tersimpan;

/**
 * Created by KHAN on 08/04/2018.
 */
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UploadData {
    public static final String UPLOAD_URL= Url.setKeranjangTransaksi;

    private int serverResponseCode;

    public String uploadData(String paramsArray) {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String tgl_penelitian="";



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
            dos.writeBytes("Content-Disposition: form-data; name=\"paramsArray\""
                    + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(paramsArray);
            dos.writeBytes(lineEnd);


            serverResponseCode = conn.getResponseCode();
            dos.flush();
            dos.close();
                //return conn.getResponseMessage();
//            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            String info = ex.getMessage();
        } catch (Exception ec) {
            ec.printStackTrace();
            String info = ec.getMessage();
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
                String info = ioex.getMessage();
            }
            return sb.toString();
        }else {

            return "Could not upload";

        }
    }



}
