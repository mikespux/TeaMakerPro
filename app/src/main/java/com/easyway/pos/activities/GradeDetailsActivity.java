package com.easyway.pos.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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
public class GradeDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddGrade, btn_svGrade;
    ListView listGrades;
    EditText grade_code, grade_name;
    String s_mcrcode, s_mcrname;
    String accountId, GradeCode;
    TextView textAccountId;
    Boolean success = true;

    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());
    String Server = "";
    private int progressStatus = 0;
    int count = 0;
    String getGrades;
    int GradesResponse;
    String RecordIndex, FGRef, FGDescription;


    SharedPreferences prefs;
    SQLiteDatabase db;

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
        getSupportActionBar().setTitle(R.string.title_grade);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void initializer() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();

        btAddGrade = findViewById(R.id.btAddUser);
        btAddGrade.setVisibility(View.GONE);
        btAddGrade.setOnClickListener(v -> showAddUserDialog());
        listGrades = this.findViewById(R.id.lvUsers);
        listGrades.setOnItemClickListener((parent, selectedView, arg2, arg3) -> {
            textAccountId = selectedView.findViewById(R.id.txtAccountId);
            Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
            // Intent intent = new Intent(Activity_ListStock.this, UpdateStock.class);
            // intent.putExtra("accountid", textAccountId.getText().toString());
            // startActivity(intent);
            showUpdateUserDialog();
        });


    }

    public void showAddUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_grade, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Grades");
        grade_code = dialogView.findViewById(R.id.grade_code);
        grade_name = dialogView.findViewById(R.id.grade_name);


        btn_svGrade = dialogView.findViewById(R.id.btn_svGrade);
        btn_svGrade.setOnClickListener(v -> {

            try {
                s_mcrcode = grade_code.getText().toString();
                s_mcrname = grade_name.getText().toString();


                if (s_mcrcode.equals("") || s_mcrname.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                    return;
                }
                Cursor checkGrade = dbhelper.CheckGrade(s_mcrcode);
                //Check for duplicate shed
                if (checkGrade.getCount() > 0) {
                    Toast.makeText(getApplicationContext(), "Grade already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                dbhelper.AddGrade(s_mcrcode, s_mcrname, "0");
                if (success) {


                    Toast.makeText(GradeDetailsActivity.this, "Grade Saved successfully!!", Toast.LENGTH_LONG).show();

                    grade_code.setText("");
                    grade_name.setText("");

                }
            } catch (Exception ignored) {


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
        final View dialogView = inflater.inflate(R.layout.dialog_add_grade, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Grades");
        accountId = textAccountId.getText().toString();

        grade_code = dialogView.findViewById(R.id.grade_code);
        grade_code.setEnabled(false);
        grade_name = dialogView.findViewById(R.id.grade_name);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.GRADES_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            grade_code.setText(account.getString(account
                    .getColumnIndex(Database.GRADE_CODE)));
            grade_name.setText(account.getString(account
                    .getColumnIndex(Database.GRADE_NAME)));


        }
        account.close();
        db.close();
        dbhelper.close();


        btn_svGrade = dialogView.findViewById(R.id.btn_svGrade);
        btn_svGrade.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", (dialog, whichButton) -> {
            //do something with edt.getText().toString();
            deleteGrade();

        });
        dialogBuilder.setNegativeButton("Update", (dialog, whichButton) -> {
            //pass
            updateGrade();
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
                LoadGrades();

                return true;
            case R.id.action_clear:

                db.delete(Database.GRADES_TABLE_NAME, null, null);
                db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.GRADES_TABLE_NAME + "'");
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

        AlertDialog.Builder builder = new AlertDialog.Builder(GradeDetailsActivity.this);
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

    public void LoadGrades() {
        progressDialog = ProgressDialog.show(GradeDetailsActivity.this,
                "Loading Grades",
                "Please Wait.. ");


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
                                progressDialog.setTitle("REFRESHING GRADES...");
                                progressDialog.setMessage(progressStatus + "/" + count + " Records");
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
                    progressDialog.dismiss();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    @SuppressLint("InflateParams") View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Server Not Found\nFailed to Connect");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();

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
                    progressDialog.dismiss();
                    progressStatus = 0;
                    count = 0;
                    getdata();
                }

            });
        });
    }

    public void updateGrade() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put(Database.GRADE_CODE, grade_code.getText().toString());
            values.put(Database.GRADE_NAME, grade_name.getText().toString());


            long rows = db.update(Database.GRADES_TABLE_NAME, values,
                    "_id = ?", new String[]{accountId});

            db.close();
            if (rows > 0) {
                Toast.makeText(this, "Updated Grade Successfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Sorry! Could not update Grade!",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteGrade() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Grade?")
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
            Cursor c = db.rawQuery("select grade_code from grades where _id= '" + accountId + "' ", null);
            if (c != null) {
                c.moveToFirst();
                GradeCode = c.getString(c.getColumnIndex("grade_code"));

            }
            c.close();

            int rows = db.delete(Database.GRADES_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if (rows == 1) {
                Toast.makeText(this, "Grade Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            } else {
                Toast.makeText(this, "Could not delete Grade!", Toast.LENGTH_LONG).show();
            }


        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void getdata() {

        try {
            int ROWID = 0;
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.GRADES_TABLE_NAME, null, Database.ROW_ID + ">'" + ROWID + "'", null, null, null, null, null, null);

            String[] from = {Database.ROW_ID, Database.GRADE_CODE, Database.GRADE_NAME};
            int[] to = {R.id.txtAccountId, R.id.txtUserName, R.id.txtUserType};

            @SuppressWarnings("deprecation")
            SimpleCursorAdapter ca = new SimpleCursorAdapter(this, R.layout.userlist, accounts, from, to);

            ListView listusers = this.findViewById(R.id.lvUsers);
            listusers.setAdapter(ca);
            // dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
