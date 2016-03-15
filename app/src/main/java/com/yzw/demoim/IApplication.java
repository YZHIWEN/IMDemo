package com.yzw.demoim;

import android.app.Application;

import com.yzw.demoim.im.IMConfig;
import com.yzw.demoim.im.IMManger;

/**
 * Created by yzw on 2016/3/16 0016.
 */
public class IApplication extends Application {

    IMConfig imConfig;
    String ip = "192.168.23.1";
    int port = 5222;
    String servicename = "topviewim";

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    public void init() {
        imConfig = new IMConfig.Builder()
                .setIp(ip)
                .setPort(port)
                .setServiceName(servicename)
                .build();
        IMManger.getInstance().init(imConfig);
    }
}
