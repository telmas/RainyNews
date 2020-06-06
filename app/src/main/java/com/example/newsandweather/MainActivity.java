package com.example.newsandweather;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().hasExtra("com.example.newsandweather")) {
            if (Objects.requireNonNull(getIntent().getExtras()).getString("com.example.newsandweather") != null) {
                setUid(getIntent().getExtras().getString("com.example.newsandweather"));
            }
        }

        BottomNavigationView navigationView = findViewById(R.id.bottom_nav);

        final NewsFragment newsFragment = new NewsFragment();
        final WeatherFragment weatherFragment = new WeatherFragment();
        final BookmarkFragment bookmarkFragment = new BookmarkFragment();

        final Bundle args = new Bundle();
        args.putString("uid", getUid());

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == R.id.newsTabID){
                    newsFragment.setArguments(args);
                    setFragment(newsFragment);
                    return true;
                } else if (id == R.id.weatherTabID){
                    weatherFragment.setArguments(args);
                    setFragment(weatherFragment);
                    return true;
                } else if (id == R.id.bookmarksTabID) {
                    bookmarkFragment.setArguments(args);
                    setFragment(bookmarkFragment);
                    return true;
                }
                return false;
            }
        });
        navigationView.setSelectedItemId(R.id.newsTabID);
    }

    private void setFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
