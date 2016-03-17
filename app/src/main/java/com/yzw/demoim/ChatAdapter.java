package com.yzw.demoim;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yzw.demoim.bean.ChatMessage;

import java.util.List;

/**
 * Created by Administrator on 2016/3/17 0017.
 */
public class ChatAdapter extends BaseAdapter {

    private static final int TYPE_SEND = 0;
    private static final int TYPE_RECEIVE = 1;


    private List<ChatMessage> msglist;
    private Context context;

    public ChatAdapter(Context context, List<ChatMessage> list) {
        this.context = context;
        this.msglist = list;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return msglist.get(position).getType() == ChatMessage.Type.SEND ?
                TYPE_SEND : TYPE_RECEIVE;
    }

    @Override
    public int getCount() {
        return msglist.size();
    }

    @Override
    public Object getItem(int position) {
        return msglist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VH vh = null;
        if (convertView == null) {
            if (msglist.get(position).getType() == ChatMessage.Type.SEND)
                convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_send, parent, false);
            else if (msglist.get(position).getType() == ChatMessage.Type.RECEIVE)
                convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_receive, parent, false);
            vh = new VH();
            if (convertView == null)
                throw new NullPointerException("why null??");
            convertView.setTag(vh);
            vh.tv = (TextView) convertView.findViewById(R.id.content);
        } else {
            vh = (VH) convertView.getTag();
        }

        vh.tv.setText(msglist.get(position).getBody());
        return convertView;
    }

    class VH {
        TextView tv;
    }

}
