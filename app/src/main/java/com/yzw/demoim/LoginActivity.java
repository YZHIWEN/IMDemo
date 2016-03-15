package com.yzw.demoim;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yzw.demoim.im.IMManger;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginActivity extends AppCompatActivity implements ConnectionListener {

    private static final String TAG = LoginActivity.class.getName();
    @Bind(R.id.userName)
    EditText userName;
    @Bind(R.id.userPw)
    EditText userPw;
    @Bind(R.id.login)
    Button login;
    @Bind(R.id.register)
    Button register;
    private IMManger imManger;


    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
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
        };

        imManger = IMManger.getInstance();

    }

    @OnClick(R.id.login)
    public void login(View view) {
        login();
    }

    private void login() {
        new Thread() {
            @Override
            public void run() {
                boolean res = imManger.login(userName.getText().toString(), userPw.getText().toString());
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
                boolean res = imManger.registerUser(userName.getText().toString(), userPw.getText().toString());
                handler.sendMessage(android.os.Message.obtain(handler, 3, res));
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imManger.disConnect();
    }

    @Override
    public void authenticated(XMPPConnection arg0, boolean arg1) {
        Log.i(TAG, "Authenticated");
    }

    @Override
    public void connected(XMPPConnection arg0) {
        Log.i(TAG, "Connected");
//        try {
//            SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
//            SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
//
//            mConnection.login("test", "ilink@2012");
//        } catch (XMPPException | SmackException | IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, e.getMessage());
//        }
    }

    @Override
    public void connectionClosed() {
        Log.i(TAG, "Connection closed");
    }

    @Override
    public void connectionClosedOnError(Exception arg0) {
        Log.i(TAG, "Connection closed on error");
    }

    @Override
    public void reconnectingIn(int arg0) {
        Log.i(TAG, "Reconnecting in");
    }

    @Override
    public void reconnectionFailed(Exception arg0) {
        Log.i(TAG, "Reconnection failed");
    }

    @Override
    public void reconnectionSuccessful() {
        Log.i(TAG, "Reconnection successful");
    }
}
