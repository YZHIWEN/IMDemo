package com.yzw.demoim;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yzw.demoim.im.IMBaseActivity;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginActivity extends IMBaseActivity {

    private static final String TAG = LoginActivity.class.getName();
    @Bind(R.id.userName)
    EditText userName;
    @Bind(R.id.userPw)
    EditText userPw;
    @Bind(R.id.login)
    Button login;
    @Bind(R.id.register)
    Button register;


    private Her handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        handler = new Her(this);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 1:
                Toast.makeText(LoginActivity.this, "init IM Manager success", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(LoginActivity.this, "init IM Manager error", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(LoginActivity.this, msg.obj + "", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @OnClick(R.id.login)
    public void login(View view) {
        login();
    }

    private void login() {
        new Thread() {
            @Override
            public void run() {
                if (mImServiceBinder == null)
                    return;

                boolean res = mImServiceBinder.login(userName.getText().toString(), userPw.getText().toString());
                if (res) {
                    handler.sendMessage(android.os.Message.obtain(handler, 3, true));
                    Intent intent = new Intent(LoginActivity.this, RosterActivity.class);
                    LoginActivity.this.startActivity(intent);
                } else {
                    handler.sendMessage(android.os.Message.obtain(handler, 3, false));
                }
            }
        }.start();
    }

    @OnClick(R.id.register)
    public void register(View view) {
        register();
    }

    private void register() {
        new Thread() {
            @Override
            public void run() {
                if (mImServiceBinder == null)
                    return;
                boolean res = mImServiceBinder.register(userName.getText().toString(), userPw.getText().toString());
                handler.sendMessage(android.os.Message.obtain(handler, 3, res));
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
