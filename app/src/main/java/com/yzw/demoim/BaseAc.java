package com.yzw.demoim;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by yzw on 2016/3/17 0017.
 */
public class BaseAc extends AppCompatActivity {

    public static final String TAG = "Base Activity";

    public void handlerMessage(Message msg) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static class Her extends Handler {
        private WeakReference<BaseAc> wac;

        public Her(BaseAc ac) {
            wac = new WeakReference<BaseAc>(ac);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseAc ac = wac.get();
            if (ac != null) {
                ac.handlerMessage(msg);
            } else {
                Log.e(TAG, "activity is null");
            }
        }
    }


}
