package com.dncdevelopment.thc4000;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.sqlite.SQLiteTableLockedException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views for testing
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private ImageView mBackgroundImage;

    private ListView mDevicesBondedList;

    private ArrayAdapter<String> mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private ArrayList<String> mDeviceList = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //String TAG = "Main Activity";

        mDevicesBondedList = (ListView) findViewById(R.id.device_list);

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
                mDeviceList.add(device.getName() + "\n" + device.getAddress());
            }
        }

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDeviceList);
        mDevicesBondedList.setAdapter(mAdapter);


        //mDevicesBondedList.setAdapter(mArrayAdapter);





        //mConversationArrayAdapter =

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
