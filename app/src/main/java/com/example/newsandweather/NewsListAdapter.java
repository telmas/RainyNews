package com.example.newsandweather;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


class NewsListAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private String uid;

    private final boolean[] mHighlightedPositions = new boolean[10];
    private int var = 0;

    public NewsListAdapter(Activity a, ArrayList<HashMap<String, String>> d, String uid) {
        activity = a;
        data = d;
        this.uid = uid;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        NewsListViewHolder holder;
        if (convertView == null) {
            holder = new NewsListViewHolder();

            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.news_list_row, parent, false);
            holder.thumbnailImage = (ImageView) convertView.findViewById(R.id.galleryImage);
            holder.author = (TextView) convertView.findViewById(R.id.author);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.description = (TextView) convertView.findViewById(R.id.sdetails);
            holder.publishTime = (TextView) convertView.findViewById(R.id.time);
            holder.saveButton = (Button) convertView.findViewById(R.id.buttonBookmark);

            convertView.setTag(holder);
        } else {
            holder = (NewsListViewHolder) convertView.getTag();
        }
        holder.thumbnailImage.setId(position);
        holder.author.setId(position);
        holder.title.setId(position);
        holder.description.setId(position);
        holder.publishTime.setId(position);
        holder.saveButton.setId(position);


        HashMap<String, String> articles;

        articles = data.get(position);

        try {

            holder.author.setText(Objects.equals(articles.get(NewsFragment.KEY_AUTHOR), "null") ? "" : articles.get(NewsFragment.KEY_AUTHOR));
            holder.title.setText(Objects.equals(articles.get(NewsFragment.KEY_TITLE), "null") ? "" : articles.get(NewsFragment.KEY_TITLE));
            holder.publishTime.setText(Objects.equals(articles.get(NewsFragment.KEY_PUBLISHEDAT), "null") ? "" : articles.get(NewsFragment.KEY_PUBLISHEDAT));
            holder.description.setText(Objects.equals(articles.get(NewsFragment.KEY_DESCRIPTION), "null") ? "" : articles.get(NewsFragment.KEY_DESCRIPTION));

            if (articles.get(NewsFragment.KEY_URLTOIMAGE).length() < 5) {
                holder.thumbnailImage.setVisibility(View.GONE);
            } else {
                Picasso.with(activity)
                        .load(articles.get(NewsFragment.KEY_URLTOIMAGE))
                        .resize(300, 200)
                        .into(holder.thumbnailImage);
            }

            final Bookmark bookmark = new Bookmark();
            final DBHelper db = new DBHelper(convertView.getContext());
            bookmark.setAuthor(holder.author.getText().toString());
            bookmark.setDescription(holder.description.getText().toString());
            bookmark.setImageUrl(articles.get(NewsFragment.KEY_URLTOIMAGE));
            bookmark.setUrl(articles.get(NewsFragment.KEY_URL));
            bookmark.setPublicationTime(holder.publishTime.getText().toString());
            bookmark.setUserId(uid);
            bookmark.setTitle(holder.title.getText().toString());
            holder.saveButton.setTag(position);
            if(mHighlightedPositions[position] || db.hasBeenBookmarked(bookmark)) {
                holder.saveButton.setBackgroundResource(R.drawable.inverted_round_button);
                holder.saveButton.setTextColor(convertView.getResources().getColor(R.color.colorAccent));
            } else {
                holder.saveButton.setBackgroundResource(R.drawable.round_button);
                holder.saveButton.setTextColor(convertView.getResources().getColor(R.color.white));
            }

            holder.saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int)v.getTag();
                    LinearLayout layout = (LinearLayout)v.getParent();
                    Button button = (Button)layout.getChildAt(1);
                    if(mHighlightedPositions[position] && var == 0) {
                        button.setBackgroundResource(R.drawable.round_button);
                        button.setTextColor(v.getResources().getColor(R.color.white));
                        mHighlightedPositions[position] = false;
                    } else {
                        button.setBackgroundResource(R.drawable.inverted_round_button);
                        button.setTextColor(v.getResources().getColor(R.color.colorAccent));
                        mHighlightedPositions[position] = true;
                        var = 1;
                    }
                    if (db.hasBeenBookmarked(bookmark)) {
                        Toast.makeText(v.getContext(), "Has already been bookmarked", Toast.LENGTH_SHORT).show();
                    } else if (db.bookmarkNews(bookmark)) {
                        Toast.makeText(v.getContext(), "Bookmarked", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (Exception e) {
        }
        return convertView;
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
}


class NewsListViewHolder {
    ImageView thumbnailImage;
    TextView author, title, description, publishTime;
    Button saveButton;
}