package com.yzw.demoim;

import android.content.Intent;
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
import com.yzw.demoim.bean.Friend;
import com.yzw.demoim.im.IMAdpter;
import com.yzw.demoim.im.IMManger;
import com.yzw.demoim.im.PresenceListener;

import org.jivesoftware.smack.packet.Presence;
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
        , PresenceListener, RosterListener {


    private static final String TAG = RosterActivity.class.getName();
    private static final int SUBSCRIBE = 111;
    private static final int SHOW_SERACH_FRIENDS = 222;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.listview)
    ListView listview;

    private H handler;

    private MaterialDialog progress_Dialog;

    private List<Friend> fs;
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
            case SHOW_SERACH_FRIENDS:
                final List<String> list = (List<String>) msg.obj;
                String titile = list.size() == 0 ? "用户不存在" : "请选择要添加的好友";
                new MaterialDialog.Builder(RosterActivity.this)
                        .title(titile)
                        .adapter(
                                new SearchFriendAdapter(RosterActivity.this, list, R.layout.item_friend),
                                new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        addFriend(list.get(which));
                                        dialog.dismiss();
                                    }
                                })
                        .show();
                break;
        }
    }

    private void addFriend(final String s) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                handler.sendEmptyMessage(2);
                boolean res = imManger.addRoster(s, null, null);
                handler.sendMessage(Message.obtain(handler, 1, res));
                handler.sendEmptyMessage(3);


            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roster);
        ButterKnife.bind(this);

        toolbar.setTitle("联系人");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);


        fs = new ArrayList<>();
        adapter = new FriendAdapter(this, fs, R.layout.item_friend);
        listview.setAdapter(adapter);

        imManger = IMManger.getInstance();
        imManger.setPresenceListener(this);
        imManger.setRosterListener(this);

        progress_Dialog = new MaterialDialog.Builder(RosterActivity.this)
                .content("请稍等")
                .progress(true, 0).build();

        handler = new H(this);

        initContacts();
    }

    private void initContacts() {
        new Thread() {
            @Override
            public void run() {
                imManger = IMManger.getInstance();
                IMAdpter imAdpter = new IMAdpter(imManger);
                fs.clear();
                fs.addAll(imAdpter.getFriends());
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
                        imManger.deleteRoster(fs.get((int) id).getUsername());
                    }
                }).show();
        return true;
    }

    @OnItemClick(R.id.listview)
    public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
        Friend re = fs.get(position);
        Intent intent = new Intent(this, ChatAc.class);
        intent.putExtra(Friend.class.getName(), re);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.roster_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_roster:
                searchRoster();
                break;
            case R.id.add_group:
                addGroup();
                break;
        }
        return true;
    }

    private void addGroup() {
        new Thread() {
            @Override
            public void run() {
                Log.e(TAG, "search group info :" + imManger.getGroups().toString());
            }
        }.start();
        new MaterialDialog.Builder(RosterActivity.this)
                .title("请输入分组名")
                .customView(R.layout.input_box, true)
                .positiveText("确认")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        new Thread() {
                            @Override
                            public void run() {
                                String name = ((EditText) dialog.findViewById(R.id.input_box)).getText().toString();
                                boolean res = imManger.addGroup(name);
                                String t;
                                if (res) t = "创建成功";
                                else t = "创建失败";
                                handler.sendMessage(Message.obtain(handler, 1, t));
                            }
                        }.start();
                    }
                }).show();
    }

    private void searchRoster() {
        boolean wrapInScrollView = true;
        new MaterialDialog.Builder(this)
                .title("添加好友")
                .customView(R.layout.input_box, wrapInScrollView)
                .positiveText("添加")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        new Thread() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(2);
                                EditText box = (EditText) dialog.getCustomView().findViewById(R.id.input_box);
                                String username = box.getText().toString();
                                List<String> list = imManger.searchUser(username);
                                handler.sendMessage(Message.obtain(handler, SHOW_SERACH_FRIENDS, list));
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
    public void subscribed(final Presence presence) {
        handler.sendMessage(Message.obtain(handler, 4, "subscribed"));
        Log.e(TAG, "subscribed: " + presence.toString());
        new Thread() {
            @Override
            public void run() {
                imManger.handlerReceiverSubscribed(presence);
            }
        }.start();
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
        String user = presence.getFrom();
        Presence bestPresence = imManger.getRoster().getPresence(user);
        handler.sendMessage(Message.obtain(handler, 4, bestPresence.getFrom() + " **presenceChanged** mode " + bestPresence.getMode() + " type " + bestPresence.getType()));
    }

    // ------------------------------

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
