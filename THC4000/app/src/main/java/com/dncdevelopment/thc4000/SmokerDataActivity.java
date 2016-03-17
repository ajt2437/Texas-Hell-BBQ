package com.dncdevelopment.thc4000;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class SmokerDataActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 3;
    private BluetoothAdapter mBluetoothAdapter = null;
    private static final String TAG = "SmokerDataActivity";

    private BluetoothServerSocket mServerSocket;
    private AcceptThread mAcceptThread;
    private ConnectedThread mConnectedThread;
    private TextView mBluetoothStatusTextView;
    private Button mConnectButton;
    private TextView mMessageTextView;
    private TextView mMessageStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoker_data);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBluetoothStatusTextView = (TextView) findViewById(R.id.status_bluetooth_text_view);
        mConnectButton = (Button) findViewById(R.id.connect_button);
        mMessageTextView = (TextView) findViewById(R.id.message_text_view);
        mMessageStatusTextView = (TextView) findViewById(R.id.status_message_text_view);

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        setupChat();
    }

    private void setupChat() {

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }

    public void connected(BluetoothSocket socket) {
        if (socket != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        Toast.makeText(this, "Bluetooth connected", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        r = mConnectedThread;
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                UUID serverUUID = UUID.fromString("ef7da76a-760c-4a38-93bb-6e3550c2615b");
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("THC4000", serverUUID);
            } catch (IOException e) { }
            mServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    connected(socket);
                    try {
                        mServerSocket.close();
                        break;
                    }
                    catch (IOException e) {}
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mServerSocket.close();
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
            byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mInputStream.read(buffer);
                    
                    // TODO: parsing
                    mMessageTextView.setText(buffer.toString());
                    // information obtained
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
