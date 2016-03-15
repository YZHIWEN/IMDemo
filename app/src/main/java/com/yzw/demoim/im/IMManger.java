package com.yzw.demoim.im;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class IMManger {

    private static final String TAG = IMManger.class.getName();
    private static IMManger imManger;
    public AbstractXMPPConnection conn;
    private String ip = "192.168.23.1";
    private int port = 5222;


    private PresenceListener presenceListener;

    private IMManger() {
    }

    public synchronized static IMManger getInstance() {
        if (imManger == null) {
            imManger = new IMManger();
        }
        return imManger;
    }

    public void setPresenceListener(PresenceListener listener) {
        this.presenceListener = listener;
    }

    public void setRosterListener(RosterListener listener) {
        Roster roster = Roster.getInstanceFor(conn);
        roster.addRosterListener(listener);
    }

    public boolean login(String username, String pw) {
        try {
            // 添加SASL验证 ， 否则会报错并且登录失败
            SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
            SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");

            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, pw)
                    .setServiceName(ip) // Must provide XMPP service name
                    .setHost(ip)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled) //
                    .setPort(port)
                    .build();

            conn = new XMPPTCPConnection(config);
            conn.connect();
            conn.login();

            addRosterListener();
            addStanzaListener();


            return true;
        } catch (SmackException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }

    }

    private void addStanzaListener() {

        conn.addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                Log.e(TAG, "--> Presence processPacket: " + packet.toXML());
                Presence p = (Presence) packet;
                if (p.getType().equals(Presence.Type.subscribe)) {
                    Log.e(TAG, "processPacket: subscribe");
                    // 如何 接收和拒绝 ？？
                    // 无意中查看到Roster 的 PresencePacketListener
                    // 其中有代码
//                    if (response != null) {
//                        response.setTo(presence.getFrom());
//                        connection.sendStanza(response);
//                    }
                    // 仿照 PresencePacketListener 逻辑实现 在接收 订阅  可以看到触发entriesAdded:函数

                    presenceListener.subscribe(p);
                } else if (p.getType().equals(Presence.Type.unsubscribe)) {
                    Log.e(TAG, "processPacket: unsubscribe");
                    presenceListener.unsubscribe(p.getFrom());
                } else if (p.getType().equals(Presence.Type.subscribed)) {
                    Log.e(TAG, "processPacket: subscribed");
                    presenceListener.subscribed(p.getFrom());
                } else if (p.getType().equals(Presence.Type.unsubscribed)) {
                    Log.e(TAG, "processPacket: unsubscribed");
                    presenceListener.unsubscribed(p.getFrom());
                } else {
                    Log.e(TAG, "processPacket: other");
                }
            }
        }, new StanzaFilter() {
            @Override
            public boolean accept(Stanza stanza) {
                if (stanza instanceof Presence) {
                    Log.e(TAG, "accept: Presence");
                    return true;
                }
                Log.e(TAG, "accept: Other");
                return false;
            }
        });
    }

    public boolean agreeSubscribe(Presence p) {
        try {
            Log.e(TAG, "send subscribed agreeSubscribe");
            Presence np = new Presence(Presence.Type.subscribed);
            np.setTo(p.getFrom());
            np.setFrom(p.getTo());
            conn.sendStanza(np);
            return true;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean disagreeSubscribe(Presence p) {
        try {
            Presence np = new Presence(Presence.Type.unsubscribe);
            np.setTo(p.getFrom());
            np.setFrom(p.getTo());
            conn.sendStanza(np);
            return true;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void addRosterListener() {
        Roster roster = Roster.getInstanceFor(conn);
        // 用户自己处理添加好友等请求
        roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        roster.addRosterListener(new RosterListener() {
            // Ignored events public void entriesAdded(Collection<String> addresses) {}
            public void entriesDeleted(Collection<String> addresses) {
                Log.e(TAG, "entriesDeleted: ");
            }

            @Override
            public void entriesAdded(Collection<String> addresses) {
                Log.e(TAG, "entriesAdded: ");
            }

            public void entriesUpdated(Collection<String> addresses) {
                Log.e(TAG, "entriesUpdated: ");
            }

            public void presenceChanged(Presence presence) {
                Log.e(TAG, "presenceChanged: " + "Presence changed: " + presence.getFrom() + " " + presence);
            }
        });
    }

    public boolean registerUser(String username, String pw) {
        try {
            AccountManager am = AccountManager.getInstance(conn);
            am.createAccount(username, pw);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRoster(RosterEntry re) {
        try {
            Roster roster = Roster.getInstanceFor(conn);
            roster.removeEntry(re);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean hideLogin(String username, String pw) {
        return false;
    }

    public Roster getRoster() {
        return Roster.getInstanceFor(conn);
    }

    public List<RosterEntry> getRosterEntrys() {
        List<RosterEntry> list = new ArrayList<>();
        Set<RosterEntry> sre = Roster.getInstanceFor(conn).getEntries();
        for (RosterEntry re : sre) {
            list.add(re);
        }
        return list;
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
        Roster roster = Roster.getInstanceFor(conn);
        try {
            roster.createEntry(userJid + "@topviewim", username, groups);
            return true;
        } catch (Exception e) {
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
        return false;
    }

    // TODO: 2016/3/13 0013 think image vedio file
    public boolean sendMessage(String userJid, final String msg) {
        return false;
    }


    public void disConnect() {
        // TODO: 2016/3/13 0013 other release
    }
}
