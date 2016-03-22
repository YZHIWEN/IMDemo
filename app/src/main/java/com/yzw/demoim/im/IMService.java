package com.yzw.demoim.im;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yzw.demoim.im.callback.IMCallBack;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * IM Service
 * Created by yzw on 2016/3/16 0016.
 */
public class IMService extends Service implements FileTransferListener, ChatManagerListener {

    private static final String TAG = "IMService";
    IMManger imManger;
    private IMBinder imBinder;
    private List<IMCallBack> imCallBacks;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return imBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        imManger = IMManger.getInstance();
        imManger.setIMFileListener(this);
        imBinder = new IMBinder(imManger);

        imCallBacks = new ArrayList<>();
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

    @Override
    public void fileTransferRequest(FileTransferRequest request) {
        tonotify(request);
    }

    private void tonotify(FileTransferRequest request) {
        for (IMCallBack ic : imCallBacks) {
            ic.receive(request.getRequestor(), request);
        }
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        Log.d(TAG, "chatCreated: chat " + chat.toString() + " locally ? " + createdLocally);
        Log.d(TAG, "chatCreated: chat messagelistener size " + chat.getListeners().size());
        chat.addMessageListener(new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                Log.d(TAG, "processMessage: chat " + chat.toString() + " message " + message.toString());
                tonotify(message);
            }
        });
    }

    private void tonotify(Message message) {
        for (IMCallBack ic : imCallBacks) {
            ic.receive(message.getFrom(), message.getBody());
        }
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

        public void setCallback(IMCallBack imCallBack) {
            imCallBacks.add(imCallBack);
        }

        public void removeCallback(IMCallBack imCallBack) {
            if (imCallBack == null)
                return;
            imCallBacks.remove(imCallBack);
        }
    }

}
