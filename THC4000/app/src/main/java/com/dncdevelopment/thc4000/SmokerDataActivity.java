package com.dncdevelopment.thc4000;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
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
    //private TextView mBluetoothStatusTextView;
    private TextView mInternalTemperatureTextView;
    private TextView mExternalTemperatureTextView;
    private TextView mMessageStatusTextView;
    private TextView mTimerTextView;

    // Model
    private StringBuffer sbu;
    private String str = "";
    private String mMessage = "";
    private Handler mReceiverHandler = new Handler();
    private Handler mResponseHandler = new Handler();
    private int totalTime = 5;
    private int timeLeft;
    private Timer mTimer = new Timer();

    private Switch mySwitch;

    private Spinner spinner;
    private ArrayList<String> uriList = new ArrayList<String>();
    private ArrayList<String> list = new ArrayList<String>();
    private ArrayAdapter<String> mAdapter;
    private Uri alarmaddress;
    private MediaPlayer alarm_player;
    private Button sound_button;
    private int count = 0;

    private boolean connectedFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoker_data);

        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_ALARM);
        Cursor cursor = manager.getCursor();


        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);

            list.add(notificationTitle);
            String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            uriList.add(uri + "/" + id);
        }
        mAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown, list);
        mAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner = (Spinner) findViewById(R.id.spinner1);
        spinner.setAdapter(mAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                alarmaddress = Uri.parse(uriList.get(position));
                if(alarm_player==null){
                    alarm_player = MediaPlayer.create(getApplicationContext(), alarmaddress);
                }
                else{
                    if (alarm_player.isPlaying()) {
                        alarm_player.stop();
                    }
                    alarm_player = MediaPlayer.create(getApplicationContext(), alarmaddress);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //do nothing
            }

        });
        alarmaddress = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        alarm_player = MediaPlayer.create(getApplicationContext(), alarmaddress);



//        sound_button = (Button) findViewById(R.id.soundButton);
//        sound_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (alarm_player.isPlaying()) {
//                    alarm_player.pause();
//                    alarm_player.seekTo(0);
//                } else {
//                    alarm_player.start();
//                }
//
//            }
//        });
        alarm_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                if(count==2){
                    finish();
                    count = 0;
                }
                else{
                    count++;
                    alarm_player.seekTo(0);
                    alarm_player.start();
                }
            }
        });

        mySwitch = (Switch) findViewById(R.id.mySwitch);
        mySwitch.setChecked(true);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (mySwitch.isChecked()) {
                    //get the values of external + internal in fahrenheit

                }
                else{
                    //convert to celsius
                }
            }
        });



        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mInternalTemperatureTextView = (TextView) findViewById(R.id.internal_temperature_text_view);
        mExternalTemperatureTextView = (TextView) findViewById(R.id.external_temperature_text_view);



        //TODO: Uncomment before pushing
        mBluetoothDevice = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        //mBluetoothStatusTextView.setText("Not Connected");

        // Testing services
        //boolean shouldStartAlarm = !SmokerDataService.isServiceAlarmOn(getApplicationContext(), mBluetoothDevice);
        //SmokerDataService.setServiceAlarm(getApplicationContext(), mBluetoothDevice, shouldStartAlarm);

//        mMessageStatusTextView = (TextView) findViewById(R.id. message_status);
//        mMessageStatusTextView.setText("message shown here");

        mTimerTextView = (TextView) findViewById(R.id.timer_text_view);

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }

        Log.i(TAG, "Background thread started");
        //startTimer(totalTime);

        //TODO: Uncomment before pushing
        setupChat();
    }



    private void timerTick() {

        if (timeLeft != 0) {
            timeLeft--;
            mTimerTextView.setText(parseTimer());
            if (timeLeft == 0) {
                mTimer.cancel();
                mTimerTextView.setText("00:00:00");
                mTimerTextView.setTextColor(Color.RED);
                mTimerTextView.setAlpha(0.5f);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Animation animation = new AlphaAnimation(0.0f, 1.0f);
                    animation.setDuration(500);
                    animation.setRepeatMode(Animation.REVERSE);
                    animation.setRepeatCount(10);
                    mTimerTextView.startAnimation(animation);
                }
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

    private String parseTimer() {
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

    public static Intent newIntent(Context context) {
        return new Intent(context, SmokerDataActivity.class);
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
        //mParserHandler.quit();
        Log.i(TAG, "Background thread destroyed");
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

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                   //mBluetoothStatusTextView.setText("Connected");
                }
            });
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
                    mReceiverHandler.post(new Runnable() {
                        public void run() {

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
                                char currentChar = (char) Integer.parseInt(chars[i]);
                                if (currentChar != ' ' && currentChar != '\n' && currentChar != '\b' && currentChar != '\t') {
                                    sbu.append((char) Integer.parseInt(chars[i]));
                                } else {
                                    sbu.setLength(0);
                                    str = "";
                                }
                            }

                            str += sbu;
                            //mMessageStatusTextView.setText(str);

//                            if (str.contains(Parser.startData)) {
//                                str = "";
//                            }
                            // Parse data, identify start and stop tag to know we received all the data to display
                            parseResult = Parser.stringHandler(str);

                            if (parseResult[0] != null) {
                                // Identify the tag and take the correction action
                                mMessage = parseResult[1];
                                int dataI;
                                double dataD;
                                switch (parseResult[0]) {
                                    //double dataD;

                                    case Parser.startTimeTag:
                                        dataI = Integer.parseInt(mMessage);
                                        write("-".getBytes());
                                        startTimer(dataI);
                                        break;
//TODO need to extract this data into a global variable to change between C and F
                                    case Parser.startITempTag:
                                        dataD = Double.parseDouble(mMessage);
                                        dataI = (int) Math.ceil(dataD);
                                        mInternalTemperatureTextView.setText("" + dataI);
                                        write("-".getBytes());
                                        str = "";
                                        break;

                                    case Parser.startETempTag:
                                        dataD = Double.parseDouble(mMessage);
                                        dataI = (int) Math.ceil(dataD);
                                        mExternalTemperatureTextView.setText("" + dataI);
                                        write("-".getBytes());
                                        str = "";
                                        break;

                                    case Parser.startErrorTag:

                                        break;
                                }
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
