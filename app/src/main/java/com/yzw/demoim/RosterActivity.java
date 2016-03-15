package com.yzw.demoim;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.yzw.demoim.im.ChatListener;
import com.yzw.demoim.im.IMManger;
import com.yzw.demoim.im.PresenceListener;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;

public class RosterActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener
        , PresenceListener, RosterListener, ChatListener {


    private static final String TAG = RosterActivity.class.getName();
    private static final int SUBSCRIBE = 111;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.listview)
    ListView listview;
    private String ip = "192.168.23.1";
    private int port = 5222;

    private H handler;

    private MaterialDialog progress_Dialog;

    private List<RosterEntry> res;
    private FriendAdapter adapter;

    private IMManger imManger;

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                Toast.makeText(RosterActivity.this, msg.obj + "", Toast.LENGTH_SHORT).show();
                break;

            case 2:
                if (!progress_Dialog.isShowing()) {
                    progress_Dialog.show();
                }
                break;

            case 3:
                if (progress_Dialog.isShowing()) {
                    progress_Dialog.dismiss();
                }
                break;

            case 4:
                Toast.makeText(RosterActivity.this, msg.obj + "", Toast.LENGTH_SHORT).show();
                break;
            case 5:
                adapter.notifyDataSetChanged();
                break;

            case SUBSCRIBE:
                final Presence p = (Presence) msg.obj;


                new MaterialDialog.Builder(RosterActivity.this)
                        .title("好友添加请求")
                        .positiveText("同意")
                        .negativeText("拒绝")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        imManger.agreeSubscribe(p);
                                    }
                                }.start();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        imManger.disagreeSubscribe(p);
                                    }
                                }.start();
                            }
                        })
                        .show();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roster);
        ButterKnife.bind(this);

        toolbar.setTitle("联系人");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);

        res = new ArrayList<>();
        adapter = new FriendAdapter(this, res, R.layout.item_friend);
        listview.setAdapter(adapter);

        imManger = IMManger.getInstance();
        imManger.setPresenceListener(this);
        imManger.setRosterListener(this);
        imManger.setChatListener(this);

        progress_Dialog = new MaterialDialog.Builder(RosterActivity.this)
                .content("请稍等")
                .progress(true, 0).build();

        handler = new H(this);

        initContacts();

        // Assume we've created an XMPPConnection name "connection"._
//        ChatManager chatManager = ChatManager.getInstanceFor(imManger.conn);
//        chatManager.addChatListener(new ChatManagerListener() {
//            @Override
//            public void chatCreated(Chat chat, boolean createdLocally) {
//                Log.e(TAG, "chatCreated " + chat + " " + createdLocally);
//                if (!createdLocally)
//                    chat.addMessageListener(new ChatMessageListener() {
//                        @Override
//                        public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
//                            Log.e(TAG, "processMessage " + message);
//                        }
//                    });
//            }
//        });
    }

    private void initContacts() {
        new Thread() {
            @Override
            public void run() {
                imManger = IMManger.getInstance();
                res.clear();
                res.addAll(imManger.getRosterEntrys());
                Log.e(TAG, "res size " + res.size());
                for (RosterEntry re : res) {
                    Log.e(TAG, "re " + re.toString());
                }
                handler.sendEmptyMessage(5);
            }
        }.start();

    }

    @OnItemLongClick(R.id.listview)
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
        new MaterialDialog.Builder(this)
                .title("删除好友")
                .content("确认删除")
                .positiveText("确认")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        imManger.deleteRoster(res.get((int) id));
                    }
                }).show();
        return true;
    }

    @OnItemClick(R.id.listview)
    public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
        final RosterEntry re = res.get(position);
        new MaterialDialog.Builder(this)
                .title("发送消息")
                .positiveText("确认")
                .customView(R.layout.input_box, true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        imManger.send(re, ((EditText) dialog.findViewById(R.id.input_box)).getText().toString());
                    }
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.roster_menu, menu);
        return true;
    }

//    @OnClick(R.id.send)
//    public void send(View view) {
//        new Thread() {
//            @Override
//            public void run() {
//                // Assume we've created an XMPPConnection name "connection"._
//                ChatManager chatmanager = ChatManager.getInstanceFor(imManger.conn);
//                Chat newChat = chatmanager.createChat(sendTo.getText().toString() + "@topviewim", new ChatMessageListener() {
//                    @Override
//                    public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
//                        try {
//                            chat.sendMessage(message.getBody());
//                        } catch (SmackException.NotConnectedException e) {
//                            e.printStackTrace();
//                        }
//                        Log.e(TAG, "Received message: " + message);
//                    }
//                });
//
//                try {
//                    newChat.sendMessage("Howdy!" + imManger.conn.getUser());
//                } catch (Exception e) {
//                    Log.e(TAG, "Error Delivering block");
//                }
//            }
//        }.start();
//    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_roster:
                addRoster();
                break;
        }
        return true;
    }

    private void addRoster() {
        boolean wrapInScrollView = true;
        new MaterialDialog.Builder(this)
                .title("添加好友")
                .customView(R.layout.input_box, wrapInScrollView)
                .positiveText("添加")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        handler.sendEmptyMessage(2);
                        new Thread() {
                            @Override
                            public void run() {
                                EditText box = (EditText) dialog.getCustomView().findViewById(R.id.input_box);
                                String username = box.getText().toString();

                                boolean res = imManger.addRoster(username, null, null);
                                handler.sendMessage(Message.obtain(handler, 1, res));
                                handler.sendEmptyMessage(3);
                            }
                        }.start();
                    }
                })
                .show();
    }

    @Override
    public void available(String user) {
        handler.sendMessage(Message.obtain(handler, 4, "available"));
        Log.e(TAG, "available: " + user);
    }

    @Override
    public void unavailable(String user) {
        handler.sendMessage(Message.obtain(handler, 4, "unavailable"));
        Log.e(TAG, "unavailable: " + user);
    }

    @Override
    public boolean subscribe(Presence presence) {
        handler.sendMessage(Message.obtain(handler, 4, "subscribe"));
        Log.e(TAG, " show dialog subscribe: " + presence.toString());
        handler.sendMessage(Message.obtain(handler, SUBSCRIBE, presence));
        return true;
    }

    @Override
    public void unsubscribe(String user) {
        handler.sendMessage(Message.obtain(handler, 4, "unsubscribe"));
        Log.e(TAG, "unsubscribe: " + user);
    }

    @Override
    public void subscribed(String user) {
        handler.sendMessage(Message.obtain(handler, 4, "subscribed"));
        Log.e(TAG, "subscribed: " + user);
        initContacts();
    }

    @Override
    public void unsubscribed(String user) {
        handler.sendMessage(Message.obtain(handler, 4, "unsubscribed"));
        Log.e(TAG, "unsubscribed: " + user);
        imManger.deleteRoster(user);
    }

    @Override
    public void entriesAdded(Collection<String> addresses) {
        Log.e(TAG, "entriesAdded: " + addresses.toString());
        initContacts();
    }

    // ------------------------------

    @Override
    public void entriesUpdated(Collection<String> addresses) {
        Log.e(TAG, "entriesUpdated: " + addresses.toString());
        initContacts();
    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {
        Log.e(TAG, "entriesDeleted: " + addresses.toString());
        initContacts();
    }

    @Override
    public void presenceChanged(Presence presence) {
        Log.e(TAG, "presenceChanged: " + presence.toString());
        handler.sendMessage(Message.obtain(handler, 4, presence.getFrom() + " **presenceChanged** mode " + presence.getMode() + " type " + presence.getType()));
    }

    // ------------------------------
    @Override
    public void receviceChat(Chat chat, final org.jivesoftware.smack.packet.Message message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                new MaterialDialog.Builder(RosterActivity.this)
                        .title("收到来自" + message.getFrom() + "的消息")
                        .content(message.getBody())
                        .positiveText("确认")
                        .show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imManger.disConnect();
    }

    class H extends Handler {
        private WeakReference<RosterActivity> wa;

        public H(RosterActivity a) {
            this.wa = new WeakReference<RosterActivity>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RosterActivity ra = wa.get();
            if (ra != null) {
                ra.handleMessage(msg);
            }
        }
    }
}
