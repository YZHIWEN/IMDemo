package com.yzw.demoim.im.callback;


import org.jivesoftware.smackx.filetransfer.FileTransferRequest;

/**
 * Created by yzw on 2016/3/19 0019.
 */
public interface IMCallBack {
    void receive(String user, String msg);

    void receive(String user,FileTransferRequest request);
}
