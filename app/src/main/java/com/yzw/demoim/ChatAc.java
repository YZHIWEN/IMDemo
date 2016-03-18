package com.yzw.demoim;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatAc extends BaseAc {

    private final static int FILE_CHOICE_CODE = 1111;
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

    @OnClick(R.id.file)
    public void choiceFile(View view) {
        showFileChooser();
    }

    /**
     * 调用文件选择软件来选择文件
     **/
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择文件"),
                    FILE_CHOICE_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;

        switch (requestCode) {
            case FILE_CHOICE_CODE:
                Uri uri = data.getData();
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
                int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                actualimagecursor.moveToFirst();
                String path = actualimagecursor.getString(actual_image_column_index);// 获取选择文件的路径
                Log.e(TAG, "onActivityResult: " + path);
                sendFile(path);
                break;
        }

    }

    private void sendFile(final String filepath) {
        new Thread() {
            @Override
            public void run() {
                File file = new File(filepath);
                if (!file.exists())
                    file.mkdir();
                Log.e(TAG, "run: " + file.toString() + " " + file.canRead() + " " + file.exists());
                imManger.sendFile(friend.getUsername(), file, "");
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
            case 4:
                Toast.makeText(this, (CharSequence) msg.obj, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Subscribe
    public void onEventMainThread(ChatMessage msg) {
        cmsglist.add(msg);
        her.sendEmptyMessage(3);
    }

    @Subscribe
    public void onEventMainThread(final FileTransferRequest request) {
        her.sendMessage(Message.obtain(her, 4, "accept"));
        new Thread() {
            @Override
            public void run() {
                try {

                    IncomingFileTransfer ift = request.accept();
                    InputStream in = ift.recieveFile();
                    File sdCard = Environment.getExternalStorageDirectory();
                    File directory_pictures = new File(sdCard, "Pictures");
                    File file = new File(directory_pictures, "file.jpg");
                    if (file.exists())
                        file.delete();
                    file.createNewFile();
                    FileOutputStream os = new FileOutputStream(file);
                    byte[] bs = new byte[1024];
                    while (in.read(bs) != -1) {
                        os.write(bs, 0, bs.length);
                    }
                    os.flush();
                    os.close();

                    her.sendMessage(Message.obtain(her, 4, "accept success"));
                } catch (Exception e) {
                    e.printStackTrace();
                    her.sendMessage(Message.obtain(her, 4, "accept fail"));
                }
            }
        }.start();
    }
}
