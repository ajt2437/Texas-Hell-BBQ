package com.practiceapp1.nathan.firstandroidapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
    private Button swapActivity;
    private int listposition;
    private Handler mHandler;
    private int timerValue = 1000;
    private static TextView messaging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor cursor = manager.getCursor();


        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String notificationUri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);

            list.add(notificationTitle);
            uriList.add(notificationUri);
        }
        mAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown, list);
        mAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner = (Spinner) findViewById(R.id.spinner1);
        spinner.setAdapter(mAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
               /* Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(uriList.get(position)));
                r.play();
                Toast.makeText(getApplicationContext(), uriList.get(position), Toast.LENGTH_LONG).show(); */
                listposition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
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


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

