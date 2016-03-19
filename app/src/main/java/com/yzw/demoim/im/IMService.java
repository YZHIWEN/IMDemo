package com.yzw.demoim.im;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;

import java.io.File;
import java.util.List;

/**
 * IM Service
 * Created by yzw on 2016/3/16 0016.
 */
public class IMService extends Service {

    IMManger imManger;
    private IMBinder imBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return imBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        imManger = IMManger.getInstance();
        imBinder = new IMBinder(imManger);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        imBinder = null;
        imManger.disConnect();
        imManger = null;
    }

    public class IMBinder extends Binder implements IMListener {
        private IMManger imManger;

        public IMBinder(IMManger imManger) {
            this.imManger = imManger;
        }

        @Override
        public boolean login(String user, String pw) {
            return imManger.login(user, pw);
        }

        @Override
        public boolean logout() {
            return imManger.logout();
        }

        @Override
        public boolean register(String user, String pw) {
            return imManger.register(user, pw);
        }

        @Override
        public List<RosterEntry> getRosterEntrys() {
            return imManger.getRosterEntrys();
        }

        @Override
        public List<String> search(String key) {
            return imManger.search(key);
        }

        @Override
        public boolean addRoster(String user) {
            return imManger.addRoster(user);
        }

        @Override
        public boolean deleteRoster(String user) {
            return imManger.deleteRoster(user);
        }

        @Override
        public boolean agreeSubscribe(Presence p) {
            return imManger.agreeSubscribe(p);
        }

        @Override
        public boolean disagreeSubscribe(Presence p) {
            return imManger.disagreeSubscribe(p);
        }

        @Override
        public boolean sendMessage(String user, String msg) {
            return imManger.sendMessage(user, msg);
        }

        @Override
        public boolean sendFile(String user, File file) {
            return imManger.sendFile(user, file);
        }
    }

}
