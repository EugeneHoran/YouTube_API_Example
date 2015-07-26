package com.horan.eugene.youtubetesting.SearchDatabase;

import android.view.View;

public interface OnRecyclerViewItemClickListener<SearchIem> {
    void onItemClick(View view, SearchIem searchIem);
}