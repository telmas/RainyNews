package com.example.newsandweather;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class NewsFragment extends Fragment {

    String API_KEY = "2d5befad57814590ba27bef8424c305b";
//    String API_KEY = "50a5f46dd34b4d0c93cb19503e728e75";
    String NEWS_SOURCE = "bbc-news";
    String xml;
    String sourcesXml;
    ListView listNews;
    ProgressBar loader;
    String uid;
    HashMap<Integer, String> sourcesIDHashMap = new HashMap<>();
    HashMap<Integer, String> sourcesNamesHashMap = new HashMap<>();
    HashMap<Integer, String> sourcesCategoriesHashMap = new HashMap<>();

    static final String KEY_SOURCE_ID = "id";
    static final String KEY_SOURCE_NAME = "name";
        static final String KEY_SOURCE_CATEGORY= "category";

    ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
    static final String KEY_AUTHOR = "author";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_URL = "url";
    static final String KEY_URLTOIMAGE = "urlToImage";
    static final String KEY_PUBLISHEDAT = "publishedAt";
    private int counter = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_fragment, container, false);
    }


    @Override
    public void  onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        listNews = (ListView) getView().findViewById(R.id.listBookmarkID);
        loader = (ProgressBar) getView().findViewById(R.id.loader);
        listNews.setEmptyView(loader);

        uid = getArguments().getString("uid");
        if (Utilities.hasNetworkAccess(getContext())){
            DownloadNews newsTask = new DownloadNews();
            newsTask.execute();
        } else {
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }


    class DownloadNews extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dataList.clear();
        }
        protected String doInBackground(String... args) {
            sourcesXml = Utilities.excuteget("https://newsapi.org/v1/sources?&apiKey="+API_KEY);
            xml = Utilities.excuteget("https://newsapi.org/v1/articles?source="+NEWS_SOURCE.trim()+"&sortBy=top&apiKey="+API_KEY);
            return  xml;
        }
        @Override
        protected void onPostExecute(String xml) {
            if(counter < 1){
                loadSources();
            }

            if(xml.length()>10){
                try {
                    JSONObject jsonResponse = new JSONObject(xml);
                    JSONArray jsonArray = jsonResponse.optJSONArray("articles");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();
                            map.put(KEY_AUTHOR, jsonObject.optString(KEY_AUTHOR) == null ? "" : jsonObject.optString(KEY_AUTHOR));
                            map.put(KEY_TITLE, jsonObject.optString(KEY_TITLE)== null ? "" : jsonObject.optString(KEY_TITLE));
                            map.put(KEY_DESCRIPTION, jsonObject.optString(KEY_DESCRIPTION)== null ? "" : jsonObject.optString(KEY_DESCRIPTION));
                            map.put(KEY_URL, jsonObject.optString(KEY_URL)== null ? "" : jsonObject.optString(KEY_URL));
                            map.put(KEY_URLTOIMAGE, jsonObject.optString(KEY_URLTOIMAGE)== null ? "" : jsonObject.optString(KEY_URLTOIMAGE));
                            map.put(KEY_PUBLISHEDAT, jsonObject.optString(KEY_PUBLISHEDAT)== null ? "" : jsonObject.optString(KEY_PUBLISHEDAT));
                            dataList.add(map);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                }

                NewsListAdapter adapter = new NewsListAdapter(getActivity(), dataList, uid);

                listNews.setAdapter(adapter);

                listNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Intent i = new Intent(getContext(), NewsDetailsActivity.class);
                        i.putExtra("url", dataList.get(+position).get(KEY_URL));
                        startActivity(i);
                    }
                });

            }else{
                Toast.makeText(getContext(), "No news found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.addSubMenu(Menu.NONE, -1, Menu.NONE,"Sources");

        SubMenu sourcesMenu = menu.findItem(-1).getSubMenu();
        sourcesMenu.clear();
        sourcesMenu.addSubMenu(-2, -2, Menu.NONE, "Business");
        sourcesMenu.addSubMenu(-2, -3, Menu.NONE, "Entertainment");
        sourcesMenu.addSubMenu(-2, -4, Menu.NONE, "General");
        sourcesMenu.addSubMenu(-2, -5, Menu.NONE, "Science");
        sourcesMenu.addSubMenu(-2, -6, Menu.NONE, "Sports");
        sourcesMenu.addSubMenu(-2, -7, Menu.NONE, "Technology");
        SubMenu businessCategory = sourcesMenu.findItem(-2).getSubMenu();
        SubMenu entertainmentCategory = sourcesMenu.findItem(-3).getSubMenu();
        SubMenu generalCategory = sourcesMenu.findItem(-4).getSubMenu();
        SubMenu scienceCategory = sourcesMenu.findItem(-5).getSubMenu();
        SubMenu sportsCategory = sourcesMenu.findItem(-6).getSubMenu();
        SubMenu technologyCategory = sourcesMenu.findItem(-7).getSubMenu();

        menu.addSubMenu(Menu.NONE, -3, Menu.NONE,"Logout");

        for (int i = 0; i < sourcesNamesHashMap.size(); i++) {
            switch (sourcesCategoriesHashMap.get(i)) {
                case "business":
                    businessCategory.add(-1, i, Menu.NONE, sourcesNamesHashMap.get(i));
                    continue;
                case "entertainment":
                    entertainmentCategory.add(-1, i, Menu.NONE, sourcesNamesHashMap.get(i));
                    continue;
                case "general":
                    generalCategory.add(-1, i, Menu.NONE, sourcesNamesHashMap.get(i));
                    continue;
                case "science":
                    scienceCategory.add(-1, i, Menu.NONE, sourcesNamesHashMap.get(i));
                    continue;
                case "sports":
                    sportsCategory.add(-1, i, Menu.NONE, sourcesNamesHashMap.get(i));
                    continue;
                case "technology":
                    technologyCategory.add(-1, i, Menu.NONE, sourcesNamesHashMap.get(i));
            }
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == -3){
            FirebaseAuth.getInstance().signOut();
            getActivity().finish();
            startActivity(new Intent(getContext(), LogInActivity.class));
            return super.onOptionsItemSelected(item);
        }

        for (int i = 0; i < sourcesIDHashMap.size(); i++) {
            if (i == item.getItemId()) {
                NEWS_SOURCE = sourcesIDHashMap.get(item.getItemId());
                if (Utilities.hasNetworkAccess(getContext())){
                    try{
                        loader.setVisibility(View.VISIBLE);
                        DownloadNews newsTask = new DownloadNews();
                        newsTask.execute();
                    } catch (Exception e){
                        Toast.makeText(getContext(), "No News From This Source", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSources(){
        if(sourcesXml.length()>3){
            try {
                JSONObject jsonResponse = new JSONObject(sourcesXml);
                JSONArray jsonArraySources = jsonResponse.optJSONArray("sources");
                for (int i = 0; i < jsonArraySources.length(); i++) {
                    JSONObject jsonObject = jsonArraySources.getJSONObject(i);

                    sourcesIDHashMap.put(i, jsonObject.optString(KEY_SOURCE_ID));
                    sourcesNamesHashMap.put(i, jsonObject.optString(KEY_SOURCE_NAME));
                    sourcesCategoriesHashMap.put(i, jsonObject.optString(KEY_SOURCE_CATEGORY));
                }
                counter = 1;
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
