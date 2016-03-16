package com.yzw.demoim.im.listener;

import android.util.Log;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by yzw on 2016/3/16 0016.
 */
public class IMChatListener implements ChatManagerListener {

    private static final String TAG = "IMChatListener";

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        Log.d(TAG, "chatCreated: chat " + chat.toString() + " locally ? " + createdLocally);
        Log.d(TAG, "chatCreated: chat messagelistener size " + chat.getListeners().size());
        chat.addMessageListener(new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                Log.d(TAG, "processMessage: chat " + chat.toString() + " message " + message.toString());
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        new MaterialDialog.Builder(RosterActivity.this)
//                                .title("收到来自" + message.getFrom() + "的消息")
//                                .content(message.getBody())
//                                .positiveText("确认")
//                                .show();
//                    }
//                });
            }
        });
    }
}
