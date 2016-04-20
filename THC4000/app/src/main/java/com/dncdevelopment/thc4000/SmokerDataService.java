package com.dncdevelopment.thc4000;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

///**
// * Created by AbelardoJose on 4/13/2016.
// */

public class SmokerDataService extends Service {
    private static final String TAG = "SmokerDataService";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothDevice mBluetoothDevice;
    private Handler mReceiverHandler = new Handler();
    private Handler mResponseHandler = new Handler();
    private int totalTime = 5;
    private int timeLeft;
    private Timer mTimer = new Timer();
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public static Intent newIntent(Context context, BluetoothDevice bluetoothDevice) {
        Intent i = new Intent(context, SmokerDataService.class);
        i.putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice);
        return i;
    }

    public void setRingtone () {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (mBluetoothDevice == null) {
            Log.d(TAG, "Bluetooth device not found");
            stopSelf();


        }



        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //        mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//
//        //Resources resources = getResources();
//        Intent i = SmokerDataActivity.newIntent(this, mBluetoothDevice);
//        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
//        Notification notification = new NotificationCompat.Builder(this)
//                .setTicker("Check me out!")
//                .setSmallIcon(android.R.drawable.ic_menu_report_image)
//                .setContentTitle("ALERT!!!")
//                .setContentText("Over here")
//                .setContentIntent(pi)
//                .setAutoCancel(true)
//                .build();
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        notificationManager.notify(0, notification);

    private void timerTick() {

        if (timeLeft != 0) {
            timeLeft--;
            if (timeLeft == 0) {
                mTimer.cancel();
                // TODO: notify the user and play ringtone
            }
        }
    }

    private void startTimer(int timerValue) {
        mTimer.cancel();
        timeLeft = timerValue + 1; // Timer start with one second less so I added the second back
        TimerTask mTicker = new TimerTask() {
            @Override
            public void run() {
                mResponseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        timerTick();
                    }
                });
            }
        };
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(mTicker, 0, 1000);
    }

    private String parseTimer(int timeLeft) {
        int hours = timeLeft / 3600;
        int minutes = (timeLeft % 3600) / 60;
        int seconds = (timeLeft % 3600) % 60;

        String time = "";

        if (hours < 10) {
            time += "0";
        }

        time += hours + ":";

        if (minutes < 10) {
            time += "0";
        }

        time += minutes + ":";

        if (seconds < 10) {
            time += "0";
        }

        time += seconds;

        return time;
    }

    private void setupChat() {
        mConnectThread = new ConnectThread(mBluetoothDevice);
        mConnectThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mConnectThread.cancel();
        //mParserHandler.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    void manageConnectedSocket(BluetoothSocket socket) {
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
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
        private String parseResult[];

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
