package com.horan.eugene.youtubetesting.GenerateCategories;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class CategoriesAdapter extends ArrayAdapter<Categories> {
    Context mContext;
    public static List<Categories> mLogs;

    public CategoriesAdapter(Context context, int textViewResourceId, List<Categories> logs) {
        super(context, textViewResourceId);
        mContext = context;
        mLogs = logs;
    }

    public void setLogs(List<Categories> logs) {
        mLogs = logs;
    }

    public List<Categories> getLogs() {
        return mLogs;
    }

    public void add(Categories log) {
        mLogs.add(log);
    }

    public int getCount() {
        return mLogs.size();
    }

    public Categories getItem(int position) {
        return mLogs.get(position);
    }

}
