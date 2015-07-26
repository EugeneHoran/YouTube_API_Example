package com.horan.eugene.youtubetesting.UI;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.horan.eugene.youtubetesting.AdaptersGettersSetters.ChannelInfo;
import com.horan.eugene.youtubetesting.AdaptersGettersSetters.VideoSearchInfo;
import com.horan.eugene.youtubetesting.AdaptersGettersSetters.VideoSearchRecyclerAdapter;
import com.horan.eugene.youtubetesting.R;
import com.horan.eugene.youtubetesting.SearchDatabase.DatabaseHandler;
import com.horan.eugene.youtubetesting.AdaptersGettersSetters.SearchHistoryAdapter;
import com.horan.eugene.youtubetesting.AdaptersGettersSetters.SearchHistoryItem;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class FragmentSearch extends Fragment {
    // Inflate
    private View v;
    // Context
    private MainActivity mainActivity;
    // Widgets
    private EditText search;
    private RelativeLayout loading;
    private RecyclerView recyclerSearch;
    private RelativeLayout resultContainer;
    private ImageView clearSearch;
    private Button deleteHistory;

    // Search Result Items
    private List<ChannelInfo> chanelSearchInfo;
    private List<VideoSearchInfo> videoSearchInfo;
    private VideoSearchRecyclerAdapter videoSearchRecyclerAdapter;
    private ActionBarDrawerToggle toolbarDrawerToggle;

    // Search History Items
    private DatabaseHandler db;
    private SearchHistoryAdapter searchHistoryAdapter;

    // URL Connections
    private HttpURLConnection urlConnectionChannel;
    private HttpURLConnection urlConnectionVideos;

    // AsyncTask
    private LoadVideoData loadVideoData;

    // Retain State
    private boolean retain_nav_state = true;
    private static String RETAIN_NAV_STATE = "retain_search_state";

    private void addSearchItem() {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < searchHistoryAdapter.getItemCount(); i++) {
            SearchHistoryItem ls = searchHistoryAdapter.getItem(i);
            String name = ls.get_name();
            set.add(name.toUpperCase());
        }
        if (set.add(search.getText().toString().toUpperCase())) {
            SearchHistoryItem searchHistoryItem = new SearchHistoryItem();
            searchHistoryItem.set_name(search.getText().toString());
            db.addSearchItem(searchHistoryItem);
            searchHistoryAdapter.add(searchHistoryItem);
            searchHistoryAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_search, container, false);
        mainActivity = (MainActivity) getActivity();
        if (savedInstanceState != null) {
            retain_nav_state = savedInstanceState.getBoolean(RETAIN_NAV_STATE);
        }
        loadVideoData = new LoadVideoData();
        loading = (RelativeLayout) v.findViewById(R.id.loading);
        search = (EditText) v.findViewById(R.id.edit_text_search);
        clearSearch = (ImageView) v.findViewById(R.id.clearSearch);
        search.requestFocus();
        resultContainer = (RelativeLayout) v.findViewById(R.id.resultContainer);
        HandleAnimations();

        //Search History
        db = new DatabaseHandler(getActivity());
        deleteHistory = (Button) v.findViewById(R.id.deleteHistory);
        deleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.removeAll();
                for (int i = 0; i < searchHistoryAdapter.getItemCount(); i++) {
                    SearchHistoryItem searchHistoryItem1 = searchHistoryAdapter.getItem(i);
                    searchHistoryAdapter.remove(searchHistoryItem1);
                    searchHistoryAdapter.notifyDataSetChanged();
                }
                InitiateHistoryAdapter(0, "");
            }
        });
        //Search Results
        videoSearchInfo = new ArrayList<>();
        chanelSearchInfo = new ArrayList<>();

        videoSearchRecyclerAdapter = new VideoSearchRecyclerAdapter(mainActivity, chanelSearchInfo, videoSearchInfo, R.layout.list_row_channel, R.layout.list_row_video);

        recyclerSearch = (RecyclerView) v.findViewById(R.id.recyclerSearch);
        recyclerSearch.setLayoutManager(new LinearLayoutManager(mainActivity));
        InitiateHistoryAdapter(0, "");

        // Initiate Search and Save Item to History
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (search.getText().toString().trim().length() > 0) {
                        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(), 0);
                        addSearchItem();
                        loadVideoData.execute(search.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (search.getText().toString().length() > 0) {
                    clearSearch.setImageResource(R.mipmap.ic_cancel_black_24dp);
                    String filter = search.getText().toString();
                    InitiateHistoryAdapter(1, filter);
                } else {
                    clearSearch.setImageResource(R.mipmap.ic_keyboard_voice_black_24dp);
                    InitiateHistoryAdapter(0, "");
                }
                deleteHistory.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search.getText().toString().length() > 0) {
                    checkAsyncTaskStatus();
                    search.setText("");
                    InitiateHistoryAdapter(0, "");
                    clearSearch.setImageResource(R.mipmap.ic_keyboard_voice_black_24dp);
                    deleteHistory.setVisibility(View.VISIBLE);
                } else {
                    search.setText("");
                }
            }
        });
        if (getArguments() != null) {
            search.setText(getArguments().getString("SEARCH_ITEM"));
            new LoadVideoData().execute(getArguments().getString("SEARCH_ITEM"));
        }
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(RETAIN_NAV_STATE, toolbarDrawerToggle.isDrawerIndicatorEnabled());
    }

    // From Adapter Set Text
    public void setSearchText(String s) {
        if (search != null) {
            search.setText(s);
        }
    }

    // From Adapter Set Text Then Search
    public void setSearchTextSearch(String s) {
        if (search != null) {
            search.setText(s);
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(), 0);
            loadVideoData.execute(s);
        }
    }

    private void InitiateHistoryAdapter(int pos, String s) {
        switch (pos) {
            case 0:
                List<SearchHistoryItem> listSearch = db.getAllItems();
                searchHistoryAdapter = new SearchHistoryAdapter(getActivity(), listSearch, R.layout.list_row_search);
                recyclerSearch.setAdapter(searchHistoryAdapter);
                break;
            case 1:
                List<SearchHistoryItem> listSearch1 = db.getSearchItemFilter(s);
                searchHistoryAdapter = new SearchHistoryAdapter(getActivity(), listSearch1, R.layout.list_row_search);
                recyclerSearch.setAdapter(searchHistoryAdapter);
                break;

            default:
                break;
        }
    }

    class LoadVideoData extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
            recyclerSearch.setVisibility(View.GONE);
            deleteHistory.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            String searchItemFirst = params[0];
            searchItemFirst = searchItemFirst.replace(" ", "+");
            Channels(searchItemFirst);
            Videos(searchItemFirst);
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            loadVideoData = new LoadVideoData();
            loading.setVisibility(View.GONE);
            recyclerSearch.setVisibility(View.VISIBLE);
            videoSearchRecyclerAdapter.notifyDataSetChanged();
            recyclerSearch.setAdapter(videoSearchRecyclerAdapter);
        }
    }

    private void Channels(String s) {
        chanelSearchInfo.clear();
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + s + "&type=channel&maxResults=1&key=" + Constants.API_KEY);
            urlConnectionChannel = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnectionChannel.getInputStream());
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
                    JSONObject id = data.getJSONObject("id");
                    String channelId = id.getString("channelId");
                    JSONObject snippet = data.getJSONObject("snippet");
                    String title = snippet.getString("title");
                    String description = snippet.getString("description");
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
                    ChannelInfo test = new ChannelInfo();
                    test.setId(channelId);
                    test.setTitle(title);
                    test.setUrlLarge(highUrl);
                    test.setTitle(title);
                    if (defaultURL != null)
                        test.setImage(defaultURL);
                    test.setDescription(description);
                    chanelSearchInfo.add(test);
                }
            } catch (JSONException e) {
                Log.e("JSON_EXCEPTION", e.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnectionChannel.disconnect();
        }
    }


    private void Videos(String s) {
        videoSearchInfo.clear();
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL("https://www.googleapis.com/youtube/v3/search?videoEmbeddable=true&order=rating&part=snippet&q=" + s + "&type=video&maxResults=20&key=" + Constants.API_KEY);
            urlConnectionVideos = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnectionVideos.getInputStream());
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
                    JSONObject id = data.getJSONObject("id");
                    String videoId = id.getString("videoId");
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
                    VideoSearchInfo info = new VideoSearchInfo();
                    info.setId(videoId);
                    info.setUrlLarge(highUrl);
                    info.setTitle(title);
                    if (defaultURL != null)
                        info.setImage(defaultURL);
                    info.setDescription(description);
                    videoSearchInfo.add(info);
                }
            } catch (JSONException e) {
                Log.e("JSON_EXCEPTION", e.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnectionVideos.disconnect();
        }
    }

    private void checkAsyncTaskStatus() {
        if (loadVideoData.getStatus() == AsyncTask.Status.RUNNING) {
            loadVideoData.cancel(true);
            loading.setVisibility(View.GONE);
            recyclerSearch.setVisibility(View.VISIBLE);
            loadVideoData = new LoadVideoData();
        }
    }


    /**
     * Annimations
     */
    private void HandleAnimations() {
        // Fade in & Fade Out
        final Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(300);
        final Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator());
        fadeOut.setDuration(300);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar1);
        mainActivity.setSupportActionBar(toolbar);
        final DrawerLayout drawerLayout = (DrawerLayout) mainActivity.findViewById(R.id.drawer_layout);
        toolbarDrawerToggle = new ActionBarDrawerToggle(
                mainActivity,
                drawerLayout,
                toolbar,
                0,
                0
        );
        toolbarDrawerToggle.setDrawerIndicatorEnabled(retain_nav_state);
        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                toolbarDrawerToggle.onDrawerSlide(drawerLayout, slideOffset);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(300);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                resultContainer.startAnimation(fadeIn);
                InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                toolbarDrawerToggle.setDrawerIndicatorEnabled(false);
                if (!toolbarDrawerToggle.isDrawerIndicatorEnabled()) {
                    mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
        toolbarDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarDrawerToggle.setDrawerIndicatorEnabled(true);
                ValueAnimator anim = ValueAnimator.ofFloat(1, 0);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float slideOffset = (Float) valueAnimator.getAnimatedValue();
                        toolbarDrawerToggle.onDrawerSlide(drawerLayout, slideOffset);
                    }
                });
                anim.setInterpolator(new DecelerateInterpolator());
                anim.setDuration(300);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        resultContainer.startAnimation(fadeOut);
                        ((InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(), 0);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mainActivity.getSupportFragmentManager().popBackStack();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                anim.start();
                //
            }
        });
        toolbarDrawerToggle.syncState();
    }
}
