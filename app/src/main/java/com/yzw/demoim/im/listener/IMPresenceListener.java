package com.yzw.demoim.im.listener;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.yzw.demoim.IApplication;

import org.jivesoftware.smack.packet.Presence;

/**
 * Created by yzw on 2016/3/19 0019.
 */
public class IMPresenceListener implements PresenceListener {

    private static final String TAG = "IMPresenceListener";

    @Override
    public boolean subscribe(Presence presence) {
        Log.e(TAG, "subscribe: ");
        return false;
    }

    @Override
    public void unsubscribe(String user) {
        Log.e(TAG, "unsubscribe: ");
    }

    @Override
    public void subscribed(Presence presence) {
        Log.e(TAG, "subscribed: ");
    }

    @Override
    public void unsubscribed(String user) {

    }
}
