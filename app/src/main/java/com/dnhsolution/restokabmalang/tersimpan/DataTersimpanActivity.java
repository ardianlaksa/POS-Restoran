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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnhsolution.restokabmalang.MainActivity;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.database.DatabaseHandler;
import com.dnhsolution.restokabmalang.utilities.CheckNetwork;
import com.dnhsolution.restokabmalang.utilities.OnDataFetched;
import com.dnhsolution.restokabmalang.utilities.TaskRunner;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.dnhsolution.restokabmalang.utilities.dialog.AdapterWizard;
import com.dnhsolution.restokabmalang.utilities.dialog.ItemView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataTersimpanActivity extends AppCompatActivity implements OnDataFetched {

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
    private boolean isRunnerRunning = false;
    private Menu menuTemp;
    private int statusJaringan = 0;
    private final String _tag = getClass().getSimpleName();
    private String tipeStruk;
    private String idPengguna;
    private String idTmptUsaha;
    private String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        tipeStruk = sharedPreferences.getString(Url.SESSION_TIPE_STRUK, "");
        idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "0");
        idTmptUsaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "");
        uuid = sharedPreferences.getString(Url.SESSION_UUID, "");

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
        fab_upload.setOnClickListener(view -> {
            if(new CheckNetwork().checkingNetwork(this) && statusJaringan == 1){
                jml_data = databaseHandler.CountDataTersimpanUpload();
                AlertDialog.Builder builder = new AlertDialog.Builder(DataTersimpanActivity.this);
                builder.setMessage("Lanjut upload " + jml_data + " data ke server ?");
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "Ya",
                        (dialog, id) -> {
                            if(new CheckNetwork().checkingNetwork(getApplicationContext())){
                                SendData();
                            }else{
                                Toast.makeText(DataTersimpanActivity.this, R.string.tidak_terkoneksi_internet, Toast.LENGTH_SHORT).show();
                            }
                        });

                builder.setNegativeButton(
                        "Tidak",
                        (dialog, id) -> dialog.cancel());

                AlertDialog alert = builder.create();
                alert.show();
            } else Toast.makeText(this, R.string.tidak_terkoneksi_internet, Toast.LENGTH_SHORT).show();
        });

        dataTersimpan = new ArrayList<>();
        adapter = new TersimpanAdater(dataTersimpan, DataTersimpanActivity.this);

        linearLayoutManager = new LinearLayoutManager(DataTersimpanActivity.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), linearLayoutManager.getOrientation());

        rvData.setHasFixedSize(true);
        rvData.setLayoutManager(linearLayoutManager);
        rvData.setAdapter(adapter);

        String url = Url.serverPos + "getProduk?idTmpUsaha=" + idTmptUsaha+
                "&jenisProduk=0&idPengguna="+idPengguna+"&uuid="+uuid;
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(!isRunnerRunning) {
                    TaskRunner runner1 = new TaskRunner();
                    runner1.executeAsync(new CekDataTersimpanNetworkTask(DataTersimpanActivity.this, url));
                    isRunnerRunning = true;
                }
                handler.postDelayed(this, 5000);
            }
        });

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
//                    Log.d("hhhhuu", String.valueOf(RecyclerViewClickedItemPos));
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

            protected void onProgressUpdate(Integer... values) { progressdialog.setProgress(values[0]); }

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
                            sendNotification(datax +" data berhasil diupload !");
                            if (progressdialog.isShowing())
                                progressdialog.dismiss();
                            Log.d("INFORMASI", "suksesUpload: ");
                            for (ItemTersimpan v : dataTersimpan) {
                                int posisi = v.getNo()-1;
                                dataTersimpan.get(posisi).setStatus("1");
                                adapter.notifyItemChanged(posisi);
                            }
                            showHideFabUpload();
                        }
                    }else if(hasil.equalsIgnoreCase("Gagal.")){
                        if (progressdialog.isShowing())
                            progressdialog.dismiss();
                        Log.d("INFORMASI", "gagalUpload: ");
                    }else{
                        if (progressdialog.isShowing())
                            progressdialog.dismiss();
                        Log.d("INFORMASI", "gagalUpload: "+s);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... params) {

                List<ItemTersimpan> listDataTersimpanUpload = databaseHandler.getDataTersimpanUpload();

                String msg = "";
                for (ItemTersimpan f : listDataTersimpanUpload) {
                    try {
                        UploadData u = new UploadData();

                        JSONObject rootObject = new JSONObject();
                        rootObject.put("uuid",uuid);
                        rootObject.put("idTmptUsaha",idTmptUsaha);
                        rootObject.put("user",idPengguna);
                        rootObject.put("disc_rp",f.getDisc_rp());
                        String disc = f.getDisc();
                        if(disc.isEmpty()){
                            disc = f.getDisc_rp();
                        }
                        rootObject.put("disc",disc);
                        rootObject.put("omzet",f.getOmzet());
                        rootObject.put("pajakRp",f.getPajakRp());

                        JSONArray jsonArr = new JSONArray();

                        List<ItemDetailTersimpan> listDetailTrx=databaseHandler.getDetailTersimpan(String.valueOf(f.getId()));
                        for(ItemDetailTersimpan d:listDetailTrx){
                            JSONObject pnObj = new JSONObject();
                            pnObj.put("idProduk",d.getId_produk());
                            pnObj.put("nmProduk",d.getNama());
                            pnObj.put("qty",d.getQty());
                            pnObj.put("hrgProduk",d.getHarga());
                            pnObj.put("isPajak",d.getIsPajak());
                            pnObj.put("tipeStruk",tipeStruk);
                            jsonArr.put(pnObj);
                        }
                        rootObject.put("produk",jsonArr);
                        msg = u.uploadData(rootObject.toString());

                        Log.e("INFO_PENTING", "NUMBER: "+status);
                        status++;
                        publishProgress(status);

                        Log.e(_tag, msg);
                        JSONObject jsonMsg = new JSONObject(msg);

                        if(jsonMsg.getString("message").equalsIgnoreCase("Berhasil.")){
                            datax++;
                            databaseHandler.updateDataTersimpan(new ItemTersimpan(f.getId(),"1"));
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

        SQLiteDatabase db = databaseHandler.getReadableDatabase();
        Cursor mCount= db.rawQuery("select * from detail_transaksi where id_trx='" + idTrx + "'", null);
        mCount.moveToFirst();
        int countTersimpan= mCount.getInt(0);
        Log.d("DETAIL_TERSIMPAN", "getDetailTersimpan: "+mCount.getInt(0)+"/"+mCount.getInt(1)+"/"+mCount.getString(2)+"/"+mCount.getString(3)+"/"+mCount.getInt(4)+"/"+mCount.getInt(5));
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
                it.setIsPajak(f.isPajak);
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
        int jml_data = databaseHandler.CountDataTersimpan2();

        if(jml_data==0){
            tvKet.setVisibility(View.VISIBLE);
            fab_upload.setVisibility(View.GONE);
        }else{
            showHideFabUpload();
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
                it.setPajakRp(f.getPajakRp());
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
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo_sipanji));
        String app_name = String.valueOf(R.string.app_name);
        builder.setContentTitle(app_name);
        builder.setContentText(message);
       // builder.setSubText("Tap to view the website.");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(1, builder.build());
    }

    private void showHideFabUpload(){
        int jmlData = databaseHandler.CountDataTersimpanUpload();
        if(jmlData==0){
            fab_upload.setVisibility(View.GONE);
            tv_count.setText("");
            tv_count.setVisibility(View.INVISIBLE);
        }else if(jmlData<=9){
            tv_count.setVisibility(View.VISIBLE);
            tv_count.setText(String.valueOf(jmlData));
        }else {
            tv_count.setVisibility(View.VISIBLE);
            tv_count.setText("9+");
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_tersimpan, menu);
        menuTemp = menu;
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_menu_bantuan) {
            tampilAlertDialogTutorial();
            return true;
        }else if (item.getItemId() == R.id.action_menu_wifi) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void tampilAlertDialogTutorial(){
        AlertDialog alertDialog = new AlertDialog.Builder(DataTersimpanActivity.this).create();
        final View rowList = getLayoutInflater().inflate(R.layout.dialog_tutorial, null);
        ListView listView = rowList.findViewById(R.id.listView);
        AdapterWizard tutorialArrayAdapter;
        ArrayList<ItemView> arrayList = new ArrayList<>();
        arrayList.add(new ItemView("1", "Status Belum Sinkron warna orange : menandakan data transaksi belum tersinkron dengan server."));
        arrayList.add(new ItemView("2", "Status Sudah sinkron warna hijau : menandakan data transaksi sudah tersinkron dengan server."));
        arrayList.add(new ItemView("3", "Saat ada data dengan status Belum Sinkron, akan tampil tombol icon Upload warna hijau. Tombol ini digunakan untuk upload data transaksi yang Belum Sinkron ke server."));
        arrayList.add(new ItemView("4", "Angka background merah diatas tombol upload menandakan jumlah data dengan status Belum Sinkron."));
        tutorialArrayAdapter = new AdapterWizard(DataTersimpanActivity.this, arrayList);
        listView.setAdapter(tutorialArrayAdapter);
        alertDialog.setView(rowList);
        alertDialog.show();
    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void setDataInPageWithResult(@Nullable Object result) {
        if(result == null) return;
        gantiIconWifi(result.toString().equalsIgnoreCase("1"));
        isRunnerRunning = false;
    }

    public void gantiIconWifi(Boolean value){
        if(value) {
            menuTemp.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_wifi_24_green));
            statusJaringan = 1;
        } else {
            menuTemp.getItem(1).setIcon(ContextCompat.getDrawable(this,R.drawable.ic_baseline_wifi_24_gray));
            statusJaringan = 0;
        }
    }
}
