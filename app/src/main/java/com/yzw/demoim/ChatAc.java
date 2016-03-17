package com.yzw.demoim;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.yzw.demoim.bean.ChatMessage;
import com.yzw.demoim.bean.Friend;
import com.yzw.demoim.im.IMManger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatAc extends BaseAc {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.content)
    EditText content;
    @Bind(R.id.send)
    Button send;
    @Bind(R.id.file)
    Button file;
    @Bind(R.id.bottom_layout)
    LinearLayout bottomLayout;
    @Bind(R.id.listview)
    ListView listview;

    private Friend friend;
    private IMManger imManger;
    private Her her;
    private EventBus eb;
    private List<ChatMessage> cmsglist;
    private ChatAdapter adpter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        friend = (Friend) getIntent().getSerializableExtra(Friend.class.getName());

        imManger = IMManger.getInstance();
        her = new Her(this);
        cmsglist = new ArrayList<>();
        adpter = new ChatAdapter(this, cmsglist);
        listview.setAdapter(adpter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: 2016/3/17 0017 需要读取接收不到信息 ？？？？？
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.send)
    public void send(View view) {
        final String c = content.getText().toString();
        if (c == null || "".equals(c))
            return;
        new Thread() {
            @Override
            public void run() {

                ChatMessage cm = new ChatMessage();
                cm.setBody(c);
                cm.setType(ChatMessage.Type.SEND);
                cm.setFrom(friend.getUsername());
                cmsglist.add(cm);
                her.sendEmptyMessage(3);
                boolean res = imManger.send(friend.getUsername(), c);
                if (res)
                    her.sendEmptyMessage(1);
                else
                    her.sendEmptyMessage(2);
            }
        }.start();
    }

    @Override
    public void handlerMessage(Message msg) {
        switch (msg.what) {
            case 1:
                Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                content.setText("");
                break;
            case 2:
                Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show();
                content.setText("");
                break;
            case 3:
                adpter.notifyDataSetChanged();
                break;
        }
    }

    @Subscribe
    public void onEventMainThread(ChatMessage msg) {
        cmsglist.add(msg);
        her.sendEmptyMessage(3);
    }
}
