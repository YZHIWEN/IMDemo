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
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
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
    private IMConfig imconfig;


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

    public synchronized void init(IMConfig imconfig) {
        this.imconfig = imconfig;
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


            XMPPTCPConnectionConfiguration.Builder b = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, pw)
                    .setServiceName(imconfig.serviceName) // Must provide XMPP service name
                    .setHost(imconfig.ip)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled) //
                    .setPort(imconfig.port)
                    .setSendPresence(false); // 设置先不发送Presence 此时login后还是离线状态，获取离线消息后，再sendStanza 设置登录在线状态


            XMPPTCPConnectionConfiguration config = b.build();
            conn = new XMPPTCPConnection(config);
            conn.connect();
            conn.login();

            // 离线消息
            OfflineMessageManager offlineManager = new OfflineMessageManager(conn);
            Log.e(TAG, "offline msg " + offlineManager.getMessageCount());
            Log.e(TAG, "offline msg " + offlineManager.getMessages());

            // 设置为在线状态
            conn.sendStanza(new Presence(Presence.Type.available));

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
                        presenceListener.subscribed(p);
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
                    Log.e(TAG, "accept: Presence" + ((Presence) stanza).toString());
                    return true;
                } else if (stanza instanceof Message) {
//                    Log.e(TAG, "accept: Message");
//                    return true;
                }
                return false;
            }
        });

        conn.addPacketSendingListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {

            }
        }, new StanzaFilter() {
            @Override
            public boolean accept(Stanza stanza) {
                Log.e(TAG, "accept:  send" + stanza.toString());
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

    public boolean handlerReceiverSubscribed(Presence presence) {
        Log.e(TAG, "handlerReceiverSubscribed: " + presence.toString());
        List<RosterEntry> relist = getRosterEntrys();
        for (RosterEntry re : relist) {
            if (re.getUser().equals(presence.getFrom())) {
                if (re.getType().equals(RosterPacket.ItemType.both)) {
                    Log.e(TAG, "handlerReceiverSubscribed: return true");
                    return true;
                } else
                    break;
            }
        }
        Log.e(TAG, "handlerReceiverSubscribed: send");
        return agreeSubscribe(presence);
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

    /**
     * 只显示both关系
     *
     * @return
     */
    public List<RosterEntry> getRosterEntrys() {
        List<RosterEntry> list = new ArrayList<>();
        Set<RosterEntry> sre = Roster.getInstanceFor(conn).getEntries();
        for (RosterEntry re : sre) {
            if (re.getType().equals(RosterPacket.ItemType.both))
                list.add(re);
        }
        return list;
    }

    /**
     * 添加好友，需保证用户账号存在
     * add roster
     *
     * @param userJid
     * @param username
     * @param groups
     * @return
     */
    public boolean addRoster(String userJid, String username, String[] groups) {
        try {
            Roster roster = Roster.getInstanceFor(conn);
            roster.createEntry(userJid + "@" + imconfig.serviceName, username, groups);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 模糊查找 有关key 用户
     *
     * @param key
     * @return
     */
    public List<String> searchUser(String key) {
        List<String> res = new ArrayList<>();
        try {
            UserSearchManager userSearchManager = new UserSearchManager(conn);
            Form searchForm = userSearchManager.getSearchForm("search." + conn.getServiceName());
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", key);//Here username must be added name replace by "amith"

            ReportedData resultData = userSearchManager.getSearchResults(answerForm, "search." + conn.getServiceName());

            if (resultData != null) {
                List<ReportedData.Row> rows = resultData.getRows();
                Iterator<ReportedData.Row> it = rows.iterator();
                Log.e(TAG, "result data != null has next ?" + it.hasNext());
                while (it.hasNext()) {
                    ReportedData.Row row = it.next();
                    List<String> values = row.getValues("jid");
                    if (values != null)
                        Log.e(TAG, "row jids  " + values.toString());
                    List<String> vs = row.getValues("name");
                    if (vs != null)
                        Log.e(TAG, "row names " + vs.toString());

                    for (String str : values) {
                        // 需要加+ "@topviewim"，str是 "@topviewim"结尾
                        int index = str.lastIndexOf("@" + imconfig.serviceName);
                        String s = str.substring(0, index);
                        Log.e(TAG, "searchUser: " + s);
                        if (s.contains(key))
                            res.add(s);
                    }
                }
            } else {
                Log.e(TAG, "result data == null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
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
