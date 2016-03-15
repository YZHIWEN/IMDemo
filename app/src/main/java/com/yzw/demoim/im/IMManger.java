package com.yzw.demoim.im;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class IMManger implements ChatManagerListener, ChatMessageListener {

    private static final String TAG = IMManger.class.getName();
    private static IMManger imManger;
    public AbstractXMPPConnection conn;
    private String ip = "192.168.23.1";
    private int port = 5222;


    private PresenceListener presenceListener;
    private ChatListener chatListener;

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

    public void setChatListener(ChatListener lis) {
        this.chatListener = lis;
        ChatManager.getInstanceFor(conn).addChatListener(this);
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
                Log.e(TAG, "StanzaListener: type " + p.getType() + " mode " + p.getMode());
                Log.e(TAG, "StanzaListener: presence " + p.toString());
                switch (p.getType()) {
                    case available:
//                        presenceListener.available(p.getFrom());
                        break;
                    case unavailable:
//                        presenceListener.unavailable(p.getFrom());
                        break;
                    case subscribe:
                        // 如何 接收和拒绝 ？？

                        // 无意中查看到Roster 的 PresencePacketListener
                        // 其中有代码
                        //                    if (response != null) {
                        //                        response.setTo(presence.getFrom());
                        //                        connection.sendStanza(response);
                        //                    }
                        // 仿照 PresencePacketListener 逻辑实现 在接收 订阅  可以看到触发entriesAdded:函数
                        presenceListener.subscribe(p);
                        break;
                    case unsubscribe:
                        presenceListener.unsubscribe(p.getFrom());
                        break;
                    case subscribed:
                        presenceListener.subscribed(p.getFrom());
                        break;
                    case unsubscribed:
                        presenceListener.unsubscribed(p.getFrom());
                        break;
                }
            }
        }, new StanzaFilter() {
            @Override
            public boolean accept(Stanza stanza) {

                Log.e(TAG, "accept: " + stanza.toString());
                if (stanza instanceof Presence) {
                    Log.e(TAG, "accept: Presence");
                    return true;
                } else if (stanza instanceof Message) {
//                    Log.e(TAG, "accept: Message");
//                    return true;
                }
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


    public Presence getPresence(String userJid) {
        return Roster.getInstanceFor(conn).getPresence(userJid);
    }

    private void addRosterListener() {
        Roster roster = Roster.getInstanceFor(conn);
        // 用户自己处理添加好友等请求
        roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
    }

    public boolean send(final RosterEntry re, String msg) {
        try {
            Log.e(TAG, "send: name " + re.toString() + " user " + re.getUser() + " name " + re.getName());
            ChatManager chatmanager = ChatManager.getInstanceFor(conn);
            Chat newChat = chatmanager.createChat(re.getUser(), null);
            newChat.sendMessage(msg);
            return true;
        } catch (Exception e) {
            System.out.println("Error Delivering block");
            return false;
        }
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
            Log.e(TAG, "deleteRoster: " + re.toString());
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

//        try {
//            Presence presencePacket = new Presence(Presence.Type.subscribe);
//            presencePacket.setTo(userJid + "@topviewim");
//            conn.sendStanza(presencePacket);
//            return true;
//        } catch (SmackException.NotConnectedException e) {
//            e.printStackTrace();
//            return false;
//        }

        try {
            UserSearchManager userSearchManager = new UserSearchManager(conn);
            Form searchForm = userSearchManager.getSearchForm("search." + conn.getServiceName());
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", userJid+ "@topviewim");//Here username must be added name replace by "amith"

            ReportedData resultData = userSearchManager.getSearchResults(answerForm, "search." + conn.getServiceName());


            Iterator<ReportedData.Row> it = resultData.getRows().iterator();
            ReportedData.Row row = null;
            while (it.hasNext()) {
                row = it.next();
                String value = row.toString();
                Log.i("It row values...", " " + value);
            }

            Iterator<ReportedData.Column> cit = resultData.getColumns().iterator();
            ReportedData.Column c = null;
            while (it.hasNext()) {
                c = cit.next();
                String value = row.toString();
                Log.i("It column values...", " " + value);
            }
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        try {
            Roster roster = Roster.getInstanceFor(conn);
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
        try {
            Log.e(TAG, "deleteRoster: " + userJid);
            Roster r = Roster.getInstanceFor(conn);
            RosterEntry re = r.getEntry(userJid);
            if (re != null)
                r.removeEntry(re);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // TODO: 2016/3/13 0013 think image vedio file
    public boolean sendMessage(String userJid, final String msg) {
        return false;
    }


    public void disConnect() {
        // TODO: 2016/3/13 0013 other release
        conn.disconnect();
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        Log.e(TAG, "chatCreated: " + chat + " " + createdLocally);
        if (createdLocally) {

        } else {
            chat.addMessageListener(this);
        }
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        chatListener.receviceChat(chat, message);
    }
}
