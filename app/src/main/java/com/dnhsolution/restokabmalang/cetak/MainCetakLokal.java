package com.dnhsolution.restokabmalang.cetak;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.database.DatabaseHandler;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.zj.btsdk.BluetoothService;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainCetakLokal extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, BluetoothHandler.HandlerInterface{

    RecyclerView rvData;
    List<ItemProduk> itemProduk;
    LinearLayout linearLayout;

    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private RecyclerView.Adapter adapter;

    TextView tvSubtotal, tvDisc, tvJmlDisc, tvTotal, tv_status,tvJmlPajak;
    Button btnKembali, btnCetak, btnPilih;

    private final String _tag = getClass().getSimpleName();
    public static final int RC_BLUETOOTH = 0;
    public static final int RC_CONNECT_DEVICE = 1;
    public static final int RC_ENABLE_BLUETOOTH = 2;

    private BluetoothService mService = null;
    private boolean isPrinterReady = false;


    SharedPreferences sharedPreferences;
    String idTrx="";
    DatabaseHandler databaseHandler;
    private String tipeStruk;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);
        String label = sharedPreferences.getString(Url.setLabel, "Belum disetting");
        String tema = sharedPreferences.getString(Url.setTema, "0");
        tipeStruk = sharedPreferences.getString(Url.SESSION_TIPE_STRUK, "");

        databaseHandler = new DatabaseHandler(MainCetakLokal.this);

        if(tema.equalsIgnoreCase("0")){
            MainCetakLokal.this.setTheme(R.style.Theme_First);
        }else if(tema.equalsIgnoreCase("1")){
            MainCetakLokal.this.setTheme(R.style.Theme_Second);
        }else if(tema.equalsIgnoreCase("2")){
            MainCetakLokal.this.setTheme(R.style.Theme_Third);
        }else if(tema.equalsIgnoreCase("3")){
            MainCetakLokal.this.setTheme(R.style.Theme_Fourth);
        }else if(tema.equalsIgnoreCase("4")){
            MainCetakLokal.this.setTheme(R.style.Theme_Fifth);
        }else if(tema.equalsIgnoreCase("5")){
            MainCetakLokal.this.setTheme(R.style.Theme_Sixth);
        }


        setContentView(R.layout.activity_main_cetak);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(label);

        rvData = findViewById(R.id.recyclerView);
        linearLayout = findViewById(R.id.linearLayout);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvJmlPajak = findViewById(R.id.tvJmlPajak);
        tvDisc = findViewById(R.id.tvDisc);
        tvJmlDisc = findViewById(R.id.tvJmlDisc);
        tvTotal = findViewById(R.id.tvTotal);
        tv_status = findViewById(R.id.tv_status);

        btnCetak = findViewById(R.id.btnCetak);
        btnKembali = findViewById(R.id.btnKembali);
        btnPilih = findViewById(R.id.btnPilihBT);

        itemProduk = new ArrayList<>();
        adapter = new AdapterProduk(itemProduk, MainCetakLokal.this);

        linearLayoutManager = new LinearLayoutManager(MainCetakLokal.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(rvData.getContext(), linearLayoutManager.getOrientation());

        rvData.setHasFixedSize(true);
        rvData.setLayoutManager(linearLayoutManager);
        //recyclerView.addItemDecoration(dividerItemDecoration);
        rvData.setAdapter(adapter);

        btnKembali.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        btnPilih.setOnClickListener(v -> {
            if (!mService.isAvailable()) {
                Log.i(_tag, "printText: perangkat tidak support bluetooth");
                return;
            }
            if (mService.isBTopen()) {
                startActivityForResult(new Intent(MainCetakLokal.this, DeviceActivity.class), RC_CONNECT_DEVICE);
            }
            else{
                requestBluetooth();
            }
        });

        if(!getIntent().getStringExtra("idTrx").isEmpty()){
            idTrx= getIntent().getStringExtra("idTrx");
            getData();
        }


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
        SQLiteDatabase db = databaseHandler.getReadableDatabase();

        //data transaksi
        Cursor cTrx = db.rawQuery("select id, omzet, disc_rp, pajak_rp from transaksi where id='" + idTrx + "'", null);
        cTrx.moveToFirst();
        int id_trx = cTrx.getInt(0);
        int omzet = Integer.parseInt(cTrx.getString(1));
        int disc_rp = Integer.parseInt(cTrx.getString(2));
        int pajak_rp = Integer.parseInt(cTrx.getString(3));
        int sub_total = omzet+disc_rp-pajak_rp;
        tvSubtotal.setText(currencyFormatter(String.valueOf(sub_total)));
        tvJmlPajak.setText(currencyFormatter(String.valueOf(pajak_rp)));
        tvJmlDisc.setText(currencyFormatter(String.valueOf(disc_rp)));
        tvTotal.setText(currencyFormatter(String.valueOf(omzet)));
        cTrx.close();

        //detail transaksi
        Cursor cDetailTrx = db.rawQuery("select dt.nama_produk, dt.qty, dt.harga,p.ispajak " +
                "from detail_transaksi dt LEFT JOIN produk p ON dt.id_produk = p.id " +
                "where id_trx ='" + idTrx + "'", null);
        if (cDetailTrx.moveToFirst()){
            int no = 1;
            do {
                // Passing values
                int total_harga = Integer.parseInt(cDetailTrx.getString(1)) * Integer.parseInt(cDetailTrx.getString(2));
                ItemProduk id = new ItemProduk();
                id.setNo(no);
                id.setNama_produk(cDetailTrx.getString(0));
                id.setQty(cDetailTrx.getString(1));
                id.setHarga(currencyFormatter(cDetailTrx.getString(2)));
                id.setIsPajak(cDetailTrx.getString(3));
                id.setTotal_harga(currencyFormatter(String.valueOf(total_harga)));

                itemProduk.add(id);
                no++;
                // Do something Here with values
            } while(cDetailTrx.moveToNext());
        }

        cDetailTrx.close();
        db.close();
        adapter.notifyDataSetChanged();

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
                    Log.i(_tag, "onActivityResult: bluetooth aktif");
                } else
                    Log.i(_tag, "onActivityResult: bluetooth harus aktif untuk menggunakan fitur ini");
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
            Log.i(_tag, "printText: perangkat tidak support bluetooth");
            return;
        }
        if (isPrinterReady) {

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
                String nama_produk = itemProduk.get(i).getNama_produk();
                String qty = itemProduk.get(i).getQty();
                String harga = itemProduk.get(i).getHarga();
                String total_harga = itemProduk.get(i).getTotal_harga();

                mService.write(PrinterCommands.ESC_ALIGN_LEFT);
                mService.sendMessage(nama_produk, "");
                writePrint(PrinterCommands.ESC_ALIGN_CENTER, gantiKetitik(harga)+" x "+qty+" : "+gantiKetitik(total_harga));
            }

            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage("--------------------------------", "");
            String subTotal = tvSubtotal.getText().toString().replace(".","");

            writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Subtotal : "+gantiKetitik(subTotal));

            if(tipeStruk.equalsIgnoreCase("1")) {
                writePrint(PrinterCommands.ESC_ALIGN_CENTER, "Pajak : " + tvJmlPajak.getText().toString());
            }

            writePrint(PrinterCommands.ESC_ALIGN_CENTER, tvDisc.getText().toString()+" : "+tvJmlDisc.getText().toString());

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
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
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

    public String currencyFormatter(String num) {
        double m = Double.parseDouble(num);
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        return formatter.format(m);
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
