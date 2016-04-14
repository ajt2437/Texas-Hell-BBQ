package com.dncdevelopment.thc4000;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by AbelardoJose on 4/11/2016.
 */
public class ParserHandler extends HandlerThread {
    private static final String TAG = "ParserHandler";
    private static final int MESSAGE_RECEIVED = 0;
    private Handler mResponseHandler;

    public ParserHandler(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    public void queueData(String data) {
        Log.i(TAG, "Got a string: " + data);
    }
}
