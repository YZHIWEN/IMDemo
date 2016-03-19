package com.yzw.demoim;

import android.content.Context;

import com.yzw.demoim.utils.CommonAdapter;
import com.yzw.demoim.utils.ViewHolder;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.List;

/**
 * Created by Administrator on 2016/3/13 0013.
 */
public class FriendAdapter extends CommonAdapter<RosterEntry> {

    public FriendAdapter(Context context, List<RosterEntry> datas, int mLayoutId) {
        super(context, datas, mLayoutId);
    }

    @Override
    public void convert(RosterEntry f, ViewHolder viewHolder) {
        viewHolder.setText(R.id.item_friend_tv, f.getUser());
    }
}
