package com.im;

import com.im.utils.Log;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;

public class IMManger {

    private static IMManger imManger;

    private String ip;
    private int port;
    private XMPPConnection conn;
    private IMListener imListener;

    private IMManger() {
    }

    public synchronized static IMManger getInstance() throws XMPPException {
        if (imManger == null) {
            imManger = new IMManger();
        }
        return imManger;
    }

    public void conn(String ip, int port, IMListener listener) throws XMPPException {
        this.ip = ip;
        this.port = port;
        this.imListener = listener;

        ConnectionConfiguration config = new ConnectionConfiguration(ip, port);
        config.setDebuggerEnabled(true);
        conn = new XMPPConnection(config);
        conn.connect();

        initRosterSubscribeListener();
        initChatListener();
    }

    private void initChatListener() {
        conn.getChatManager().addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createLocally) {
                if (!createLocally) {
                    chat.addMessageListener(new MessageListener() {
                        @Override
                        public void processMessage(Chat chat, Message message) {
                            // TODO: 2016/3/13 0013 need to think image vedio file
                            imListener.msgReceive(chat, message);
                            Log.d("recevice msg", message.getBody());
                        }
                    });
                }
            }
        });
    }

    /**
     * Subscribe & unSubscribe listener
     */
    private void initRosterSubscribeListener() {
        PacketFilter filter = new PacketTypeFilter(Presence.class);
        PacketListener listener = new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                Presence presence = (Presence) packet;
//                if (presence.getType().equals(Presence.Type.subscribe)) {
                    imListener.subscribe();
//                    Log.d("SubscribeListener processPacket subscribe", packet.toXML());
//                } else if (presence.getType().equals(Presence.Type.unsubscribe)) {
//                    imListener.unsubscribe();
//                    Log.d("SubscribeListener processPacket unsubscribe", packet.toXML());
//                } else
//                    Log.d("SubscribeListener processPacket other", packet.toXML());
            }
        };
        conn.addPacketListener(listener, filter);
    }

    public boolean registerUser(String username, String pw) {
        if (!conn.isConnected())
            return false;

        AccountManager ac = conn.getAccountManager();
        try {
            ac.createAccount(username, pw);
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean login(String username, String pw) {
        try {
            conn.login(username, pw);
            Presence presence = new Presence(Presence.Type.available);
            conn.sendPacket(presence);
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hideLogin(String username, String pw) {
        try {
            conn.login(username, pw);
            Presence presence = new Presence(Presence.Type.unavailable);
            conn.sendPacket(presence);
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Collection<RosterEntry> getRoster() {
        return conn.getRoster().getEntries();
    }

    /**
     * add roster
     *
     * @param userJid
     * @param username
     * @param groups
     * @return
     */
    public boolean addRoster(String userJid, String username, String[] groups) {
        // TODO: 2016/3/13 0013 先查好是否有改人。。。
        // 下面代码添加好友失败 
        Roster roster = conn.getRoster();
        try {
            roster.createEntry(userJid, username, groups);
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * delete roster
     *
     * @param userJid
     * @return
     */
    public boolean deleteRoster(String userJid) {
        Roster roster = conn.getRoster();
        try {
            roster.removeEntry(roster.getEntry(userJid));
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
    }

    // TODO: 2016/3/13 0013 think image vedio file
    public boolean sendMessage(String userJid, final String msg) {
        Chat chat = conn.getChatManager().createChat(userJid, new MessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                Log.d("sendMessage processMessage recevice ", message + "");
            }
        });

        try {
            Message message = new Message();
            message.setBody(msg);
            chat.sendMessage(message);
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void disConnect() {
        // TODO: 2016/3/13 0013 other release
        conn.disconnect();
    }
}
