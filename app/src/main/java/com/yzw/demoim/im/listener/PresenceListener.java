package com.yzw.demoim.im.listener;

import org.jivesoftware.smack.packet.Presence;

/**
 * Created by yzw on 2016/3/14 0014.
 */
public interface PresenceListener {

    /**
     * 返回是否接受
     *
     * @param presence
     * @return
     */
    boolean subscribe(Presence presence);

    void unsubscribe(String user);

    void subscribed(Presence presence);

    void unsubscribed(String user);

}
