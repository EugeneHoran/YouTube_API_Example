package com.horan.eugene.youtubetesting.SearchDatabase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.horan.eugene.youtubetesting.R;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> implements View.OnClickListener {
    private List<SearchItem> mSearchItem;
    private int itemLayout;
    private OnRecyclerViewItemClickListener<SearchItem> itemClickListener;
    public static Context context;

    public SearchAdapter(Context contex, List<SearchItem> log, int itemLayout) {
        context = contex;
        this.mSearchItem = log;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final SearchItem item = mSearchItem.get(position);
        holder.itemView.setTag(item);
        holder.title.setText(item.get_name());
    }

    public SearchItem getItem(int position) {
        return mSearchItem.get(position);
    }

    @Override
    public int getItemCount() {
        return mSearchItem.size();
    }

    public void add(SearchItem item) {
        mSearchItem.add(item);
    }

    public void remove(SearchItem item) {
        int position = mSearchItem.indexOf(item);
        mSearchItem.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;


        public ViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.search);
        }
    }

    @Override
    public void onClick(View view) {
        if (itemClickListener != null) {
            SearchItem model = (SearchItem) view.getTag();
            itemClickListener.onItemClick(view, model);
        }
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener<SearchItem> listener) { // Click Lister
        this.itemClickListener = listener;
    }
}