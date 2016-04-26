package com.dncdevelopment.thc4000;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

///**
// * Created by AbelardoJose on 4/13/2016.
// */

public class SmokerDataService extends Service {
    private static final String TAG = "SmokerDataService";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String EXTRA_MEDIA_POSITION = "MediaPlayer";
    private static final String ALARM_DONE = "AlarmDone";
    private static final String EXTRA_TEMP = "Temperature";
    private static final String EXTRA_MESSAGE = "AlertMessage";

    // Model
    private StringBuffer sbu;
    private String str = "";
    private String mMessage = "";
    private Handler mReceiverHandler = new Handler();
    private Handler mResponseHandler = new Handler();
    private int currentETemp = 0;
    private int currentITemp = 0;
    private boolean acquiredSettings = false;
    private int setTemperature = 0;
    private MediaPlayer alarmPlayer;
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothDevice mBluetoothDevice;
    private int timeLeft;
    private Timer mTimer = new Timer();
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private final IBinder mBinder = new LocalBinder();
    private Notification notification;
    NotificationManagerCompat notificationManager;
    private ArrayList<String> uriList = new ArrayList<String>();
    int setRingtone;
    int ETempCount = 0;
    int prevETemp = 0;
    int ITempCount = 0;
    int prevITemp = 0;
    int ATimeCount = 0;
    int prevAValue = 0;
    private boolean isTimeSet = false;
    private boolean isTempSet = false;
    private boolean startConditionCheck = false;

    public class LocalBinder extends Binder {
        SmokerDataService getService() {
            return SmokerDataService.this;
        }
    }

    public static Intent newIntent(Context context, BluetoothDevice bluetoothDevice, int position) {
        Intent i = new Intent(context, SmokerDataService.class);
        i.putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice);
        i.putExtra(EXTRA_MEDIA_POSITION, position);
        return i;
    }

    public String getInput() {
        return str;
    }

    public int getTimer() {
        return timeLeft;
    }

    public int getCurrentETemp() {
        return currentETemp;
    }

    public int getCurrentITemp() {
        return currentITemp;
    }

    public void setAlarmPlayer (int position) {
        setRingtone = position;
    }

    public void stopAlarm() {
        if (alarmPlayer.isPlaying()) {
            alarmPlayer.pause();
            alarmPlayer.seekTo(0);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        setRingtone = intent.getIntExtra(EXTRA_MEDIA_POSITION, 0);
        Log.d(TAG, "onStartCommand");
        if (mBluetoothDevice == null) {
            Log.d(TAG, "Bluetooth device not found");
            stopSelf();
        }
        alarmPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(uriList.get(setRingtone)));
        notificationManager = NotificationManagerCompat.from(this);
        setupChat();
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_ALARM);
        Cursor cursor = manager.getCursor();

        while (cursor.moveToNext()) {
            String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            uriList.add(uri + "/" + id);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mConnectThread.cancel();
        Log.i(TAG, "Background thread destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
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

    private void timerTick() {

        if (timeLeft != 0) {
            timeLeft--;
            if (timeLeft == 0) {
                mTimer.cancel();
                // TODO: notify the user and play ringtone
                alertUser(ALARM_DONE, null);
            }
        }
    }

    public void alertUser (String tag, Intent data) {
        Resources resources = getResources();
        Context context = getBaseContext();
        //getApplicationContext();
        ActivityManager am = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(10);
        ActivityManager.RunningTaskInfo task = tasks.get(0); // get current task
        ComponentName topActivity = task.topActivity;
        Intent notificationIntent = new Intent();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setTicker("THC-4000")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(Uri.parse(uriList.get(setRingtone)))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        PendingIntent pi;
        boolean found = false;

        for (int i = 0; i < tasks.size(); i++)
        {
            task = tasks.get(i);
            topActivity = task.topActivity;
            String packageName = topActivity.getPackageName();
            if(packageName.equalsIgnoreCase("com.dncdevelopment.thc4000")) {
                //your app is open
                // Now build an Intent that will bring this task to the front

                //gets the top activity from root activity

                notificationIntent = SmokerDataActivity.newIntent(context, mBluetoothDevice);
                notificationIntent.setComponent(topActivity);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                break;
            }
        }

        //Testing
        //notificationIntent = new Intent();
        notificationIntent.setComponent(topActivity);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        pi = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pi);

        switch (tag) {
            case Parser.startETempTag:
                int currentTemp = data.getIntExtra(EXTRA_TEMP, 0);
                if (isTimeSet && isTempSet) {
                    if(currentTemp >= setTemperature){
                        startConditionCheck = true;
                    }
                }
                if(startConditionCheck) {
                    //Temperature is too high
                    if (currentTemp > (setTemperature + 50)) {

                        notification = notificationBuilder
                                .setContentTitle("Food overheating")
                                .setContentText("Possible fire hazard")
                                .build();
                        notificationManager.notify(0, notification);
                    }
                    else if(currentTemp < (setTemperature - 50)) {
                        notification = notificationBuilder
                                .setContentTitle("Food undercooked")
                                .setContentText("Add more fuel")
                                .build();
                        notificationManager.notify(0, notification);
                    }
                }
                break;
            case ALARM_DONE:
                notification = notificationBuilder
                        .setContentTitle("Food is ready")
                        .setContentText("Time to eat!")
                        .build();
                notificationManager.notify(0, notification);
                break;
        }
    }

    private void setupChat() {
        mConnectThread = new ConnectThread(mBluetoothDevice);
        mConnectThread.start();
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
        public static final char startTimeTag = 'A';
        public static final char startITempTag = 'I';
        public static final char startETempTag = 'E';
        public static final char startSetTag = 'S';

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
            int startTag;
            int dataChar;

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                if (!isTimeSet || !isTempSet) {
                    write("*". getBytes());
                }
                try {
                    // Read from the InputStream
                    while((startTag = mInputStream.read()) == -1){
                    }
                    while((dataChar = mInputStream.read()) != startTag){
                        if(dataChar != -1){
                            str += (char) dataChar;
                        }
                    }

                    // Identify the tag and take the correction action
                    int dataI;
                    Intent data = new Intent();
                    dataI = Integer.parseInt(str);
                    switch (startTag) {
                        case startTimeTag:
                            startTimer(dataI);
                            write("-".getBytes());
                            str = "";
                            isTimeSet = true;
                            break;
                        case startSetTag:
                            if (dataI > 999) {
                                str = "";
                                break;
                            }
                            if (isTempSet) {
                                acquiredSettings = false;
                            }
                            write("-".getBytes());
                            setTemperature = dataI;
                            isTempSet = true;
                            str = "";
                            break;

                        case startITempTag:
                            if (dataI > 999) {
                                str = "";
                                break;
                            }
                            write("-".getBytes());
                            currentITemp = dataI;
                            str = "";
                            break;
                        case startETempTag:
                            if (dataI > 999) {
                                str = "";
                                break;
                            }
                            write("-".getBytes());
                            currentETemp = dataI;
                            data.putExtra(EXTRA_TEMP, dataI);
                            alertUser(Parser.startETempTag, data);
                            str = "";
                            break;
                    }
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
