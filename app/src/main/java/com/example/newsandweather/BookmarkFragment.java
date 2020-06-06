package com.example.newsandweather;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;

public class BookmarkFragment extends Fragment {

    ListView listBookmarks;
    ProgressBar loader;
    String uid;
    DBHelper dataBase;

    ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
    static final String KEY_ID= "_id";
    static final String KEY_USER_ID= "userid";
    static final String KEY_AUTHOR = "author";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_URL = "url";
    static final String KEY_URLTOIMAGE = "urlToImage";
    static final String KEY_PUBLISHEDAT = "publishedAt";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmark_fragment, container, false);
    }

    @Override
    public void  onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listBookmarks = (ListView) getView().findViewById(R.id.listBookmarkID);
        loader = (ProgressBar) getView().findViewById(R.id.loader);
        listBookmarks.setEmptyView(loader);

        uid = getArguments().getString("uid");
        LoadNewsBookmarks bookmarks = new LoadNewsBookmarks();
        bookmarks.execute();
    }

    class LoadNewsBookmarks extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dataList.clear();
            dataBase = new DBHelper(getActivity());
            Cursor upcoming = dataBase.getBookmarks();
            loadDataList(upcoming, dataList);
        }

        @Override
        protected String doInBackground(String... args) {
            String xml = "";
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            loadListView(listBookmarks,dataList);
            loader.setVisibility(View.GONE);
        }
    }

    public void populateData() {
        loader.setVisibility(View.VISIBLE);
        LoadNewsBookmarks loadNewsBookmarks = new LoadNewsBookmarks();
        loadNewsBookmarks.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateData();
    }

    public void loadDataList(Cursor cursor, ArrayList<HashMap<String, String>> dataList){
        if(cursor!=null ) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {

                HashMap<String, String> mapBookmark = new HashMap<String, String>();
                mapBookmark.put(KEY_ID, cursor.getString(0));
                mapBookmark.put(KEY_USER_ID, cursor.getString(1));
                mapBookmark.put(KEY_AUTHOR, cursor.getString(2));
                mapBookmark.put(KEY_TITLE, cursor.getString(3));
                mapBookmark.put(KEY_DESCRIPTION, cursor.getString(4));
                mapBookmark.put(KEY_URL, cursor.getString(5));
                mapBookmark.put(KEY_URLTOIMAGE, cursor.getString(6));
                mapBookmark.put(KEY_PUBLISHEDAT, cursor.getString(7));

                dataList.add(mapBookmark);
                cursor.moveToNext();
            }
        }
    }

    public void loadListView(ListView listView, final ArrayList<HashMap<String, String>> dataList){
        BookmarkListAdapter bookmarkListAdapter = new BookmarkListAdapter(getActivity(), dataList, uid);
        listView.setAdapter(bookmarkListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(getActivity(), NewsDetailsActivity.class);
                i.putExtra("url", dataList.get(+position).get(KEY_URL));
                startActivity(i);
            }
        });
    }
}
