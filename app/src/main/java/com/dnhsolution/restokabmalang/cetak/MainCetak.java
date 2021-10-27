package com.dnhsolution.restokabmalang.cetak;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.*;
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
import com.dnhsolution.restokabmalang.utilities.Url;
import com.zj.btsdk.BluetoothService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class MainCetak extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, BluetoothHandler.HandlerInterface{

    RecyclerView rvData;
    List<ItemProduk> itemProduk;
    LinearLayout linearLayout;
    private final String _tag = getClass().getSimpleName();

    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private RecyclerView.Adapter adapter;

    TextView tvSubtotal, tvDisc, tvJmlDisc, tvTotal, tv_status;
    private TextView tvJmlPajak;
    Button btnKembali, btnCetak, btnPilih;

    private final String TAG = MainActivity.class.getSimpleName();
    public static final int RC_BLUETOOTH = 0;
    public static final int RC_CONNECT_DEVICE = 1;
    public static final int RC_ENABLE_BLUETOOTH = 2;

    private BluetoothService mService = null;
    private boolean isPrinterReady = false;
    private String tipeStruk;

    SharedPreferences sharedPreferences;
    private String idTrx = "0";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        String tema = sharedPreferences.getString(Url.setTema, "0");
        tipeStruk = sharedPreferences.getString(Url.SESSION_TIPE_STRUK, "");
        Intent intent = getIntent();
        idTrx = intent.getStringExtra("getIdItem");
        Log.i(_tag,idTrx);

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
        tvJmlPajak = (TextView)findViewById(R.id.tvJmlPajak);
        tvTotal = (TextView)findViewById(R.id.tvTotal);
        tv_status = (TextView)findViewById(R.id.tv_status);

        btnCetak = (Button)findViewById(R.id.btnCetak);
        btnKembali = (Button)findViewById(R.id.btnKembali);
        btnPilih = (Button)findViewById(R.id.btnPilihBT);

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
//                startActivity(new Intent(MainCetak.this, MainActivity.class));
//                finishAffinity();
                setResult(RESULT_OK);
                finish();
            }
        });

        btnPilih.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mService.isAvailable()) {
                    Log.i(TAG, "printText: perangkat tidak support bluetooth");
                    return;
                }
                if (mService.isBTopen())
                    startActivityForResult(new Intent(MainCetak.this, DeviceActivity.class), RC_CONNECT_DEVICE);
                else
                    requestBluetooth();
            }
        });

        getData();

        ButterKnife.bind(this);
        setupBluetooth();

        SharedPreferences sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        String bt_device = sharedPreferences.getString(Url.SESSION_PRINTER_BT, "");

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled :)
        } else {
            if(!bt_device.equalsIgnoreCase("")){
                BluetoothDevice mDevice = mService.getDevByMac(bt_device);
                mService.connect(mDevice);
                Log.d("BT_DEVICE", bt_device);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        getSupportActionBar().setTitle(label);
//        String tema = sharedPreferences.getString(Url.setTema, "0");
//
//        if(tema.equalsIgnoreCase("0")){
//            MainCetak.this.setTheme(R.style.Theme_First);
//        }else if(tema.equalsIgnoreCase("1")){
//            MainCetak.this.setTheme(R.style.Theme_Second);
//        }else if(tema.equalsIgnoreCase("2")){
//            MainCetak.this.setTheme(R.style.Theme_Third);
//        }else if(tema.equalsIgnoreCase("3")){
//            MainCetak.this.setTheme(R.style.Theme_Fourth);
//        }else if(tema.equalsIgnoreCase("4")){
//            MainCetak.this.setTheme(R.style.Theme_Fifth);
//        }else if(tema.equalsIgnoreCase("5")){
//            MainCetak.this.setTheme(R.style.Theme_Sixth);
//        }
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
//                        tvDisc.setText(json.getString("disc"));
                        tvJmlDisc.setText(json.getString("jml_disc"));
                        tvJmlPajak.setText(json.getString("jml_pajak"));
                        tvTotal.setText(json.getString("total"));

                        int i;
                        for (i = 1; i < jsonArray.length(); i++) {
                            try {

                                JSONObject jO = jsonArray.getJSONObject(i);
                                ItemProduk id = new ItemProduk();
                                id.setNo(i);
                                id.setNama_produk(jO.getString("nama_produk"));
                                id.setQty(jO.getString("qty"));
                                id.setIsPajak(jO.getString("ispajak"));
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
                params.put("idTrx",idTrx);

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
        tv_status.setText("Tidak terhubung ke perangkat printer");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
            String alamat = sharedPreferences.getString(Url.SESSION_ALAMAT, "0");

            String tanggal = getDateTime();
            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage(nm_tempat_usaha, "");

            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage(alamat, "");
            mService.write(PrinterCommands.ESC_ENTER);

            mService.write(PrinterCommands.ESC_ALIGN_LEFT);
            mService.sendMessage("Tanggal : "+tanggal, "");

            mService.write(PrinterCommands.ESC_ALIGN_LEFT);
            mService.sendMessage("Kasir   : "+nm_tempat_usaha, "");

            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage("--------------------------------", "");

            for (int i=0; i<itemProduk.size(); i++) {
//                if(itemProduk.get(i).getIsPajak().equalsIgnoreCase("1")) {
                    String nama_produk = itemProduk.get(i).getNama_produk();
                    String qty = itemProduk.get(i).getQty();
                    String harga = itemProduk.get(i).getHarga();
                    String total_harga = itemProduk.get(i).getTotal_harga();

                    mService.write(PrinterCommands.ESC_ALIGN_LEFT);
                    mService.sendMessage(nama_produk, "");
                    writePrint(PrinterCommands.ESC_ALIGN_CENTER, gantiKetitik(harga) + " x " + qty + " : " + gantiKetitik(total_harga));
//                }
            }

            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage("--------------------------------", "");
            String subTotal = tvSubtotal.getText().toString().replace(".","");

            writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Subtotal : "+gantiKetitik(subTotal));

            writePrint(PrinterCommands.ESC_ALIGN_CENTER, tvDisc.getText().toString()+" : "+tvJmlDisc.getText().toString());

            if(tipeStruk.equalsIgnoreCase("1")) {
                writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Pajak : " + tvJmlPajak.getText().toString());
            }

            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage("--------------------------------", "");

//            writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Total : "+tvTotal.getText().toString());
            String a = "Total : "+gantiKetitik(tvTotal.getText().toString());
            printConfig(a,1,2,1);
            mService.write(PrinterCommands.ESC_ENTER);

            printConfig("- Terima Kasih -",3,1,1);
            mService.write(PrinterCommands.ESC_ENTER);
            mService.write(PrinterCommands.ESC_ENTER);
        } else {
            Toast.makeText(this, "Tidak terhubung printer manapun !", Toast.LENGTH_SHORT).show();
//            if (mService.isBTopen())
//                startActivityForResult(new Intent(this, DeviceActivity.class), RC_CONNECT_DEVICE);
//            else
//                requestBluetooth();
            if (!mService.isBTopen())
                requestBluetooth();
        }
    }

    private String gantiKetitik(String value){
        value = value.replace(",",".");
        return value;
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
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    void writePrint(byte[] align, String msg){
        mService.write(align);
        StringBuilder space = new StringBuilder("   ");
        int l = msg.length();
        if(l < 31){
            for(int x = 31-l; x >= 0; x--) {
                space.append(" ");
            }
        }
        msg = msg.replace(" : ", space.toString());
        mService.write( msg.getBytes());
    }

    protected void printConfig(String bill, int size, int style, int align)
    {
        //size 1 = large, size 2 = medium, size 3 = small
        //style 1 = Regular, style 2 = Bold
        //align 0 = left, align 1 = center, align 2 = right

        try{

            byte[] format = new byte[]{27,33, 0};
            byte[] change = new byte[]{27,33, 0};

            mService.write(format);

            //different sizes, same style Regular
            if (size==1 && style==1)  //large
            {
                change[2] = (byte) (0x10); //large
                mService.write(change);
            }else if(size==2 && style==1) //medium
            {
                //nothing to change, uses the default settings
            }else if(size==3 && style==1) //small
            {
                change[2] = (byte) (0x3); //small
                mService.write(change);
            }

            //different sizes, same style Bold
            if (size==1 && style==2)  //large
            {
                change[2] = (byte) (0x10 | 0x8); //large
                mService.write(change);
            }else if(size==2 && style==2) //medium
            {
                change[2] = (byte) (0x8);
                mService.write(change);
            }else if(size==3 && style==2) //small
            {
                change[2] = (byte) (0x3 | 0x8); //small
                mService.write(change);
            }


            switch (align) {
                case 0:
                    //left align
                    mService.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    mService.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    mService.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }
            mService.write(bill.getBytes());
            mService.write(new byte[]{PrinterCommands.LF});
        }catch(Exception ex){
            Log.e("error", ex.toString());
        }
    }
    protected void printNewLine() {
        mService.write(PrinterCommands.FEED_LINE);
    }
}
