package com.horan.eugene.youtubetesting.SearchDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.horan.eugene.youtubetesting.AdaptersGettersSetters.SearchHistoryItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    /**
     * All Static variables
     */
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "searchManager";

    // Contacts table name
    private static final String TABLE_SEARCH = "searchItems";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SEARCH + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH);
        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new searchHistoryItem
    public void addSearchItem(SearchHistoryItem searchHistoryItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, searchHistoryItem.get_name()); // SearchHistoryItem Name
        // Inserting Row
        db.insert(TABLE_SEARCH, null, values);
        db.close(); // Closing database connection
    }

    // Query the search
    public List<SearchHistoryItem> getSearchItemFilter(String filter) {
        List<SearchHistoryItem> dailyCalorieIntakeList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_SEARCH + " WHERE " + KEY_NAME + " LIKE ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{filter + "%"});
        if (cursor.moveToFirst()) {
            do {
                SearchHistoryItem searchHistoryItem = new SearchHistoryItem();
                searchHistoryItem.set_id(Integer.parseInt(cursor.getString(0)));
                searchHistoryItem.set_name(cursor.getString(1));
                dailyCalorieIntakeList.add(searchHistoryItem);
            } while (cursor.moveToNext());
        }
        // return contact list
        return dailyCalorieIntakeList;
    }

    public void removeAll() {
        SQLiteDatabase db = this.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
        db.delete(this.TABLE_SEARCH, null, null);
    }

    public List<SearchHistoryItem> getAllItems() {
        List<SearchHistoryItem> searchHistoryItemList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SEARCH + " ORDER BY " + KEY_ID + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SearchHistoryItem searchHistoryItem = new SearchHistoryItem();
                searchHistoryItem.set_id(Integer.parseInt(cursor.getString(0)));
                searchHistoryItem.set_name(cursor.getString(1));
                // Adding searchHistoryItem to list
                searchHistoryItemList.add(searchHistoryItem);
            } while (cursor.moveToNext());
        }

        // return contact list
        return searchHistoryItemList;
    }
}