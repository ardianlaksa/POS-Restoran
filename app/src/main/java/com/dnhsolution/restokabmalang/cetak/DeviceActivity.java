package com.dnhsolution.restokabmalang.cetak;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.databinding.ActivityDeviceBinding;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.zj.btsdk.BluetoothService;

import java.util.Set;

public class DeviceActivity extends AppCompatActivity {

//    @BindView(R.id.paired_devices)
    ListView lvPairedDevice;
//    @BindView(R.id.new_devices)
    ListView lvNewDevice;
//    @BindView(R.id.title_new_devices)
    TextView tvNewDevice;
//    @BindView(R.id.title_paired_devices)
    TextView tvPairedDevice;

    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothService mService = null;
    private ArrayAdapter<String> newDeviceAdapter;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    newDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
                }
//                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                    setTitle("Pilih Perangkat");
//                    if (newDeviceAdapter.getCount() == 0) {
//                        newDeviceAdapter.add("Perangkat tidak ditemukan");
//                    }
//                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.dnhsolution.restokabmalang.databinding.ActivityDeviceBinding binding = ActivityDeviceBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setTitle("Perangkat Bluetooth");
//        ButterKnife.bind(this);
        lvPairedDevice = binding.pairedDevices;
        lvNewDevice = binding.newDevices;
        tvNewDevice = binding.titleNewDevices;
        tvPairedDevice = binding.titlePairedDevices;

        ArrayAdapter<String> pairedDeviceAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        lvPairedDevice.setAdapter(pairedDeviceAdapter);
        lvPairedDevice.setOnItemClickListener(mDeviceClickListener);

        newDeviceAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        lvNewDevice.setAdapter(newDeviceAdapter);
        lvNewDevice.setOnItemClickListener(mDeviceClickListener);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, intentFilter);

        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, intentFilter);

        mService = new BluetoothService(this, null);

        Set<BluetoothDevice> pairedDevice = mService.getPairedDev();

        if (pairedDevice.size() > 0) {
            tvPairedDevice.setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevice) {
                pairedDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevice = "Tidak ada perangkat terhubung!";
            pairedDeviceAdapter.add(noDevice);
        }

        binding.buttonScan.setOnClickListener(view1 -> {
            doDiscovery();
            view.setVisibility(View.GONE);
        });
    }

    private final AdapterView.OnItemClickListener mDeviceClickListener = (parent, view, position, id) -> {
        mService.cancelDiscovery();

        String info = ((TextView) view).getText().toString();
        String address = info.substring(info.length() - 17);

        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
        SharedPreferences sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE);

        //membuat editor untuk menyimpan data ke shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //menambah data ke editor
        editor.putString(Url.SESSION_PRINTER_BT, address);

        //menyimpan data ke editor
        editor.apply();

        setResult(RESULT_OK, intent);
        finish();
    };

    private void doDiscovery() {
        setTitle("Mencari perangkat...");
        tvNewDevice.setVisibility(View.VISIBLE);

        if (mService.isDiscovering()) {
            mService.cancelDiscovery();
        }

        mService.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.cancelDiscovery();
        }
        mService = null;
        unregisterReceiver(mReceiver);
    }
}
