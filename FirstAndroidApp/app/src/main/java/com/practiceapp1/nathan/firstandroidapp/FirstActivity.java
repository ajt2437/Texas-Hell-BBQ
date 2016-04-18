package com.practiceapp1.nathan.firstandroidapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class FirstActivity extends AppCompatActivity{
    public final static String EXTRA_MESSAGE = "com.practiceapp1.nathan.firstandroidapp.MESSAGE";
    private TextView switchStatus;
    private Switch mySwitch;
    private Spinner spinner;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> uriList = new ArrayList<String>();
    private ArrayList<String> list = new ArrayList<String>();
    private int listposition;
    private Handler mHandler;
    private int timerValue = 1000;
    private static TextView messaging;
    private Button sound;
    private Ringtone r;
    private MediaPlayer testing;
    private Uri notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_first);
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
                listposition = position;
                notification = Uri.parse(uriList.get(position));
                if(testing.isPlaying()){
                    testing.stop();
                }
                testing = MediaPlayer.create(getApplicationContext(), notification);
                testing.setLooping(true);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        testing = MediaPlayer.create(getApplicationContext(), notification);
        testing.setLooping(true);



        sound = (Button) findViewById(R.id.soundButton);
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
               if(testing.isPlaying()){
                   testing.pause();
                   testing.seekTo(0);
               }
                else{
                   testing.start();
               }

            }
        });


        mySwitch = (Switch) findViewById(R.id.mySwitch);
        mySwitch.setChecked(true);
        mySwitch.setTextOff("°C");
        mySwitch.setTextOn("°F");
        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
            }
        });
        //check the current state before we display the screen
        if(mySwitch.isChecked()) {

        }
        else {
        }



        mHandler = new Handler();
        messaging = (TextView) findViewById (R.id.textView2);
    }


    @Override
    public void onPause(){
        super.onPause();
        stopRepeatingTask();
    }

    @Override
    public void onResume(){
        super.onResume();
        startRepeatingTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first, menu);
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

    public void sendMessage(View view){
        Intent intent = new Intent(this, SecondActivity.class);
        String message = list.get(listposition);
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    Runnable mStatusChecker = new Runnable(){
        @Override
        public void run() {
            try {
                messaging.setText(String.valueOf(timerValue));
                timerValue--;
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, 1000);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    public static void decrement_timer(){
        String integer = messaging.getText().toString();
        int num1 = Integer.parseInt(integer);
        num1--;
        messaging.setText(String.valueOf(num1));

    }

}

