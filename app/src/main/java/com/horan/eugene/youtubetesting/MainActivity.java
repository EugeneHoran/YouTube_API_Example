package com.horan.eugene.youtubetesting;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.horan.eugene.youtubetesting.GenerateCategories.Categories;
import com.horan.eugene.youtubetesting.GenerateCategories.CategoriesAdapter;

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


public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private TabLayout mTabs;
    private List<Categories> categoriesList;
    private CategoriesAdapter categoriesAdapter;
    private NavigationView mNavigationView;
    private DrawerLayout mNavigationDrawer;
    ImageView image_search_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        View customView = getLayoutInflater().inflate(R.layout.toolbar_search, null);
        getSupportActionBar().setCustomView(customView);
        Toolbar parent = (Toolbar) customView.getParent();
        parent.setContentInsetsAbsolute(0, 0);
        image_search_back = (ImageView) findViewById(R.id.image_search_back);
        image_search_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenCloseNavDrawer();
            }
        });
        // toolbar.setTitle("YouTube Categories");
        // toolbar.inflateMenu(R.menu.menu_main);
        //  toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        //  toolbar.setNavigationIcon(R.mipmap.ic_menu_black_24dp);
    /*    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenCloseNavDrawer();
            }
        });
        */
        mNavigationView = (NavigationView) findViewById(R.id.nav);
        mNavigationView.getMenu().getItem(0).setChecked(true);
        mNavigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                OpenCloseNavDrawer();
                viewPager.setCurrentItem(menuItem.getOrder() - 1);
                menuItem.setChecked(true);
                return false;
            }
        });
        mTabs = (TabLayout) findViewById(R.id.tabs);
        mTabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTabs.setTabTextColors(Color.parseColor("#80ffffff"), Color.parseColor("#000000"));
        viewPager = (ViewPager) findViewById(R.id.pager);
        categoriesList = new ArrayList<>();
        new LoadCategoryData().execute("");
    }

    private void testingPagerAdapter() {
        categoriesAdapter = new CategoriesAdapter(this, 0, categoriesList);
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Categories categories = categoriesAdapter.getItem(position);
                return FragmentList.newInstance(categories.getId(), position);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return categoriesAdapter.getItem(position).getTitle();
            }

            @Override
            public int getCount() {
                return categoriesAdapter.getCount();
            }
        });
        mTabs.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(categoriesAdapter.getCount());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * Used to handle the closing and opening of the Navigation Drawer.
     * Prevent repetitive statements
     */
    private void OpenCloseNavDrawer() {
        if (mNavigationDrawer != null) {
            if (mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
                mNavigationDrawer.closeDrawer(GravityCompat.START);
            } else {
                mNavigationDrawer.openDrawer(GravityCompat.START);
            }
        }
    }

    class LoadCategoryData extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... params) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL("https://www.googleapis.com/youtube/v3/videoCategories?part=snippet&regionCode=us&key=AIzaSyAwsxG8fj-uWHs_-azyUcLAusn1g4TwRR0");
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
                        JSONObject snippet = data.getJSONObject("snippet");
                        String id = data.getString("id");
                        String title = snippet.getString("title");
                        Categories info = new Categories();
                        info.setId(id);
                        info.setTitle(title);
                        categoriesList.add(info);
                    }
                    Log.e("RESULT", result.toString());
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
            mTabs.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            testingPagerAdapter();
        }
    }
}
