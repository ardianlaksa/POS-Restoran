package com.dnhsolution.restokabmalang.tersimpan;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.database.DatabaseHandler;
import com.dnhsolution.restokabmalang.utilities.CheckNetwork;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataTersimpanActivity extends AppCompatActivity {

    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;

    SharedPreferences sharedPreferences;
    RecyclerView rvData;
    List<ItemTersimpan> dataTersimpan;
    LinearLayout linearLayout;

    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private RecyclerView.Adapter adapter;
    View ChildView;
    int RecyclerViewClickedItemPos;

    DatabaseHandler databaseHandler;
    TextView tvKet;
    FloatingActionButton fab_upload;

    DetailTersimpanAdapter detailTersimpanAdater;
    List<ItemDetailTersimpan> itemDetailTersimpans = new ArrayList<>();
    TextView tv_count;
    int jml_data = 0;
    ProgressDialog progressdialog;
    int datax = 0;

    int status = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        String tema = sharedPreferences.getString(Url.setTema, "0");
        if(tema.equalsIgnoreCase("0")){
            DataTersimpanActivity.this.setTheme(R.style.Theme_First);
        }else if(tema.equalsIgnoreCase("1")){
            DataTersimpanActivity.this.setTheme(R.style.Theme_Second);
        }else if(tema.equalsIgnoreCase("2")){
            DataTersimpanActivity.this.setTheme(R.style.Theme_Third);
        }else if(tema.equalsIgnoreCase("3")){
            DataTersimpanActivity.this.setTheme(R.style.Theme_Fourth);
        }else if(tema.equalsIgnoreCase("4")){
            DataTersimpanActivity.this.setTheme(R.style.Theme_Fifth);
        }else if(tema.equalsIgnoreCase("5")){
            DataTersimpanActivity.this.setTheme(R.style.Theme_Sixth);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_tersimpan);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(label);

        databaseHandler = new DatabaseHandler(DataTersimpanActivity.this);

        rvData = (RecyclerView) findViewById(R.id.rvData);

        tvKet = (TextView)findViewById(R.id.tvKet);
        tv_count = (TextView)findViewById(R.id.text_count);


        fab_upload = findViewById(R.id.fab);
        fab_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jml_data = databaseHandler.CountDataTersimpan();
                AlertDialog.Builder builder = new AlertDialog.Builder(DataTersimpanActivity.this);
                builder.setMessage("Lanjut upload " + jml_data + " data ke server ?");
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "Ya",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(new CheckNetwork().checkingNetwork(getApplicationContext())){
                                    SendData();
                                }else{
                                    Toast.makeText(DataTersimpanActivity.this, "Tidak ada koneksi internet !", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                builder.setNegativeButton(
                        "Tidak",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        dataTersimpan = new ArrayList<>();
        adapter = new TersimpanAdater(dataTersimpan, DataTersimpanActivity.this);

        linearLayoutManager = new LinearLayoutManager(DataTersimpanActivity.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), linearLayoutManager.getOrientation());

        rvData.setHasFixedSize(true);
        rvData.setLayoutManager(linearLayoutManager);
        //recyclerView.addItemDecoration(dividerItemDecoration);
        rvData.setAdapter(adapter);

        getData();

        rvData.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override public boolean onSingleTapUp(MotionEvent motionEvent) {
                    return true;
                }

            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                ChildView = rvData.findChildViewUnder(e.getX(), e.getY());

                if(ChildView != null && gestureDetector.onTouchEvent(e)) {
                    RecyclerViewClickedItemPos = rvData.getChildAdapterPosition(ChildView);
                    Log.d("hhhhuu", String.valueOf(RecyclerViewClickedItemPos));
                    int id_data = dataTersimpan.get(RecyclerViewClickedItemPos).getId();
                    DialogDetailTrx(id_data);
                    //Toast.makeText(DataTersimpanActivity.this, String.valueOf(id_data), Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        int jml_datax = databaseHandler.CountDataTersimpan();
        if(jml_datax==0){
            tv_count.setVisibility(View.INVISIBLE);
        }else if(jml_datax<=9){
            tv_count.setVisibility(View.VISIBLE);
            tv_count.setText(String.valueOf(jml_datax));
        }else if(jml_datax>9){
            tv_count.setVisibility(View.VISIBLE);
            tv_count.setText("9+");
        }
    }

    private void SendData() {
        class SendData extends AsyncTask<Void, Integer, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressdialog = new ProgressDialog(DataTersimpanActivity.this);

                progressdialog.setIndeterminate(false);

                progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

                progressdialog.setCancelable(true);

                progressdialog.setMessage("Upload data ke server ...");

                progressdialog.setMax(jml_data);

                progressdialog.show();

                progressdialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Minimize",
                        (DialogInterface.OnClickListener) null);

            }

            protected void onProgressUpdate(Integer... values)
            {
                progressdialog.setProgress(values[0]);

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.d("HASIL", s);
                String hasil="";
                if (progressdialog.isShowing())
                    progressdialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    hasil = jsonObject.getString("message");

                    Log.d("HASIL_JSON", jsonObject.getString("message"));
                    if(hasil.equalsIgnoreCase("Berhasil.")){
                        if(datax==jml_data){
                            sendNotification(String.valueOf(datax)+" data berhasil diupload !");
                            if (progressdialog.isShowing())
                                progressdialog.dismiss();

                            Log.d("INFORMASI", "suksesUpload: ");
                        }else{

                        }

                    }else if(hasil.equalsIgnoreCase("Gagal.")){
                        if (progressdialog.isShowing())
                            progressdialog.dismiss();
                        Log.d("INFORMASI", "gagalUpload: ");
                    }else{
                        if (progressdialog.isShowing())
                            progressdialog.dismiss();
                        Log.d("INFORMASI", "gagalUpload: "+s);
//                    String ns = Context.NOTIFICATION_SERVICE;
//                    NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
//                    nMgr.cancel(1);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            protected String doInBackground(Void... params) {

                SharedPreferences sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);

                String idTmptUsaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "");
                String pengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "");

                List<ItemTersimpan> listDataTersimpan = databaseHandler.getDataTersimpanUpload();
                String msg = null;
                for (ItemTersimpan f : listDataTersimpan) {
                    try {
                        UploadData u = new UploadData();

                        JSONObject rootObject = new JSONObject();
                        rootObject.put("idTmptUsaha",idTmptUsaha);
                        rootObject.put("user",pengguna);
                        rootObject.put("disc_rp",f.getDisc_rp());
                        String disc = f.getDisc();
                        if(disc.isEmpty()){
                            disc = f.getDisc_rp();
                        }
                        rootObject.put("disc",disc);
                        rootObject.put("omzet",f.getOmzet());

                        JSONArray jsonArr = new JSONArray();

                        List<ItemDetailTersimpan> listDetailTrx=databaseHandler.getDetailTersimpan(String.valueOf(f.getId()));
                        for(ItemDetailTersimpan d:listDetailTrx){
                            JSONObject pnObj = new JSONObject();
                            pnObj.put("idProduk",d.getId_produk());
                            pnObj.put("nmProduk",d.getNama());
                            pnObj.put("qty",d.getQty());
                            pnObj.put("hrgProduk",d.getHarga());
                            jsonArr.put(pnObj);
                        }
                        rootObject.put("produk",jsonArr);
                        msg = u.uploadData(rootObject.toString());

                        Log.e("INFO_PENTING", "NUMBER: "+status);
                        status++;
                        publishProgress(status);

                        JSONObject jsonMsg = new JSONObject(msg);


                        if(jsonMsg.getString("message").equalsIgnoreCase("Berhasil.")){
                            datax++;
                            databaseHandler.updateDataTersimpan(new ItemTersimpan(
                                    f.getId(),
                                    "1"

                            ));
                        }else{
                            Log.d("INFO_PENTING", "doInBackground: "+msg);
                        }
                    } catch (Exception e) {
                        cancel(true);
                        e.printStackTrace();
                    }

                }

                return msg;
            }
        }
        SendData uv = new SendData();
        uv.execute();
    }

    private void DialogDetailTrx(int idTrx) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_detail_rekap_harian, null);

        final RecyclerView rvDetail;
        final TextView tvTrx;


        rvDetail = (RecyclerView)dialogView.findViewById(R.id.recyclerView);
        tvTrx = (TextView) dialogView.findViewById(R.id.tvNoTrx);

        tvTrx.setText(String.valueOf(idTrx));

        detailTersimpanAdater= new DetailTersimpanAdapter(itemDetailTersimpans, DataTersimpanActivity.this);
        RecyclerView.LayoutManager mLayoutManagerss = new LinearLayoutManager(DataTersimpanActivity.this);
        rvDetail.setLayoutManager(mLayoutManagerss);
        rvDetail.setItemAnimator(new DefaultItemAnimator());
        rvDetail.setAdapter(detailTersimpanAdater);

        getDetailTersimpan(idTrx);

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void getDetailTersimpan(int idTrx) {
        itemDetailTersimpans.clear();
        detailTersimpanAdater.notifyDataSetChanged();

        SQLiteDatabase db = databaseHandler.getReadableDatabase();
        Cursor mCount= db.rawQuery("select * from detail_transaksi where id_trx='" + String.valueOf(idTrx) + "'", null);
        mCount.moveToFirst();
        int countTersimpan= mCount.getInt(0);
        Log.d("DETAIL_TERSIMPAN", "getDetailTersimpan: "+mCount.getInt(0)+"/"+mCount.getInt(1)+"/"+mCount.getString(2)+"/"+mCount.getString(3)+"/"+mCount.getInt(4)+"/"+mCount.getInt(5));
      //  Toast.makeText(this, String.valueOf(idTrx), Toast.LENGTH_LONG).show();
        mCount.close();

        try {
            List<ItemDetailTersimpan> listDetailTersimpan = databaseHandler.getDetailTersimpan(String.valueOf(idTrx));
            int no = 1;
            for (ItemDetailTersimpan f : listDetailTersimpan) {
                ItemDetailTersimpan it = new ItemDetailTersimpan();
                it.setId(f.getId());
                it.setNama(f.getNama());
                it.setQty(f.getQty());
                it.setHarga(f.getHarga());
                it.setNo(no);
                int total = (Integer.parseInt(f.getQty())) * (Integer.parseInt(f.getHarga()));
                it.setTotal(String.valueOf(total));
                itemDetailTersimpans.add(it);
                no++;
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        detailTersimpanAdater.notifyDataSetChanged();
    }

    private void getData() {
        dataTersimpan.clear();
        adapter.notifyDataSetChanged();

        int jml_data = databaseHandler.CountDataTersimpan2();

        if(jml_data==0){
            tvKet.setVisibility(View.VISIBLE);
            fab_upload.setVisibility(View.GONE);

        }else{
            int jml_data2 = databaseHandler.CountDataTersimpan();
            if(jml_data2==0){
                fab_upload.setVisibility(View.GONE);
            }else{
                fab_upload.setVisibility(View.VISIBLE);
            }
            tvKet.setVisibility(View.GONE);
        }

        try {
            List<ItemTersimpan> listDataTersimpan = databaseHandler.getDataTersimpan();
            int nomer = 1;
            for (ItemTersimpan f : listDataTersimpan) {
                ItemTersimpan it = new ItemTersimpan();
                it.setId(f.getId());
                it.setNo(nomer);
                it.setTanggal_trx(f.getTanggal_trx());
                it.setDisc(f.getDisc());
                it.setOmzet(f.getOmzet());
                it.setDisc_rp(f.getDisc_rp());
                it.setStatus(f.getStatus());
                dataTersimpan.add(it);

                nomer++;
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        Log.d("OP_TERUPDATE", String.valueOf(dataTersimpan.size()));
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        getSupportActionBar().setTitle(label);
        String tema = sharedPreferences.getString(Url.setTema, "0");
        if(tema.equalsIgnoreCase("0")){
            this.setTheme(R.style.Theme_First);
        }else if(tema.equalsIgnoreCase("1")){
            this.setTheme(R.style.Theme_Second);
        }else if(tema.equalsIgnoreCase("2")){
            this.setTheme(R.style.Theme_Third);
        }else if(tema.equalsIgnoreCase("3")){
            this.setTheme(R.style.Theme_Fourth);
        }else if(tema.equalsIgnoreCase("4")){
            this.setTheme(R.style.Theme_Fifth);
        }else if(tema.equalsIgnoreCase("5")){
            this.setTheme(R.style.Theme_Sixth);
        }
    }

    public void sendNotification(String message) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info);
        Intent intent = new Intent(getApplicationContext(), DataTersimpanActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo));
        String app_name = String.valueOf(R.string.app_name);
        builder.setContentTitle(app_name);
        builder.setContentText(message);
       // builder.setSubText("Tap to view the website.");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(1, builder.build());
    }

//    private void scheduleNotification (Notification notification , int delay) {
//        Intent notificationIntent = new Intent( this, MyNotificationPublisher. class ) ;
//        notificationIntent.putExtra(MyNotificationPublisher. NOTIFICATION_ID , 1 ) ;
//        notificationIntent.putExtra(MyNotificationPublisher. NOTIFICATION , notification) ;
//        PendingIntent pendingIntent = PendingIntent. getBroadcast ( this, 0 , notificationIntent , PendingIntent. FLAG_UPDATE_CURRENT ) ;
//        long futureInMillis = SystemClock. elapsedRealtime () + delay ;
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context. ALARM_SERVICE ) ;
//        assert alarmManager != null;
//        alarmManager.set(AlarmManager. ELAPSED_REALTIME_WAKEUP , futureInMillis , pendingIntent) ;
//    }
//    private Notification getNotification (String content) {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder( this, default_notification_channel_id ) ;
//        builder.setContentTitle( "Scheduled Notification" ) ;
//        builder.setContentText(content) ;
//        builder.setSmallIcon(R.drawable.ic_logo ) ;
//        builder.setAutoCancel( true ) ;
//        builder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
//        return builder.build() ;
//    }


}
