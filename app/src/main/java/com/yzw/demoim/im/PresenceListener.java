package com.yzw.demoim.im;

import org.jivesoftware.smack.packet.Presence;

/**
 * Created by yzw on 2016/3/14 0014.
 */
public interface PresenceListener {

    void available(String user);

    void unavailable(String user);

    /**
     * 返回是否接受
     *
     * @param presence
     * @return
     */
    boolean subscribe(Presence presence);

    void unsubscribe(String user);

    void subscribed(String user);

    void unsubscribed(String user);

}
