package com.yzw.demoim.im;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yzw.demoim.im.callback.IMCallBack;

import org.jivesoftware.smackx.filetransfer.FileTransferRequest;

import java.lang.ref.WeakReference;

/**
 * Created by yzw on 2016/3/19 0019.
 */
public class IMBaseActivity extends AppCompatActivity implements IMCallBack {
    public static final String TAG = "Base Activity";
    protected IMService.IMBinder mImServiceBinder;
    private ServiceConnection serviceconn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mImServiceBinder = (IMService.IMBinder) service;
            mImServiceBinder.setCallback(IMBaseActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void handleMessage(Message msg) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindIMService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceconn);
    }

    private void bindIMService() {
        Intent intent = new Intent(IMBaseActivity.this, IMService.class);
        bindService(intent, serviceconn, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void receive(String user, String msg) {
        Log.e(TAG, "receive: from " + user + " " + msg);
    }

    @Override
    public void receive(String user, FileTransferRequest request) {
        Log.e(TAG, "receive: file from " + user);
    }


    public static class Her extends Handler {
        private WeakReference<IMBaseActivity> wac;

        public Her(IMBaseActivity ac) {
            wac = new WeakReference<IMBaseActivity>(ac);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            IMBaseActivity ac = wac.get();
            if (ac != null) {
                ac.handleMessage(msg);
            } else {
                Log.e(TAG, "activity is null");
            }
        }
    }

}
