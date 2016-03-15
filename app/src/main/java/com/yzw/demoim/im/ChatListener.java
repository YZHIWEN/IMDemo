package com.yzw.demoim.im;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.RosterEntry;

/**
 * Created by yzw on 2016/3/15 0015.
 */
public interface ChatListener {

    void receviceChat(Chat chat, Message message);


}
