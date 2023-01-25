package com.easyway.pos.masters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.easyway.pos.R;
import com.easyway.pos.activities.LoginActivity;
import com.easyway.pos.data.DBHelper;
import com.easyway.pos.data.Database;
import com.easyway.pos.synctocloud.MasterApiRequest;
import com.easyway.pos.synctocloud.RestApiRequest;
import com.github.lzyzsd.circleprogress.ArcProgress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Michael on 21/09/2022.
 */
public class SyncMastersActivity extends AppCompatActivity {
    public Toolbar toolbar;

    DBHelper dbhelper;
    SQLiteDatabase db;

    SharedPreferences prefs;

    Button btnSync;
    String CoPrefix, TerminalID;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    String getGrades;
    int GradesResponse;


    private int progressStatus = 0;

    int count = 0;


    ArcProgress arcProgress;
    private TextView textView;
    String _URL, _TOKEN;

    String RecordIndex, FGRef, FGDescription;


    String Server = "";
    String dateFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_setup_masters);

        initializer();
        setupToolbar();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("MASTERS : " + prefs.getString("terminalID", ""));

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void initializer() {

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();

        CoPrefix = prefs.getString("company_prefix", "");
        TerminalID = prefs.getString("terminalID", "");

        dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        if (!isInternetOn()) {
            createNetErrorDialog();
            return;
        }
        if (prefs.getString("internetAccessModes", null).equals("WF")) {
            if (prefs.getString("coPort", "").equals("")) {
                _URL = prefs.getString("portalURL", null) + "/" +
                        prefs.getString("coApp", null);
            } else {
                _URL = prefs.getString("portalURL", "") + ":"
                        + prefs.getString("coPort", "") + "/" +
                        prefs.getString("coApp", "");

            }


        } else {
            if (prefs.getString("coPort", "").equals("")) {
                _URL = prefs.getString("mdportalURL", null) + "/" +
                        prefs.getString("coApp", null);
            } else {
                _URL = prefs.getString("mdportalURL", "") + ":"
                        + prefs.getString("coPort", "") + "/" +
                        prefs.getString("coApp", "");

            }
        }
        Log.i("_URL", _URL);

        _TOKEN = prefs.getString("token", null);
        if (_TOKEN == null || _TOKEN.equals("")) {
            _TOKEN = new RestApiRequest(getApplicationContext()).getToken();

        } else {
            long token_hours = new RestApiRequest(getApplicationContext()).token_hours();
            if (token_hours >= 23) {
                _TOKEN = new RestApiRequest(getApplicationContext()).getToken();

            }


        }


        arcProgress = findViewById(R.id.arc_progress);
        textView = findViewById(R.id.textView1);


        btnSync = findViewById(R.id.btnSync);
        btnSync.setOnClickListener(v -> SetupMasters());


    }

    @SuppressLint("SetTextI18n")
    public void SetupMasters() {
        arcProgress.setProgress(0);
        btnSync.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);


        executor.execute(() -> {

            // do background work here
            getGrades = new MasterApiRequest(getApplicationContext()).getGrades();
            GradesResponse = prefs.getInt("gradesresponse", 0);

            if (GradesResponse == 200) {
                try {
                    Server = "";

                    db.delete(Database.GRADES_TABLE_NAME, null, null);
                    db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.GRADES_TABLE_NAME + "'");

                    @SuppressLint("Recycle") Cursor routes = db.query(true, Database.GRADES_TABLE_NAME, null, null, null, null, null, null, null, null);


                    if (routes.getCount() == 0) {
                        String DefaultRoute = "INSERT INTO " + Database.GRADES_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.GRADE_CODE + ", "
                                + Database.GRADE_NAME + ") Values ('0', 'Select ...','Select ...')";
                        db.execSQL(DefaultRoute);
                    }

                    JSONArray arrayKnownAs = new JSONArray(getGrades);
                    if (arrayKnownAs.length() > 0) {
                        // Do something with object.
                        for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {

                            JSONObject obj = arrayKnownAs.getJSONObject(i);
                            RecordIndex = obj.getString("RecordIndex");
                            FGRef = obj.getString("FGRef");
                            FGDescription = obj.getString("FGDescription");

                            Log.i("RecordIndex", RecordIndex);
                            Log.i("FGRef", FGRef);

                            Cursor checkRoute = dbhelper.CheckGrade(FGRef);
                            if (!(checkRoute.getCount() > 0)) {
                                dbhelper.AddGrade(FGRef, FGDescription, RecordIndex);
                            }

                            count = arrayKnownAs.length();
                            progressStatus++;
                            runOnUiThread(() -> {
                                arcProgress.setProgress(progressStatus);
                                arcProgress.setMax(count);
                                arcProgress.setBottomText("ADDING GRADES...");
                                textView.setText(progressStatus + "/" + count + " Records");
                            });
                        }

                    } else {
                        RecordIndex = "-1";
                    }

                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());
                    Server = "-8080";
                    Log.e("Server Response", e.toString());
                    e.printStackTrace();

                }

            } else {
                Server = "-8080";
            }


            handler.post(() -> {
                // do UI changes after background work here
                if (Server.equals("-8080")) {

                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    @SuppressLint("InflateParams") View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Server Not Found\nFailed to Connect to " + _URL);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    btnSync.setVisibility(View.VISIBLE);
                    return;
                }
                if (GradesResponse == 200) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    @SuppressLint("InflateParams") View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText(count + " Records Added Successfully");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    arcProgress.setProgress(0);
                    btnSync.setVisibility(View.VISIBLE);
                    arcProgress.setBottomText("REFRESHING ...");
                    textView.setVisibility(View.GONE);
                    progressStatus = 0;
                    count = 0;
                }

            });
        });
    }


    public boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec = (ConnectivityManager) getBaseContext().getSystemService(CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {


            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {


            return false;
        }
        return false;
    }

    protected void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SyncMastersActivity.this);
        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>You need internet connection to proceed. Please turn on mobile network or Wi-Fi in Settings.</font>"))
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setNegativeButton("Settings",
                        (dialog, id) -> {
                            if (prefs.getString("internetAccessModes", "WF").equals("WF")) {

                                Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                startActivity(i);
                            } else {
                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(i);

                            }


                        }
                )
                .setPositiveButton("Cancel",
                        (dialog, id) -> dialog.dismiss()
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    @SuppressLint("SetTextI18n")
    public void onBackPressed() {
        //Display alert message when back button has been pressed

        if (count > 0) {
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("You cannot close window while syncing masters !!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.BOTTOM, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }

        finish();
        Intent mIntent = new Intent(SyncMastersActivity.this, LoginActivity.class);
        startActivity(mIntent);
    }

}