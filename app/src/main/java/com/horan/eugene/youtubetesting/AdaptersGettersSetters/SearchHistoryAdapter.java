package com.horan.eugene.youtubetesting.AdaptersGettersSetters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.horan.eugene.youtubetesting.R;
import com.horan.eugene.youtubetesting.UI.FragmentSearch;
import com.horan.eugene.youtubetesting.UI.MainActivity;

import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {
    private List<SearchHistoryItem> mSearchHistoryItem;
    private int itemLayout;
    public static Context context;

    public SearchHistoryAdapter(Context contex, List<SearchHistoryItem> log, int itemLayout) {
        context = contex;
        this.mSearchHistoryItem = log;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final SearchHistoryItem item = mSearchHistoryItem.get(position);
        holder.itemView.setTag(item);
        holder.title.setText(item.get_name());
        MainActivity mainActivity = (MainActivity) context;
        final FragmentSearch fragmentSearch = (FragmentSearch) mainActivity.getSupportFragmentManager().findFragmentByTag("SEARCH");
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragmentSearch != null) {
                    fragmentSearch.setSearchText(item.get_name());
                }
            }
        });
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragmentSearch != null) {
                    fragmentSearch.setSearchTextSearch(item.get_name());
                }
            }
        });
    }

    public SearchHistoryItem getItem(int position) {
        return mSearchHistoryItem.get(position);
    }

    @Override
    public int getItemCount() {
        return mSearchHistoryItem.size();
    }

    public void add(SearchHistoryItem item) {
        mSearchHistoryItem.add(item);
    }

    public void remove(SearchHistoryItem item) {
        int position = mSearchHistoryItem.indexOf(item);
        mSearchHistoryItem.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView image;

        public ViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.search);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}