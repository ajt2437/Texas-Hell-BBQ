package com.dncdevelopment.thc4000;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
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
    private BluetoothDevice mBluetoothDevice;
    private TextView mInternalTemperatureTextView;
    private TextView mExternalTemperatureTextView;
    private TextView mMessageStatusTextView;
    private TextView mTimerTextView;
    private Switch mySwitch;

    // Model
    private Spinner spinner;
    private ArrayList<String> uriList = new ArrayList<String>();
    private ArrayList<String> list = new ArrayList<String>();
    private ArrayAdapter<String> mAdapter;
    private Uri alarmaddress;
    private MediaPlayer alarm_player;
    private Button sound_button;
    private int count = 0;
    private int totalTime = 5;
    private int timeLeft;
    SmokerDataService mService;
    boolean mBound = false;
    private Timer mTimer = new Timer();
    private Handler mReceiveHandler = new Handler();

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

//        alarm_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            public void onCompletion(MediaPlayer mp) {
//                if(count==2){
//                    finish();
//                    count = 0;
//                }
//                else{
//                    count++;
//                    alarm_player.seekTo(0);
//                    alarm_player.start();
//                }
//            }
//        });

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

        mInternalTemperatureTextView = (TextView) findViewById(R.id.internal_temperature_text_view);
        mExternalTemperatureTextView = (TextView) findViewById(R.id.external_temperature_text_view);

        mTimerTextView = (TextView) findViewById(R.id.timer_text_view);

        Log.i(TAG, "Background thread started");

        //Get Bluetooth device
        mBluetoothDevice = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Intent i = SmokerDataService.newIntent(this, mBluetoothDevice);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        //startTimer(totalTime);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SmokerDataService.LocalBinder binder = (SmokerDataService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            Toast.makeText(SmokerDataActivity.this, "Connected,", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Set up alarm
        if (mService != null) {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    alarmaddress = Uri.parse(uriList.get(position));
                    if (alarm_player == null) {
                        alarm_player = MediaPlayer.create(getApplicationContext(), alarmaddress);
                        mService.setAlarmPlayer(alarm_player);
                    } else {
                        if (alarm_player.isPlaying()) {
                            alarm_player.stop();
                        }
                        alarm_player = MediaPlayer.create(getApplicationContext(), alarmaddress);
                        mService.setAlarmPlayer(alarm_player);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    //do nothing
                }

            });
            alarmaddress = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            alarm_player = MediaPlayer.create(getApplicationContext(), alarmaddress);
            mService.setAlarmPlayer(alarm_player);
            startTimer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void startTimer() {
        mTimer.cancel();
        TimerTask mTicker = new TimerTask() {
            @Override
            public void run() {
                mReceiveHandler.post(new Runnable() {
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
        // get data
        int currentETemp = mService.getCurrentETemp();
        int currentITemp = mService.getCurrentITemp();
        timeLeft = mService.getTimer();

        // Assuming everything works correctly
        mExternalTemperatureTextView.setText("" + currentETemp);
        mInternalTemperatureTextView.setText("" + currentITemp);
        mTimerTextView.setText(parseTimer());
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

    public static Intent newIntent(Context packageContext, BluetoothDevice device) {
        Intent i = new Intent(packageContext, SmokerDataActivity.class);
        i.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
        return i;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBound)
        {
            unbindService(mConnection);
            mBound = false;
        }
        Log.i(TAG, "Background thread destroyed");
    }

}
