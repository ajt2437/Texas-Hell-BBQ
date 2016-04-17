package com.dncdevelopment.thc4000;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.os.Handler;

public class SmokerDataActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter = null;
    private static final String TAG = "SmokerDataActivity";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // View
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private BluetoothDevice mBluetoothDevice;
    private TextView mBluetoothStatusTextView;
    private Button mStatusCheckButton;
    private TextView mInternalTemperatureTextView;
    private TextView mExternalTemperatureTextView;
    private TextView mMessageStatusTextView;
    private Button mClearButton;

    // Model
    private StringBuffer sbu;
    private String str = "";
    private String mMessage = "";
    private Handler mHandler = new Handler();
    private boolean connectedFlag = false;
    private Parser.TAGS tagId = Parser.TAGS.NOTFOUND;
    private Parser mParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoker_data);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mStatusCheckButton = (Button) findViewById(R.id.bluetooth_status_button);

        mInternalTemperatureTextView = (TextView) findViewById(R.id.internal_temperature_text_view);
        mExternalTemperatureTextView = (TextView) findViewById(R.id.external_temperature_text_view);

        mBluetoothDevice = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        mBluetoothStatusTextView.setText("Not Connected");

        mMessageStatusTextView = (TextView) findViewById(R.id. message_status);
        mMessageStatusTextView.setText("message shown here");

        mClearButton = (Button) findViewById(R.id.clear_buffer_button);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str = "";
                mMessageStatusTextView.setText("message shown here");
            }
        });

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }

        mStatusCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SmokerDataActivity.this, mBluetoothDevice.getName() + "\n" + mBluetoothDevice.getAddress(), Toast.LENGTH_SHORT).show();
                if (connectedFlag) {
                    mBluetoothStatusTextView.setText("Connected");
                } else {
                    mBluetoothStatusTextView.setText("Not Connected");
                }
            }
        });
        setupChat();
    }

    private void setupChat() {
        mConnectThread = new ConnectThread(mBluetoothDevice);
        mConnectThread.start();
    }

    public static Intent newIntent(Context packageContext, BluetoothDevice device) {
        Intent i = new Intent(packageContext, SmokerDataActivity.class);
        i.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
        return i;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConnectThread.cancel();
    }

    void manageConnectedSocket(BluetoothSocket socket) {
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        //mBluetoothStatusTextView.setText("Connected");
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        r = mConnectedThread;
        // Perform the write unsynchronized
        r.write(out);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            //It connected successfully if it reaches this point
            connectedFlag = true;

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //Get the input streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e) {

            }
            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }

        public void run() {
            final byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mInputStream.read(buffer);

                    // Send the obtained bytes to the UI activity
                    final int count = bytes;
                    mHandler.post(new Runnable() {
                        public void run() {

                            try {
                                sleep(5);
                            }
                            catch (InterruptedException e) {
                                Log.e(TAG, "Error: Sleep error", e);
                            }

                            // String input together
                            StringBuilder b = new StringBuilder();
                            for (int i = 0; i < count; ++i) {
                                String s = Integer.toString(buffer[i]);
                                b.append(s);
                                b.append(",");
                            }

                            String s = b.toString();
                            String[] chars = s.split(",");
                            sbu = new StringBuffer();
                            for (int i = 0; i < chars.length; i++) {
                                sbu.append((char) Integer.parseInt(chars[i]));
                            }

                            if (sbu.equals("\n") || sbu.equals("\r") || sbu.equals("\r\n")) {
                                str = "";
                                mMessage = "";
                                mMessageStatusTextView.setText("message shown here");
                            }
                            else {
                                str += sbu;
                                mMessageStatusTextView.setText(str);
                            }

                            // Parse data, identify start and stop tag to know we received all the data to display
                            mMessage = mParser.stringHandler(str);

                            tagId = mParser.getTagFound();

                            // Identify the tag and take the correction action
                            switch (tagId) {
                                //double dataD;

                                case TIME:

                                    break;

                                case ITEMP:
                                    double dataD = Double.parseDouble(mMessage);
                                    int dataI = (int) Math.ceil(dataD);
                                    mInternalTemperatureTextView.setText("" + dataI);
                                    str = "";
                                    mMessage = "";
                                    break;

                                case ETEMP:
                                    dataD = Double.parseDouble(mMessage);
                                    dataI = (int) Math.ceil(dataD);
                                    mExternalTemperatureTextView.setText("" + dataI);
                                    str = "";
                                    mMessage = "";
                                    break;

                                case ERROR:

                                    break;

                                case NOTFOUND:
                                    //str = "";
                            }
                        }
                    });
                }
                catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mOutputStream.write(buffer);

                // Share the sent message back to the UI Activity

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mSocket.close();
            }
            catch (IOException e) {}

        }
    }
}
