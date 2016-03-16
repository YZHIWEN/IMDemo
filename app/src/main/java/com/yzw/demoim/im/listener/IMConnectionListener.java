package com.yzw.demoim.im.listener;

import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.XMPPConnection;

/**
 * Created by yzw on 2016/3/16 0016.
 */
public class IMConnectionListener implements ConnectionListener {
    private static final String TAG = "IMConnectionListener";

    /**
     * Note that the connection is likely not yet authenticated and therefore only limited operations like registering
     * an account may be possible.
     *
     * @param connection
     */
    @Override
    public void connected(XMPPConnection connection) {
        Log.d(TAG, "connected: ");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.d(TAG, "authenticated: ");
    }

    @Override
    public void connectionClosed() {
        Log.d(TAG, "connectionClosed:");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d(TAG, "connectionClosedOnError:");
    }

    @Override
    public void reconnectionSuccessful() {
        Log.d(TAG, "reconnectionSuccessful:");
    }

    /**
     * Note: This method is only called if {@link ReconnectionManager#isAutomaticReconnectEnabled()} returns true, i.e.
     * only when the reconnection manager is enabled for the connection.
     *
     * @param seconds
     */
    @Override
    public void reconnectingIn(int seconds) {
        Log.d(TAG, "reconnectingIn:");
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.d(TAG, "reconnectionFailed:");
    }
}
