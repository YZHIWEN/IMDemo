package com.yzw.demoim;

import android.content.Context;

import com.yzw.demoim.utils.CommonAdapter;
import com.yzw.demoim.utils.ViewHolder;

import java.util.List;

/**
 * Created by yzw on 2016/3/16 0016.
 */
public class SearchFriendAdapter extends CommonAdapter<String> {
    public SearchFriendAdapter(Context context, List<String> datas, int mLayoutId) {
        super(context, datas, mLayoutId);
    }

    @Override
    public void convert(String s, ViewHolder viewHolder) {
        viewHolder.setText(R.id.item_friend_tv,s);
    }
}
