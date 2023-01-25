package com.easyway.pos.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easyway.pos.R;
import com.easyway.pos.adapters.SettingsAdapter;
import com.easyway.pos.data.SettingsItem;
import com.easyway.pos.helpers.DividerItemDecoration;
import com.easyway.pos.helpers.SettingsRecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Michael on 8/19/2015.
 */
public class SettingsActivity extends AppCompatActivity {
    public RecyclerView settingsList;
    public SettingsAdapter settingsAdapter;
    public Intent mIntent = null;
    public Toolbar toolbar;
    static SharedPreferences mSharedPrefs, prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializer();


    }

    /**
     * method initializer
     */
    public void initializer() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        settingsList = findViewById(R.id.settingsList);
        settingsAdapter = new SettingsAdapter(this, getData());
//        settingsList.setHasFixedSize(true);
        settingsList.setAdapter(settingsAdapter);
        settingsList.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, null);
        settingsList.addItemDecoration(itemDecoration);

        // this is the default; this call is actually only necessary with custom ItemAnimators
        settingsList.setItemAnimator(new DefaultItemAnimator());

        settingsList.addOnItemTouchListener(new SettingsRecyclerTouchListener(this, settingsList, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                switch (position) {
                    case 0:

                        mIntent = new Intent(SettingsActivity.this, SetupActivity.class);//Overall Settings
                        break;
                    case 1:
                        mIntent = new Intent(SettingsActivity.this, UserDetailsActivity.class);//user Details
                        break;
                    case 2:
                        mIntent = new Intent(SettingsActivity.this, FactoryDetailsActivity.class);//Factory Details
                        break;
                    case 3:
                        mIntent = new Intent(SettingsActivity.this, GradeDetailsActivity.class);//Routes Details
                        break;


                    default:
                        break;
                }

                if (mIntent != null) {
                    startActivity(mIntent);
                }

            }

            @Override
            public void onLongClick(View view, int position) {
                //add whatever you want like StartActivity.
            }
        }));
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        mIntent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mIntent);

    }

    public static List<SettingsItem> getData() {
        List<SettingsItem> data = new ArrayList<>();
        int[] icons = {R.mipmap.ic_settings_black_24dp, R.mipmap.ic_useradd, R.mipmap.ic_factory, R.mipmap.ic_produce};
        String[] titles = {"General Settings", "Users", "Factories", "Grades"};

        for (int i = 0; i < titles.length && i < icons.length; i++) {

            SettingsItem current = new SettingsItem();
            current.iconId = icons[i];
            current.title = titles[i];
            data.add(current);
        }

        return data;
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);

    }


}
