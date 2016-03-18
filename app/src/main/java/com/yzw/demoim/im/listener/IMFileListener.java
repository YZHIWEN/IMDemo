package com.yzw.demoim.im.listener;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;

/**
 * Created by yzw on 2016/3/18 0018.
 */
public class IMFileListener implements FileTransferListener {
    public static final String TAG = IMFileListener.class.getName();


    @Override
    public void fileTransferRequest(FileTransferRequest request) {

        Log.e(TAG, "fileTransferRequest: " + request.getFileName());
        Log.e(TAG, "fileTransferRequest: " + request.getMimeType());
        Log.e(TAG, "fileTransferRequest: " + request.getFileSize());
        Log.e(TAG, "fileTransferRequest: " + request.getRequestor());
        Log.e(TAG, "fileTransferRequest: " + request.getStreamID());
        EventBus.getDefault().post(request);
    }
}
