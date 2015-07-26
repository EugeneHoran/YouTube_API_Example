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
import android.widget.Toast;

import com.horan.eugene.youtubetesting.R;
import com.horan.eugene.youtubetesting.UI.MainActivity;
import com.horan.eugene.youtubetesting.UI.VideoViewActivity;
import com.squareup.picasso.Picasso;

import java.util.List;


public class VideoSearchRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int FIRST = 0;
    private static final int SECOND = 1;

    private List<VideoSearchInfo> mVideoSearchInfo;
    private List<ChannelInfo> testingChannelses;
    private int itemLayout;
    private int headerLayout;
    public static Context context;

    public VideoSearchRecyclerAdapter(Context contex, List<ChannelInfo> logChannels, List<VideoSearchInfo> log, int headerLayout, int itemLayout) {
        context = contex;
        this.testingChannelses = logChannels;
        this.mVideoSearchInfo = log;
        this.headerLayout = headerLayout;
        this.itemLayout = itemLayout;

    }

    @Override
    public int getItemViewType(int position) {
        if (position >= testingChannelses.size()) {
            return SECOND;
        } else {
            return FIRST;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FIRST) {
            View v = LayoutInflater.from(parent.getContext()).inflate(headerLayout, parent, false);
            ViewHeader viewHeader = new ViewHeader(v);
            return viewHeader;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final MainActivity mainActivity = (MainActivity) context;
        if (holder instanceof ViewHeader) {
            final ViewHeader viewHeader = (ViewHeader) holder;
            final ChannelInfo item = testingChannelses.get(position);
            viewHeader.itemView.setTag(item);
            viewHeader.title.setText(item.getTitle());
            viewHeader.description.setText(item.getDescription());
            Picasso.with(context).load(item.getImage()).into(viewHeader.image);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mainActivity, position + "", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            final ViewHolder viewHolder = (ViewHolder) holder;
            final VideoSearchInfo item = mVideoSearchInfo.get(position - testingChannelses.size());
            viewHolder.itemView.setTag(item);
            viewHolder.title.setText(item.getTitle());
            viewHolder.description.setText(item.getDescription());
            Picasso.with(context).load(item.getImage()).resize(convertDpToPx(145), convertDpToPx(81)).into(viewHolder.image);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Intent intent = new Intent(mainActivity, VideoViewActivity.class);
                        intent.putExtra("VIDEO_ID", item.getId());
                        intent.putExtra("VIDEO_TITLE", item.getTitle());
                        intent.putExtra("VIDEO_DESCRIPTION", item.getDescription());
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(mainActivity,
                                Pair.create((View) viewHolder.title, "title"), Pair.create((View) viewHolder.description, "description"), Pair.create((View) viewHolder.image, "image"));
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

    }

    private int convertDpToPx(int dp) {
        return Math.round(dp * (context.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));

    }

    @Override
    public int getItemCount() {
        return mVideoSearchInfo.size() + testingChannelses.size();
    }

    public void add(VideoSearchInfo item) {
        mVideoSearchInfo.add(item);
    }

    public void remove(VideoSearchInfo item) {
        int position = mVideoSearchInfo.indexOf(item);
        mVideoSearchInfo.remove(position);
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

    public static class ViewHeader extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView description;
        public ImageView image;

        public ViewHeader(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}