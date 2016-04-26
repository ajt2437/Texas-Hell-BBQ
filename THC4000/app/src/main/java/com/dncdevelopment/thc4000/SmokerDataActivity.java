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
import android.os.PersistableBundle;
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
    private static final int REQUEST_CODE_DEVICE = 0;

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
    private final static int INTERVAL = 500;


    //temperatureVars
    private int Ifahrenheit;
    private int Icelsius;
    private int Efahrenheit;
    private int Ecelsius;
    private int spinnerPosition = 0;
    private String SAVED_SPINNER = "my saved spinner";

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
        //TODO:LOAD CORRECT MEDIA PLAYER
        mMessageStatusTextView = (TextView) findViewById(R.id.message_status);

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
                    //TODO: check if switch is working
                    mExternalTemperatureTextView.setText("" + Efahrenheit + " degrees Fahrenheit");
                    mInternalTemperatureTextView.setText("" + Ifahrenheit + " degrees Fahrenheit");
                }
                else{
                    mExternalTemperatureTextView.setText("" + Ecelsius + " degrees Celsius");
                    mInternalTemperatureTextView.setText("" + Icelsius + " degrees Celsius");
                }
            }
        });

        mInternalTemperatureTextView = (TextView) findViewById(R.id.internal_temperature_text_view);
        mExternalTemperatureTextView = (TextView) findViewById(R.id.external_temperature_text_view);

        mTimerTextView = (TextView) findViewById(R.id.timer_text_view);

        Log.i(TAG, "Background thread started");

        if (savedInstanceState != null) {
            mBluetoothDevice = savedInstanceState.getParcelable(BluetoothDevice.EXTRA_DEVICE);
            spinnerPosition = savedInstanceState.getInt(SAVED_SPINNER);
        }
        else {
            mBluetoothDevice = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            spinnerPosition = 0;

        }

        //Get Bluetooth device
        if (mService == null && mBluetoothDevice != null) {

            Intent i = SmokerDataService.newIntent(this, mBluetoothDevice, 0);
            startService(i);
            bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }
        //startTimer(totalTime);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(BluetoothDevice.EXTRA_DEVICE, mBluetoothDevice);
        savedInstanceState.putInt(SAVED_SPINNER, spinnerPosition);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_DEVICE) {
            Log.d(TAG, "got bluetooth device");
            mBluetoothDevice = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        }

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
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                alarmaddress = Uri.parse(uriList.get(position));
                spinnerPosition = position;
                if (alarm_player == null) {
                    alarm_player = MediaPlayer.create(getApplicationContext(), alarmaddress);
                } else {
                    if (alarm_player.isPlaying()) {
                        alarm_player.stop();
                    }
                    alarm_player = MediaPlayer.create(getApplicationContext(), alarmaddress);
                }
                if (mService != null) {
                    mService.setAlarmPlayer(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //do nothing
            }

        });
        alarmaddress = Uri.parse(uriList.get(spinnerPosition));
        alarm_player = MediaPlayer.create(getApplicationContext(), alarmaddress);
        spinner.setSelection(spinnerPosition);
        if (mService != null) {
            mService.setAlarmPlayer(0);
            mService.stopAlarm();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
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
        mTimer.scheduleAtFixedRate(mTicker, 0, INTERVAL);
    }

    private void timerTick() {
        // get data
        if (mService != null) {
            int currentETemp = mService.getCurrentETemp();

            int currentITemp = mService.getCurrentITemp();
            timeLeft = mService.getTimer();
            String message = mService.getInput();

            if (timeLeft == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mTimerTextView.setTextColor(Color.RED);
                    Animation animation = new AlphaAnimation(0.0f, 1.0f);
                    animation.setDuration(500);
                    animation.setStartOffset(20);
                    animation.setRepeatMode(Animation.REVERSE);
                    animation.setRepeatCount(5);
                    mTimerTextView.startAnimation(animation);
                }
            }
            else {
                mTimerTextView.setTextColor(Color.BLACK);
            }

            //TODO: check if switch is working
            Efahrenheit = currentETemp;
            Ifahrenheit = currentITemp;
            Ecelsius = ((Efahrenheit - 32)*5)/9;
            Icelsius = ((Ifahrenheit - 32)*5)/9;
            if(mySwitch.isChecked()){
                mExternalTemperatureTextView.setText("" + Efahrenheit + " degrees Fahrenheit");
                mInternalTemperatureTextView.setText("" + Ifahrenheit + " degrees Fahrenheit");
            }
            else{
                mExternalTemperatureTextView.setText("" + Ecelsius + " degrees Celsius");
                mInternalTemperatureTextView.setText("" + Icelsius + " degrees Celsius");

            }
            mTimerTextView.setText(parseTimer());
            mMessageStatusTextView.setText(message);
        }
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
