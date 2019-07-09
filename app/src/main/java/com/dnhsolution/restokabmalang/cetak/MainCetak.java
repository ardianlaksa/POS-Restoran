package com.dnhsolution.restokabmalang.cetak;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dnhsolution.restokabmalang.MainActivity;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.keranjang.KeranjangActivity;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class MainCetak extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, BluetoothHandler.HandlerInterface{

    RecyclerView rvData;
    List<ItemProduk> itemProduk;
    LinearLayout linearLayout;

    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private RecyclerView.Adapter adapter;

    TextView tvSubtotal, tvDisc, tvJmlDisc, tvTotal, tv_status;
    Button btnKembali, btnCetak;

    private final String TAG = MainActivity.class.getSimpleName();
    public static final int RC_BLUETOOTH = 0;
    public static final int RC_CONNECT_DEVICE = 1;
    public static final int RC_ENABLE_BLUETOOTH = 2;

    private BluetoothService mService = null;
    private boolean isPrinterReady = false;


    SharedPreferences sharedPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        String tema = sharedPreferences.getString(Url.setTema, "0");

        if(tema.equalsIgnoreCase("0")){
            MainCetak.this.setTheme(R.style.Theme_First);
        }else if(tema.equalsIgnoreCase("1")){
            MainCetak.this.setTheme(R.style.Theme_Second);
        }else if(tema.equalsIgnoreCase("2")){
            MainCetak.this.setTheme(R.style.Theme_Third);
        }else if(tema.equalsIgnoreCase("3")){
            MainCetak.this.setTheme(R.style.Theme_Fourth);
        }else if(tema.equalsIgnoreCase("4")){
            MainCetak.this.setTheme(R.style.Theme_Fifth);
        }else if(tema.equalsIgnoreCase("5")){
            MainCetak.this.setTheme(R.style.Theme_Sixth);
        }


        setContentView(R.layout.activity_main_cetak);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(label);

        rvData = (RecyclerView) findViewById(R.id.recyclerView);
        linearLayout = (LinearLayout)findViewById(R.id.linearLayout);

        tvSubtotal = (TextView)findViewById(R.id.tvSubtotal);
        tvDisc = (TextView)findViewById(R.id.tvDisc);
        tvJmlDisc = (TextView)findViewById(R.id.tvJmlDisc);
        tvTotal = (TextView)findViewById(R.id.tvTotal);
        tv_status = (TextView)findViewById(R.id.tv_status);

        btnCetak = (Button)findViewById(R.id.btnCetak);
        btnKembali = (Button)findViewById(R.id.btnKembali);

        itemProduk = new ArrayList<>();
        adapter = new AdapterProduk(itemProduk, MainCetak.this);

        linearLayoutManager = new LinearLayoutManager(MainCetak.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(rvData.getContext(), linearLayoutManager.getOrientation());

        rvData.setHasFixedSize(true);
        rvData.setLayoutManager(linearLayoutManager);
        //recyclerView.addItemDecoration(dividerItemDecoration);
        rvData.setAdapter(adapter);

        btnKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainCetak.this, MainActivity.class));
                finish();
            }
        });

        getData();

        ButterKnife.bind(this);
        setupBluetooth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        getSupportActionBar().setTitle(label);
        String tema = sharedPreferences.getString(Url.setTema, "0");

        if(tema.equalsIgnoreCase("0")){
            MainCetak.this.setTheme(R.style.Theme_First);
        }else if(tema.equalsIgnoreCase("1")){
            MainCetak.this.setTheme(R.style.Theme_Second);
        }else if(tema.equalsIgnoreCase("2")){
            MainCetak.this.setTheme(R.style.Theme_Third);
        }else if(tema.equalsIgnoreCase("3")){
            MainCetak.this.setTheme(R.style.Theme_Fourth);
        }else if(tema.equalsIgnoreCase("4")){
            MainCetak.this.setTheme(R.style.Theme_Fifth);
        }else if(tema.equalsIgnoreCase("5")){
            MainCetak.this.setTheme(R.style.Theme_Sixth);
        }
    }

    private void getData() {
        itemProduk.clear();
        adapter.notifyDataSetChanged();
        final ProgressDialog progressDialog = new ProgressDialog(MainCetak.this);
        progressDialog.setMessage("Mencari data...");
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(MainCetak.this);
        String url = Url.serverPos+"getDataStruk";
        //Toast.makeText(WelcomeActivity.this, url, Toast.LENGTH_LONG).show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    JSONObject json = jsonArray.getJSONObject(0);
                    String pesan = json.getString("pesan");
                    if(pesan.equalsIgnoreCase("0") || pesan.equalsIgnoreCase("2")){
                        Toast.makeText(MainCetak.this, "Data kosong !", Toast.LENGTH_SHORT).show();
                    }else if(pesan.equalsIgnoreCase("1")){

                        tvSubtotal.setText(json.getString("sub_total"));
                        tvDisc.setText("Disc ("+json.getString("disc")+"%)");
                        tvJmlDisc.setText(json.getString("jml_disc"));
                        tvTotal.setText(json.getString("total"));

                        int i;
                        for (i = 1; i < jsonArray.length(); i++) {
                            try {

                                JSONObject jO = jsonArray.getJSONObject(i);
                                ItemProduk id = new ItemProduk();
                                id.setNo(i);
                                id.setNama_produk(jO.getString("nama_produk"));
                                id.setQty(jO.getString("qty"));
                                id.setHarga(jO.getString("harga"));
                                id.setTotal_harga(jO.getString("total_harga"));

                                itemProduk.add(id);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                progressDialog.dismiss();
                            }
                        }


                    }else{
                        Toast.makeText(MainCetak.this, "Jaringan masih sibuk !", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(SinkronisasiActivity.this, response, Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(MainCetak.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                SharedPreferences sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
                final String kd_pengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "0");
                final String id_tempat_usaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "0");

                params.put("kd_pengguna",kd_pengguna);
                params.put("id_tempat_usaha",id_tempat_usaha);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        queue.add(stringRequest);
    }

    @AfterPermissionGranted(RC_BLUETOOTH)
    private void setupBluetooth() {
        String[] params = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN};
        if (!EasyPermissions.hasPermissions(this, params)) {
            EasyPermissions.requestPermissions(this, "You need bluetooth permission",
                    RC_BLUETOOTH, params);
            return;
        }
        mService = new BluetoothService(this, new BluetoothHandler(this));
    }

    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // TODO: 10/11/17 do something
    }

    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // TODO: 10/11/17 do something
    }

    public void onDeviceConnected() {
        isPrinterReady = true;
        tv_status.setText("Terhubung dengan perangkat");
    }

    public void onDeviceConnecting() {
        tv_status.setText("Sedang menghubungkan...");
    }

    public void onDeviceConnectionLost() {
        isPrinterReady = false;
        tv_status.setText("Koneksi perangkat terputus");
    }

    public void onDeviceUnableToConnect() {
        tv_status.setText("Tidak dapat terhubung ke perangkat");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "onActivityResult: bluetooth aktif");
                } else
                    Log.i(TAG, "onActivityResult: bluetooth harus aktif untuk menggunakan fitur ini");
                break;
            case RC_CONNECT_DEVICE:
                if (resultCode == RESULT_OK) {
                    String address = data.getExtras().getString(DeviceActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice mDevice = mService.getDevByMac(address);
                    mService.connect(mDevice);
                }
                break;
        }
    }

    @OnClick(R.id.btnCetak)
    public void printText(@Nullable View view) {
        if (!mService.isAvailable()) {
            Log.i(TAG, "printText: perangkat tidak support bluetooth");
            return;
        }
        if (isPrinterReady) {
//            if (etText.getText().toString().isEmpty()) {
//                Toast.makeText(this, "Cant print null text", Toast.LENGTH_SHORT).show();
//                return;
//            }

            SharedPreferences sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
            String nm_tempat_usaha = sharedPreferences.getString(Url.SESSION_NAMA_TEMPAT_USAHA, "0");


            String tanggal = getDateTime();
            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage(nm_tempat_usaha, "");

            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage("Jln. Malang Raya No. 40", "");
            mService.write(PrinterCommands.ESC_ENTER);

            mService.write(PrinterCommands.ESC_ALIGN_LEFT);
            mService.sendMessage("Tanggal : "+tanggal, "");

            mService.write(PrinterCommands.ESC_ALIGN_LEFT);
            mService.sendMessage("Kasir   : "+nm_tempat_usaha, "");

            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage("--------------------------------", "");

            for (int i=0; i<itemProduk.size(); i++) {
                String nama_produk = itemProduk.get(i).getNama_produk();
                String qty = itemProduk.get(i).getQty();
                String harga = itemProduk.get(i).getHarga();
                String total_harga = itemProduk.get(i).getTotal_harga();

                mService.write(PrinterCommands.ESC_ALIGN_LEFT);
                mService.sendMessage(nama_produk, "");
                writePrint(PrinterCommands.ESC_ALIGN_CENTER, harga+" x "+qty+" : "+total_harga);
            }

            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage("--------------------------------", "");

            writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Subtotal : "+tvSubtotal.getText().toString());
            writePrint(PrinterCommands.ESC_ALIGN_CENTER, tvDisc.getText().toString()+" : "+tvJmlDisc.getText().toString());

            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage("--------------------------------", "");

            writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Total : "+tvTotal.getText().toString());
            mService.write(PrinterCommands.ESC_ENTER);

//            writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Tunai : 50.000");
//            writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Kembali : 5.000");
//            mService.write(PrinterCommands.ESC_ENTER);

            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage("- Terima Kasih -", "");
            mService.write(PrinterCommands.ESC_ENTER);
        } else {
            if (mService.isBTopen())
                startActivityForResult(new Intent(this, DeviceActivity.class), RC_CONNECT_DEVICE);
            else
                requestBluetooth();
        }
    }

    private void requestBluetooth() {
        if (mService != null) {
            if (!mService.isBTopen()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, RC_ENABLE_BLUETOOTH);
            }
        }
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    void writePrint(byte[] align, String msg){
        mService.write(align);
        String space = "   ";
        int l = msg.length();
        if(l < 31){
            for(int x = 31-l; x >= 0; x--) {
                space = space+" ";
            }
        }
        msg = msg.replace(" : ", space);
        mService.write( msg.getBytes());
    }
}
