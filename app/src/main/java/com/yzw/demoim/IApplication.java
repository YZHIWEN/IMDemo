package com.yzw.demoim;

import android.app.Application;
import android.content.Intent;

import com.yzw.demoim.im.IMConfig;
import com.yzw.demoim.im.IMManger;
import com.yzw.demoim.im.IMService;

/**
 * Created by yzw on 2016/3/16 0016.
 */
public class IApplication extends Application {

    IMConfig imConfig;
    String ip = "192.168.191.1";
    int port = 5222;
    String servicename = "topviewim";

    @Override
    public void onCreate() {
        super.onCreate();

        initConfig();
        startIMServcie();
    }

    public void initConfig() {
        imConfig = new IMConfig.Builder()
                .setIp(ip)
                .setPort(port)
                .setServiceName(servicename)
                .build();
        IMManger.getInstance().init(imConfig);
    }

    private void startIMServcie() {
        startService(new Intent(this, IMService.class));
    }

}
