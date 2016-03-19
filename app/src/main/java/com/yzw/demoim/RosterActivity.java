package com.yzw.demoim;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.yzw.demoim.im.IMBaseActivity;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;

public class RosterActivity extends IMBaseActivity implements Toolbar.OnMenuItemClickListener {


    private static final String TAG = RosterActivity.class.getName();
    private static final int SUBSCRIBE = 111;
    private static final int SHOW_SERACH_FRIENDS = 222;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.listview)
    ListView listview;

    private Her handler;

    private MaterialDialog progress_Dialog;

    private List<RosterEntry> fs;
    private FriendAdapter adapter;

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
                                        if (mImServiceBinder != null)
                                            mImServiceBinder.agreeSubscribe(p);
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
                                        if (mImServiceBinder != null)
                                            mImServiceBinder.disagreeSubscribe(p);
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
                if (mImServiceBinder != null) {

                    handler.sendEmptyMessage(2);
                    boolean res = mImServiceBinder.addRoster(s);
                    handler.sendMessage(Message.obtain(handler, 1, res));
                    handler.sendEmptyMessage(3);

                }

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

        progress_Dialog = new MaterialDialog.Builder(RosterActivity.this)
                .content("请稍等")
                .progress(true, 0).build();

        handler = new Her(this);

        initContacts();
    }

    private void initContacts() {
        new Thread() {
            @Override
            public void run() {
                if (mImServiceBinder == null)
                    return;
                fs.clear();
                fs.addAll(mImServiceBinder.getRosterEntrys());
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
                        if (mImServiceBinder != null)
                            mImServiceBinder.deleteRoster(fs.get((int) id).getUser());
                    }
                }).show();
        return true;
    }

    @OnItemClick(R.id.listview)
    public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
        RosterEntry re = fs.get(position);
        Intent intent = new Intent(this, ChatAc.class);
        intent.putExtra("username", re.getUser());
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
                                if (mImServiceBinder == null)
                                    return;
//                                String name = ((EditText) dialog.findViewById(R.id.input_box)).getText().toString();
//                                boolean res = mImServiceBinder.addGroup(name);
//                                String t;
//                                if (res) t = "创建成功";
//                                else t = "创建失败";
//                                handler.sendMessage(Message.obtain(handler, 1, t));
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
                                if (mImServiceBinder == null)
                                    return;

                                handler.sendEmptyMessage(2);
                                EditText box = (EditText) dialog.getCustomView().findViewById(R.id.input_box);
                                String username = box.getText().toString();
                                List<String> list = mImServiceBinder.search(username);
                                handler.sendMessage(Message.obtain(handler, SHOW_SERACH_FRIENDS, list));
                                handler.sendEmptyMessage(3);
                            }
                        }.start();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImServiceBinder != null)
            mImServiceBinder.logout();
    }
}
