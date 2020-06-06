package com.example.newsandweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


class BookmarkListAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private String uid;

    public BookmarkListAdapter(Activity a, ArrayList<HashMap<String, String>> d, String uid) {
        activity = a;
        data=d;
        this.uid = uid;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        BookmarkListViewHolder holder;
        if (convertView == null) {
            holder = new BookmarkListViewHolder();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.bookmark_list_row, parent, false);
            holder.thumbnailImage = (ImageView) convertView.findViewById(R.id.galleryImage);
            holder.author = (TextView) convertView.findViewById(R.id.author);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.description = (TextView) convertView.findViewById(R.id.sdetails);
            holder.publishTime = (TextView) convertView.findViewById(R.id.time);
            holder.deleteButton = (Button)convertView.findViewById(R.id.buttonBookmark);
            convertView.setTag(holder);
        } else {
            holder = (BookmarkListViewHolder) convertView.getTag();
        }
        holder.thumbnailImage.setId(position);
        holder.author.setId(position);
        holder.title.setId(position);
        holder.description.setId(position);
        holder.publishTime.setId(position);
        holder.deleteButton.setId(position);

        HashMap<String, String> bookmarks;
        bookmarks = data.get(position);

        try{
            holder.author.setText(bookmarks.get(NewsFragment.KEY_AUTHOR));
            holder.title.setText(bookmarks.get(NewsFragment.KEY_TITLE));
            holder.publishTime.setText(bookmarks.get(NewsFragment.KEY_PUBLISHEDAT));
            holder.description.setText(bookmarks.get(NewsFragment.KEY_DESCRIPTION));


            if(Objects.requireNonNull(bookmarks.get(NewsFragment.KEY_URLTOIMAGE)).length() < 5){
                holder.thumbnailImage.setVisibility(View.GONE);
            }else{
                Picasso.with(activity)
                        .load(bookmarks.get(NewsFragment.KEY_URLTOIMAGE))
                        .resize(300, 200)
                        .into(holder.thumbnailImage);
            }

            final BookmarkListViewHolder finalHolder = holder;
            final HashMap<String, String> finalBookmarks = bookmarks;
            final View finalView= convertView;
            final int positionToRemove = position;
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                    adb.setTitle("Delete?");
                    adb.setMessage("Are you sure you want to delete bookmark?");
                    adb.setNegativeButton("Cancel", null);
                    adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            data.remove(positionToRemove);
                            DBHelper db = new DBHelper(finalView.getContext());
                            Bookmark bookmark = new Bookmark();
                            bookmark.setAuthor(finalHolder.author.getText().toString());
                            bookmark.setDescription(finalHolder.description.getText().toString());
                            bookmark.setImageUrl(finalBookmarks.get(NewsFragment.KEY_URLTOIMAGE));
                            bookmark.setUrl(finalBookmarks.get(NewsFragment.KEY_URL));
                            bookmark.setPublicationTime(finalHolder.publishTime.getText().toString());
                            bookmark.setUserId(uid);
                            bookmark.setTitle(finalHolder.title.getText().toString());
                            if(db.deleteBookmark(bookmark)){
                                Toast.makeText(finalView.getContext(),"Deleted", Toast.LENGTH_LONG).show();
                                notifyDataSetChanged();
                            }
                        }});
                    adb.show();
                }
            });

        }catch(Exception e) {}
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

class BookmarkListViewHolder {
    ImageView thumbnailImage;
    TextView author, title, description, publishTime;
    Button deleteButton;
}