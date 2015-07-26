package com.horan.eugene.youtubetesting.SearchDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    // Adding new searchItem
    public void addSearchItem(SearchItem searchItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, searchItem.get_name()); // SearchItem Name
        // Inserting Row
        db.insert(TABLE_SEARCH, null, values);
        db.close(); // Closing database connection
    }

    // Query the search
    public List<SearchItem> getSearchItemFilter(String filter) {
        List<SearchItem> dailyCalorieIntakeList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_SEARCH + " WHERE " + KEY_NAME + " LIKE ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{filter + "%"});
        if (cursor.moveToFirst()) {
            do {
                SearchItem searchItem = new SearchItem();
                searchItem.set_id(Integer.parseInt(cursor.getString(0)));
                searchItem.set_name(cursor.getString(1));
                dailyCalorieIntakeList.add(searchItem);
            } while (cursor.moveToNext());
        }
        // return contact list
        return dailyCalorieIntakeList;
    }

    public List<SearchItem> getAllItems() {
        List<SearchItem> searchItemList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SEARCH;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SearchItem searchItem = new SearchItem();
                searchItem.set_id(Integer.parseInt(cursor.getString(0)));
                searchItem.set_name(cursor.getString(1));
                // Adding searchItem to list
                searchItemList.add(searchItem);
            } while (cursor.moveToNext());
        }

        // return contact list
        return searchItemList;
    }
}