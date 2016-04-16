package com.dncdevelopment.thc4000;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by AbelardoJose on 4/13/2016.
 */
public class SmokerDataService extends IntentService {
    private static final String TAG = "SmokerDataService";

    private static final int POLL_INTERVAL = 1000 * 60;
    private BluetoothDevice mBluetoothDevice;

    public static Intent newIntent(Context context, BluetoothDevice bluetoothDevice) {
        Intent i = new Intent(context, SmokerDataService.class);
        i.putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice);
        return i;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, SmokerDataService.class);
    }

    public SmokerDataService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Received an intent: " + intent);

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

    }

    public static void setServiceAlarm(Context context, BluetoothDevice device, boolean isOn) {
        Intent i = SmokerDataService.newIntent(context, device);
        //Intent i = SmokerDataService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
        }
        else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context, BluetoothDevice device) {
        Intent i = SmokerDataService.newIntent(context, device);
        //Intent i = SmokerDataService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }
}
