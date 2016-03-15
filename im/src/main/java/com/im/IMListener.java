package com.im;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by yzw on 2016/3/13 0013.
 */
public interface IMListener {

    void subscribe();


    void unsubscribe();

    void msgSendSuccess();

    void msgSendError();

    void msgReceive(Chat chat, Message message);
}
