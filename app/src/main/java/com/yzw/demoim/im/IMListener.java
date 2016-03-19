package com.yzw.demoim.im;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;

import java.io.File;
import java.util.List;

/**
 * Created by yzw on 2016/3/19 0019.
 */
public interface IMListener {

    boolean login(String user, String pw);

    boolean logout();

    boolean register(String user, String pw);

    List<RosterEntry> getRosterEntrys();

    /**
     * 模糊查找 有关key 用户
     *
     * @param key
     * @return
     */
    List<String> search(String key);

    boolean addRoster(String user);

    boolean deleteRoster(String user);

    boolean agreeSubscribe(Presence p);

    boolean disagreeSubscribe(Presence p);

    boolean sendMessage(String user, String msg);

    boolean sendFile(String user, File file);
}