package com.example.newsandweather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "bookmark.db";
    private static final String DATABASE_NEWS_TABLE = "news";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_USER_ID = "userid";
    private static final String COLUMN_AUTHOR = "author";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_IMAGEURL = "imageurl";
    private static final String COLUMN_PUBLICATONTIME = "publicationtime";

    private static final String DATABASE_WEATHER_TABLE = "weather";
    private static final String COLUMN_USER_CITY = "city";

    DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query1 = "CREATE TABLE "+ DATABASE_NEWS_TABLE + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " TEXT, " +
                COLUMN_AUTHOR + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_URL + " TEXT, " +
                COLUMN_IMAGEURL + " TEXT, " +
                COLUMN_PUBLICATONTIME + " TEXT "
                + ");";

        String query2 = "CREATE TABLE " + DATABASE_WEATHER_TABLE + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " TEXT, " +
                COLUMN_USER_CITY + " TEXT " + ");";

        db.execSQL(query1);
        db.execSQL(query2);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_NEWS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_WEATHER_TABLE);
        onCreate(db);
    }


    boolean bookmarkNews(Bookmark bookmark){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USER_ID, bookmark.getUserId().trim());
        contentValues.put(COLUMN_AUTHOR, bookmark.getAuthor());
        contentValues.put(COLUMN_TITLE, bookmark.getTitle());
        contentValues.put(COLUMN_DESCRIPTION, bookmark.getDescription());
        contentValues.put(COLUMN_URL, bookmark.getUrl());
        contentValues.put(COLUMN_IMAGEURL, bookmark.getImageUrl());
        contentValues.put(COLUMN_PUBLICATONTIME, bookmark.getPublicationTime());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(DATABASE_NEWS_TABLE, null, contentValues);
        db.close();
        return true;
    }



    Cursor getBookmarks(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from "+ DATABASE_NEWS_TABLE +
                " order by " + COLUMN_ID + " desc", null);
        return res;
    }

    boolean deleteBookmark(Bookmark bookmark){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + DATABASE_NEWS_TABLE + " WHERE " + COLUMN_AUTHOR +
                " = \"" + bookmark.getAuthor() +
                "\" AND " + COLUMN_TITLE + " = \"" +  bookmark.getTitle() +  "\"" +
                " AND " + COLUMN_USER_ID + " = \"" + bookmark.getUserId().trim() + "\";");
        return true;
    }


    boolean deleteCity(String uid){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery("select * from "+ DATABASE_WEATHER_TABLE +
                " WHERE " + COLUMN_USER_ID + " = \"" + uid + "\"", null);
        if(cursor!=null && cursor.moveToFirst()){
            db.execSQL("DELETE FROM " + DATABASE_WEATHER_TABLE + " WHERE " + COLUMN_USER_ID + " = \"" + uid +"\";");
        }
        db.close();
        return true;
    }

    boolean rememberCty(String city, String uid){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USER_ID, uid);
        contentValues.put(COLUMN_USER_CITY, city);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(DATABASE_WEATHER_TABLE, null, contentValues);
        db.close();
        return true;
    }

    Cursor getCity(String uid){
        SQLiteDatabase dbr = this.getReadableDatabase();
        Cursor res =  dbr.rawQuery("select * from "+ DATABASE_WEATHER_TABLE +
                " WHERE " + COLUMN_USER_ID + " = \"" + uid + "\"", null);
        return res;
    }

    boolean hasBeenBookmarked (Bookmark bookmark){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery("select * from "+ DATABASE_NEWS_TABLE +
                " WHERE " + COLUMN_AUTHOR + " = \"" +  bookmark.getAuthor() + "\"" +
                " AND " + COLUMN_TITLE + " = " + "\"" + bookmark.getTitle() + "\"" +
                " AND " + COLUMN_USER_ID + " = " + "\"" + bookmark.getUserId() + "\""+
                " order by " + COLUMN_ID + " desc limit 1", null);
        return cursor!=null && cursor.moveToFirst();
    }
}
