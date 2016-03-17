package com.yzw.demoim.im.listener;

import android.util.Log;

import com.yzw.demoim.bean.ChatMessage;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by yzw on 2016/3/16 0016.
 */
public class IMChatListener implements ChatManagerListener {

    private static final String TAG = "IMChatListener";
    private EventBus eb;

    public IMChatListener() {
        eb = EventBus.getDefault();
    }


    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        Log.d(TAG, "chatCreated: chat " + chat.toString() + " locally ? " + createdLocally);
        Log.d(TAG, "chatCreated: chat messagelistener size " + chat.getListeners().size());
        chat.addMessageListener(new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                Log.d(TAG, "processMessage: chat " + chat.toString() + " message " + message.toString());

                ChatMessage cm = new ChatMessage();
                cm.setBody(message.getBody());
                cm.setFrom(message.getFrom());
                cm.setType(ChatMessage.Type.RECEIVE);
                eb.post(cm);
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
