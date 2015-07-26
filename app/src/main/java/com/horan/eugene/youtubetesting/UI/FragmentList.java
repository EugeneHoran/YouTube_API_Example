package com.horan.eugene.youtubetesting.UI;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.horan.eugene.youtubetesting.AdaptersGettersSetters.VideoCategoryInfo;
import com.horan.eugene.youtubetesting.AdaptersGettersSetters.VideoCategoryRecyclerAdapter;
import com.horan.eugene.youtubetesting.R;
import com.horan.eugene.youtubetesting.Utilities.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FragmentList extends Fragment {

    public static FragmentList newInstance(String mId, int position) {
        Bundle args = new Bundle();
        args.putString("EXTRA_ID", mId);
        args.putInt("POSITION", position);
        FragmentList fragment = new FragmentList();
        fragment.setArguments(args);
        return fragment;
    }

    private String categoryId = "";
    private VideoCategoryRecyclerAdapter videoRecyclerAdapter;
    private List<VideoCategoryInfo> videoList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        categoryId = getArguments().getString("EXTRA_ID");
        videoList = new ArrayList<>();
        RecyclerView recycler = (RecyclerView) v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        videoRecyclerAdapter = new VideoCategoryRecyclerAdapter(getActivity(), videoList, R.layout.list_row_video);
        recycler.setAdapter(videoRecyclerAdapter);
        new LoadVideoData().execute("");
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    class LoadVideoData extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... params) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(Constants.API_LINK + "videos?videoEmbeddable=true&part=snippet&chart=mostPopular&regionCode=us&videoCategoryId=" + categoryId + "&key=" + Constants.API_KEY + "&maxResults=20");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                try {
                    JSONObject json = new JSONObject(result.toString());
                    JSONArray items = json.getJSONArray("items");
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject data = items.getJSONObject(i);
                        String id = data.getString("id");
                        JSONObject snippet = data.getJSONObject("snippet");
                        String defaultURL;
                        String highUrl;
                        if (snippet.isNull("thumbnails")) {
                            defaultURL = null;
                            highUrl = null;
                        } else {
                            JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                            JSONObject defaultThumbnail = thumbnails.getJSONObject("medium");
                            defaultURL = defaultThumbnail.getString("url");
                            JSONObject defaultThumbnailLarge = thumbnails.getJSONObject("high");
                            highUrl = defaultThumbnailLarge.getString("url");
                        }
                        String title = snippet.getString("title");
                        String description = snippet.getString("description");
                        VideoCategoryInfo info = new VideoCategoryInfo();
                        info.setId(id);
                        info.setUrlLarge(highUrl);
                        info.setTitle(title);
                        if (defaultURL != null)
                            info.setImage(defaultURL);
                        info.setDescription(description);
                        videoList.add(info);
                    }
                } catch (JSONException e) {
                    Log.e("JSON_EXCEPTION", categoryId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            videoRecyclerAdapter.notifyDataSetChanged();
        }
    }
}
