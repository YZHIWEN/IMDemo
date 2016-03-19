package com.yzw.demoim.im.listener;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.yzw.demoim.IApplication;
import com.yzw.demoim.im.IMManger;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;

import java.util.Collection;

/**
 * Created by yzw on 2016/3/19 0019.
 */
public class IMRosterListener implements RosterListener {

    public static final String TAG = IMRosterListener.class.getName();

    @Override
    public void entriesAdded(Collection<String> addresses) {
        Log.e(TAG, "entriesAdded: ");
    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {
        Log.e(TAG, "entriesUpdated: ");
    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {
        Log.e(TAG, "entriesDeleted: ");
    }

    @Override
    public void presenceChanged(Presence presence) {
        // 上下线通知
        Log.e(TAG, "--------------->presenceChanged: ");
        Log.e(TAG, "presence " + presence.toXML());
        String user = presence.getFrom();
        Presence bestPresence = IMManger.getInstance().getRoster().getPresence(user);
        Log.e(TAG, bestPresence.getFrom() + " **presenceChanged** mode " + bestPresence.getMode() + " type " + bestPresence.getType());
        final String s = bestPresence.getFrom() + " **presenceChanged** mode " + bestPresence.getMode() + " type " + bestPresence.getType();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(IApplication.iApplication, s, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
