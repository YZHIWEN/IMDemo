package com.yzw.demoim.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by yzw on 2015/8/20 0020.
 */
public abstract class CommonAdapter<T> extends BaseAdapter {

    protected Context mContext;
    protected List<T> mDatas;
    protected LayoutInflater mInflater;
    private int mLayoutId;

    public CommonAdapter(Context context, List<T> datas, int mLayoutId) {
        this.mDatas = datas;
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
        this.mLayoutId = mLayoutId;
    }


    @Override
    public int getCount() {
        return mDatas.size();
    }

    // 返回值是T
    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // 抽象getView（） 不同ListView的getView的item返回不同
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 获取ViewHolder
        ViewHolder viewHolder = ViewHolder.get(mContext, mLayoutId, position, convertView, parent);

        T t = mDatas.get(position);
        convert(t, viewHolder);

        return viewHolder.getConvertView();
    }

    /**
     * 根据实际item情况获取控件跟初始化
     *
     * @param t
     * @param viewHolder
     */
    public abstract void convert(T t, ViewHolder viewHolder);

}
