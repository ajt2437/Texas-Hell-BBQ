package com.dncdevelopment.thc4000;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_DISCOVERABLE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final String TAG = "MainActivity";

    // Layout Views for testing

    private ListView mDevicesBondedList;

    private BluetoothAdapter mBluetoothAdapter = null;
    private ArrayList<String> mDeviceList = new ArrayList<>();
    private ArrayList<BluetoothDevice> mDiscoveredDevices = new ArrayList<>();
    private ArrayList<BluetoothDevice> mPairedDevices = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    private BluetoothDevice selectedDevice = null;
    private Button mConnectButton;
    private Button mScanButton;
    private Spinner mSpinner;

    /* Register Receiver*/
    private IntentFilter discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            //Log.e(Tag, "got those bonds");

            for (BluetoothDevice device : pairedDevices) {
                //Add the name and address to an array adapter to show in a ListView
                mPairedDevices.add(device);
                mDeviceList.add(device.getName());
            }
        }

        mSpinner = (Spinner) findViewById(R.id.adapter_spinner);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //save the passed mac address
                selectedDevice = mPairedDevices.get(position);
                //Toast.makeText(MainActivity.this, passedMac.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // does nothing
            }
        });
        updateUI();

        mConnectButton = (Button) findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDevice == null) {
                    Toast.makeText(MainActivity.this, "Scan for devices", Toast.LENGTH_LONG).show();
                }
                else if (selectedDevice.getName().equals("THC-4000")) {
                    Intent i = SmokerDataActivity.newIntent(MainActivity.this, selectedDevice);
                    startActivity(i);
                }
                else {
                    Toast message = Toast.makeText(MainActivity.this, R.string.wrong_device_alert, Toast.LENGTH_SHORT);
                    TextView textView = (TextView) message.getView().findViewById(android.R.id.message);
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    message.show();

                }
                Log.d(TAG, "Testing");
            }
        });

//        mScanButton = (Button) findViewById(R.id.scan_button);
//        mScanButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                boolean success = mBluetoothAdapter.startDiscovery();
//
//                Toast.makeText(MainActivity.this, "Discovery: " + success, Toast.LENGTH_LONG).show();
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(mReceiver, discoveryFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mReceiver);
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mDiscoveredDevices.add(device);
                mDeviceList.add(device.getName());
                Toast.makeText(MainActivity.this, "Found new devices", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        }
    };

    private void updateUI() {

        if(mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDeviceList);
            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(mAdapter);
        }
        else {
            mAdapter.notifyDataSetChanged();
        }
    }

}
