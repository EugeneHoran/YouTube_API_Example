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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.horan.eugene.youtubetesting.AdaptersGettersSetters.VideoSearchInfo;
import com.horan.eugene.youtubetesting.AdaptersGettersSetters.VideoSearchRecyclerAdapter;
import com.horan.eugene.youtubetesting.R;
import com.horan.eugene.youtubetesting.SearchDatabase.DatabaseHandler;
import com.horan.eugene.youtubetesting.SearchDatabase.OnRecyclerViewItemClickListener;
import com.horan.eugene.youtubetesting.SearchDatabase.SearchAdapter;
import com.horan.eugene.youtubetesting.SearchDatabase.SearchItem;
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
    private View v;
    // Context
    private MainActivity mainActivity;

    private EditText search;
    private RelativeLayout loading;
    private RecyclerView recyclerSearch;
    private RelativeLayout resultContainer;
    private ImageView clearSearch;

    // Search Result Items
    private List<VideoSearchInfo> videoSearchInfo;
    private VideoSearchRecyclerAdapter videoSearchRecyclerAdapter;
    private ActionBarDrawerToggle toolbarDrawerToggle;

    // Search History Items
    private DatabaseHandler db;
    private SearchAdapter searchAdapter;

    // Retain State
    private boolean retain_nav_state = true;
    private static String RETAIN_NAV_STATE = "retain_search_state";

    private void addSearchItem() {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < searchAdapter.getItemCount(); i++) {
            SearchItem ls = searchAdapter.getItem(i);
            String name = ls.get_name();
            set.add(name.toUpperCase());
        }
        if (set.add(search.getText().toString().toUpperCase())) {
            SearchItem searchItem = new SearchItem();
            searchItem.set_name(search.getText().toString());
            db.addSearchItem(searchItem);
            searchAdapter.add(searchItem);
            searchAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_search, container, false);
        mainActivity = (MainActivity) getActivity();
        if (savedInstanceState != null) {
            retain_nav_state = savedInstanceState.getBoolean(RETAIN_NAV_STATE);
        }

        loading = (RelativeLayout) v.findViewById(R.id.loading);
        search = (EditText) v.findViewById(R.id.edit_text_search);
        clearSearch = (ImageView) v.findViewById(R.id.clearSearch);
        search.requestFocus();
        resultContainer = (RelativeLayout) v.findViewById(R.id.resultContainer);
        HandleAnimations();

        //Search History
        db = new DatabaseHandler(getActivity());

        //Search Results
        videoSearchInfo = new ArrayList<>();
        videoSearchRecyclerAdapter = new VideoSearchRecyclerAdapter(mainActivity, videoSearchInfo, R.layout.list_row_video);


        recyclerSearch = (RecyclerView) v.findViewById(R.id.recyclerSearch);
        recyclerSearch.setLayoutManager(new LinearLayoutManager(mainActivity));
        searchAdapterClick(0, "");

        // Initiate Search and Save Item to History
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (search.getText().toString().trim().length() > 0) {
                        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(), 0);
                        addSearchItem();
                        new LoadVideoData().execute(search.getText().toString());
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
                    searchAdapterClick(1, filter);
                } else {
                    clearSearch.setImageResource(R.mipmap.ic_keyboard_voice_black_24dp);
                    searchAdapterClick(0, "");
                }
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
                    searchAdapterClick(0, "");
                    clearSearch.setImageResource(R.mipmap.ic_keyboard_voice_black_24dp);
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

    private void searchAdapterClick(int pos, String s) {
        switch (pos) {
            case 0:
                List<SearchItem> listSearch = db.getAllItems();
                searchAdapter = new SearchAdapter(getActivity(), listSearch, R.layout.list_row_search);
                recyclerSearch.setAdapter(searchAdapter);
                searchAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener<SearchItem>() {
                    @Override
                    public void onItemClick(View view, SearchItem searchItem) {
                        search.setText(searchItem.get_name());
                        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(), 0);
                        new LoadVideoData().execute(searchItem.get_name());
                    }
                });
                break;
            case 1:
                List<SearchItem> listSearch1 = db.getSearchItemFilter(s);
                searchAdapter = new SearchAdapter(getActivity(), listSearch1, R.layout.list_row_search);
                recyclerSearch.setAdapter(searchAdapter);
                searchAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener<SearchItem>() {
                    @Override
                    public void onItemClick(View view, SearchItem searchItem) {
                        search.setText(searchItem.get_name());
                        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(), 0);
                        new LoadVideoData().execute(searchItem.get_name());
                    }
                });
                break;

            default:
                break;
        }
    }

    private void checkAsyncTaskStatus() {
        LoadVideoData loadVideoData = new LoadVideoData();
        if (loadVideoData.getStatus() == AsyncTask.Status.PENDING) {
            // AsyncTask has not started yet
        }

        if (loadVideoData.getStatus() == AsyncTask.Status.RUNNING) {
            loadVideoData.cancel(true);
            loading.setVisibility(View.GONE);
            recyclerSearch.setVisibility(View.VISIBLE);
        }

        if (loadVideoData.getStatus() == AsyncTask.Status.FINISHED) {
            // AsyncTask is done and onPostExecute was called
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(RETAIN_NAV_STATE, toolbarDrawerToggle.isDrawerIndicatorEnabled());
    }

    class LoadVideoData extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
            recyclerSearch.setVisibility(View.GONE);
        }

        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... params) {
            videoSearchInfo.clear();
            Log.e("PARAMAS", params[0] + "");
            String searchItemFirst = params[0];
            searchItemFirst = searchItemFirst.replace(" ", "+");
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(Constants.API_LINK + "search?safeSearch=none&videoType=any&videoDefinition=any&part=snippet&q=" + searchItemFirst + "&type=video&key=" + Constants.API_KEY + "&maxResults=20");
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
                urlConnection.disconnect();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            loading.setVisibility(View.GONE);
            recyclerSearch.setVisibility(View.VISIBLE);
            videoSearchRecyclerAdapter.notifyDataSetChanged();
            recyclerSearch.setAdapter(videoSearchRecyclerAdapter);
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
