package com.easyway.pos.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.easyway.pos.R;
import com.easyway.pos.data.DBHelper;
import com.easyway.pos.data.Database;
import com.easyway.pos.synctocloud.MasterApiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Michael on 30/06/2016.
 */
public class FactoryDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    Button btAddFactory, btn_svFactory;
    EditText fry_prefix, fry_name;
    String s_fryprefix, s_fryname, s_recordindex = "";
    ListView listFactories;
    DBHelper dbhelper;
    String accountId;
    TextView textAccountId;
    Boolean success = true;
    SharedPreferences mSharedPrefs, prefs;
    String FRecordIndex, FryPrefix, FryTitle, FryCapacity;

    String restApiResponse, CRecordIndex;
    int response;
    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_factory);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void initializer() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dbhelper = new DBHelper(getApplicationContext());
        btAddFactory = findViewById(R.id.btAddUser);
        btAddFactory.setVisibility(View.GONE);
        btAddFactory.setOnClickListener(v -> showAddUserDialog());
        listFactories = this.findViewById(R.id.lvUsers);
        listFactories.setOnItemClickListener((parent, selectedView, arg2, arg3) -> {
            textAccountId = selectedView.findViewById(R.id.txtAccountId);
            Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
            showUpdateUserDialog();
        });


    }

    public void showAddUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_factory, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Factories");
        fry_prefix = dialogView.findViewById(R.id.fry_prefix);
        fry_name = dialogView.findViewById(R.id.fry_name);


        btn_svFactory = dialogView.findViewById(R.id.btn_svFactory);
        btn_svFactory.setOnClickListener(v -> {

            try {
                s_fryprefix = fry_prefix.getText().toString();
                s_fryname = fry_name.getText().toString();


                if (s_fryprefix.equals("") || s_fryname.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                    return;
                }
                Cursor checkFactory = dbhelper.CheckFactory(s_fryprefix);
                //Check for duplicate id number
                if (checkFactory.getCount() > 0) {
                    Toast.makeText(getApplicationContext(), "Factory already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                dbhelper.AddFactories(s_fryprefix, s_fryname, s_recordindex);
                if (success) {


                    Toast.makeText(FactoryDetailsActivity.this, "Factory Saved successfully!!", Toast.LENGTH_LONG).show();

                    fry_prefix.setText("");
                    fry_name.setText("");

                }
            } catch (Exception e) {

                Toast.makeText(FactoryDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();


            }

        });

        dialogBuilder.setPositiveButton("Done", (dialog, whichButton) -> {
            //do something with edt.getText().toString();
            getdata();

        });
        dialogBuilder.setNegativeButton("Cancel", (dialog, whichButton) -> {
            //pass
            getdata();
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showUpdateUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_factory, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Factories");
        accountId = textAccountId.getText().toString();

        fry_prefix = dialogView.findViewById(R.id.fry_prefix);
        fry_name = dialogView.findViewById(R.id.fry_name);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.FACTORY_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            fry_prefix.setText(account.getString(account
                    .getColumnIndex(Database.FRY_PREFIX)));
            fry_name.setText(account.getString(account
                    .getColumnIndex(Database.FRY_TITLE)));


        }
        account.close();
        db.close();
        dbhelper.close();


        btn_svFactory = dialogView.findViewById(R.id.btn_svFactory);
        btn_svFactory.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", (dialog, whichButton) -> {
            //do something with edt.getText().toString();
            deleteFactory();

        });
        dialogBuilder.setNegativeButton("Update", (dialog, whichButton) -> {
            //pass
            updateFactory();
            getdata();


        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void onStart() {
        super.onStart();
        getdata();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sync, menu);


        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {

            case R.id.action_sync:
                if (!isInternetOn()) {
                    createNetErrorDialog();
                    return true;
                }
                LoadFactories();

                return true;
            case R.id.action_clear:

                SQLiteDatabase db = dbhelper.getWritableDatabase();
                db.delete(Database.FACTORY_TABLE_NAME, null, null);
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FACTORY_TABLE_NAME + "'");
                getdata();

                return true;
        }

        return super.onOptionsItemSelected(item);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(FactoryDetailsActivity.this);
        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>You need internet connection to proceed. Please turn on mobile network or Wi-Fi in Settings.</font>"))
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setNegativeButton("Settings",
                        (dialog, id) -> {
                            if (mSharedPrefs.getString("internetAccessModes", "WF").equals("WF")) {

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

    public void LoadFactories() {
        progressDialog = ProgressDialog.show(FactoryDetailsActivity.this,
                "Loading Factories",
                "Please Wait.. ");
        executor.execute(() -> {
            //Background work here

            CRecordIndex = prefs.getString("CRecordIndex", null);
            //  Log.e(TAG, "Response from url: " + jsonStr);
            restApiResponse = new MasterApiRequest(getApplicationContext()).getFactories(CRecordIndex);
            response = prefs.getInt("factoriesresponse", 0);
            if (response == 200) {
                try {


                    SQLiteDatabase db = dbhelper.getWritableDatabase();
                    Cursor factories = db.query(true, Database.FACTORY_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (factories.getCount() == 0) {
                        String DefaultFactory = "INSERT INTO " + Database.FACTORY_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.FRY_PREFIX + ", "
                                + Database.FRY_TITLE + ", "
                                + Database.FRY_ClOUDID + ") Values ('0','0', 'Select ...','0')";
                        db.execSQL(DefaultFactory);
                    }
                    factories.close();
                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);
                        FRecordIndex = obj.getString("RecordIndex");
                        FryPrefix = obj.getString("FryPrefix");
                        FryTitle = obj.getString("FryTitle");
                        FryCapacity = obj.getString("FryCapacity");
                        Log.i("FRecordIndex", FRecordIndex);
                        Log.i("FryPrefix", FryPrefix);
                        Cursor checkFactory = dbhelper.CheckFactory(FryPrefix);
                        //Check for duplicate FryPrefix
                        if (!(checkFactory.getCount() > 0)) {
                            dbhelper.AddFactories(FryPrefix, FryTitle, FRecordIndex);
                        }


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }

            }

            handler.post(() -> {
                //UI Thread work here

                progressDialog.dismiss();
                getdata();
            });
        });

    }

    public void updateFactory() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.FRY_PREFIX, fry_prefix.getText().toString());
            values.put(Database.FRY_TITLE, fry_name.getText().toString());


            long rows = db.update(Database.FACTORY_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            db.close();
            if (rows > 0) {
                Toast.makeText(this, "Updated Factory Successfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Factory!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteFactory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this factory?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> deleteCurrentAccount())
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void deleteCurrentAccount() {
        try {
            DBHelper dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            int rows = db.delete(Database.FACTORY_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if (rows == 1) {
                Toast.makeText(this, "Factory Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            } else
                Toast.makeText(this, "Could not delete factory!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void getdata() {

        try {
            int ROWID = 0;
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.FACTORY_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);

            String[] from = {Database.ROW_ID, Database.FRY_PREFIX, Database.FRY_TITLE};
            int[] to = {R.id.txtAccountId, R.id.txtUserName, R.id.txtUserType};

            @SuppressWarnings("deprecation")
            SimpleCursorAdapter ca = new SimpleCursorAdapter(this, R.layout.userlist, accounts, from, to);

            ListView listusers = this.findViewById(R.id.lvUsers);
            listusers.setAdapter(ca);
            dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
