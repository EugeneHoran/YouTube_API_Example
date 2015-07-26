package com.horan.eugene.youtubetesting.AdaptersGettersSetters;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.horan.eugene.youtubetesting.UI.MainActivity;
import com.horan.eugene.youtubetesting.R;
import com.horan.eugene.youtubetesting.UI.VideoViewActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoCategoryRecyclerAdapter extends RecyclerView.Adapter<VideoCategoryRecyclerAdapter.ViewHolder> {
    private List<VideoCategoryInfo> mVideoCategoryInfo;
    private int itemLayout;
    public static Context context;

    public VideoCategoryRecyclerAdapter(Context contex, List<VideoCategoryInfo> log, int itemLayout) {
        context = contex;
        this.mVideoCategoryInfo = log;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final VideoCategoryInfo item = mVideoCategoryInfo.get(position);
        holder.itemView.setTag(item);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        Picasso.with(context).load(item.getImage()).resize(convertDpToPx(145), convertDpToPx(81)).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    MainActivity mainActivity = (MainActivity) context;
                    Intent intent = new Intent(mainActivity, VideoViewActivity.class);
                    intent.putExtra("VIDEO_ID", item.getId());
                    intent.putExtra("VIDEO_TITLE", item.getTitle());
                    intent.putExtra("VIDEO_DESCRIPTION", item.getDescription());
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(mainActivity,
                            Pair.create((View) holder.title, "title"), Pair.create((View) holder.description, "description"), Pair.create((View) holder.image, "image"));
                    mainActivity.startActivity(intent, options.toBundle());
                } else {
                    Intent video = new Intent(context, VideoViewActivity.class);
                    video.putExtra("VIDEO_ID", item.getId());
                    video.putExtra("VIDEO_TITLE", item.getTitle());
                    video.putExtra("VIDEO_DESCRIPTION", item.getDescription());
                    context.startActivity(video);
                }
            }
        });
    }

    private int convertDpToPx(int dp) {
        return Math.round(dp * (context.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));

    }

    @Override
    public int getItemCount() {
        return mVideoCategoryInfo.size();
    }

    public void add(VideoCategoryInfo item) {
        mVideoCategoryInfo.add(item);
    }

    public void remove(VideoCategoryInfo item) {
        int position = mVideoCategoryInfo.indexOf(item);
        mVideoCategoryInfo.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView description;
        public ImageView image;

        public ViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            image = (ImageView) itemView.findViewById(R.id.image);

        }
    }
}