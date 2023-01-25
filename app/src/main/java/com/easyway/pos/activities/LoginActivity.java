package com.easyway.pos.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.easyway.pos.R;
import com.easyway.pos.data.DBHelper;
import com.easyway.pos.data.Database;
import com.easyway.pos.synctocloud.RestApiRequest;
import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * Created by Michael on 9/24/2015.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    static SharedPreferences mSharedPrefs, prefs;
    private DevicePolicyManager devicePolicyManager = null;
    private ComponentName demoDeviceAdmin = null;
    private int ACTIVATION_REQUEST;


    private TextInputLayout usernameWrapper, passwordWrapper;
    private ProgressBar mProgress;
    private Button signInBtn;


    DBHelper dbhelper;

    int count;
    int usercount;

    File FolderPath1, FolderPath2, FolderPath3;
    File root;
    private final String TAG = "App";

    String _URL;
    ProgressDialog progressDialog;
    Executor executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    String _TOKEN, restApiResponse;
    String access_token, token_type, expires_in, userName, issued, expires;
    String error, desc;
    String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_login);


        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        demoDeviceAdmin = new ComponentName(this, DeviceAdmin.class);
        Log.e("DeviceAdminActive==", "" + demoDeviceAdmin);


        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);// adds new device administrator
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, demoDeviceAdmin);//ComponentName of the administrator component.
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Disable app");//dditional explanation
        startActivityForResult(intent, ACTIVATION_REQUEST);

        if (!devicePolicyManager.isAdminActive(demoDeviceAdmin)) {
            Toast.makeText(this, getString(R.string.not_device_admin), Toast.LENGTH_SHORT).show();
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (devicePolicyManager.isDeviceOwnerApp(getPackageName())) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    devicePolicyManager.setLockTaskPackages(demoDeviceAdmin, new String[]{getPackageName()});
                }

            } else {
                //  Toast.makeText(this, getString(R.string.not_device_owner), Toast.LENGTH_SHORT).show();
            }
        }

        initView();
        setupProgressBar();

        requestStoragePermission();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

            } else {
                CreateFolders();

            }
        } else {
            CreateFolders();
        }

    }

    private void requestStoragePermission() {
        Dexter.withActivity(this)
                .withPermissions(

                        android.Manifest.permission.INTERNET,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.READ_PHONE_STATE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {

                        }
                        // check for permanent denial of any permission

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    public void CreateFolders() {

        root = Environment.getExternalStorageDirectory();
        FolderPath1 = new File(new File(root, "Easyweigh"), "Masters");

        if (!FolderPath1.exists()) {
            if (!FolderPath1.mkdirs()) {
                Log.d("App", "failed to create directory");
                Toast.makeText(LoginActivity.this, "failed to create Masters directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        FolderPath2 = new File(root, "/Easyweigh/Exports");

        if (!FolderPath2.exists()) {
            if (!FolderPath2.mkdirs()) {
                Log.d("App", "failed to create directory");
                Toast.makeText(LoginActivity.this, "failed to create Exports directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        FolderPath3 = new File(root, "/Easyweigh/Apks");

        if (!FolderPath3.exists()) {
            if (!FolderPath3.mkdirs()) {
                Log.d("App", "failed to create directory");
                Toast.makeText(LoginActivity.this, "failed to create Apks directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //Create your Directory here
            CreateFolders();
        }
    }

    @Override
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        super.onBackPressed();
        backButtonHandler();
    }

    public void backButtonHandler() {
        Intent intent = new Intent(android.content.Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(android.content.Intent.CATEGORY_HOME);
        finish();
        startActivity(intent);
    }

    public void setupProgressBar() {
        mProgress = findViewById(R.id.progress_bar);
        mProgress.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF5252"),
                PorterDuff.Mode.SRC_IN);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initView() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (mSharedPrefs.getString("internetAccessModes", "").equals("WF")) {
            if (mSharedPrefs.getString("coPort", "").equals("")) {
                _URL = mSharedPrefs.getString("portalURL", null) + "/" +
                        mSharedPrefs.getString("coApp", null);
            } else {
                _URL = mSharedPrefs.getString("portalURL", "") + ":"
                        + mSharedPrefs.getString("coPort", "") + "/" +
                        mSharedPrefs.getString("coApp", "");

            }


        } else {
            if (mSharedPrefs.getString("coPort", "").equals("")) {
                _URL = mSharedPrefs.getString("mdportalURL", null) + "/" +
                        mSharedPrefs.getString("coApp", null);
            } else {
                _URL = mSharedPrefs.getString("mdportalURL", "") + ":"
                        + mSharedPrefs.getString("coPort", "") + "/" +
                        mSharedPrefs.getString("coApp", "");

            }
        }

        signInBtn = findViewById(R.id.signInBtn);
        signInBtn.setVisibility(View.VISIBLE);

        usernameWrapper = findViewById(R.id.usernameWrapper);
        passwordWrapper = findViewById(R.id.passwordWrapper);
        signInBtn.setOnClickListener(this);

        dbhelper = new DBHelper(LoginActivity.this);

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor accounts = db.query(true, Database.GRADES_TABLE_NAME, null, null, null, null, null, null, null, null);
        count = accounts.getCount();

        Cursor users = db.query(true, Database.OPERATORSMASTER_TABLE_NAME, null, null, null, null, null, null, null, null);
        usercount = users.getCount();

        if (usercount == 0) {

            String DefaultUsers = "INSERT INTO " + Database.OPERATORSMASTER_TABLE_NAME + " ("
                    + Database.USERFULLNAME + ", "
                    + Database.USERNAME + ", "
                    + Database.USERPWD + ", "
                    + Database.ACCESSLEVEL + ") Values ('OCTAGON', 'ODS', '1234', '1')";

            db.execSQL(DefaultUsers);

        }


    }


    @Override
    public void onClick(final View view) {
        if (view.getId() == R.id.signInBtn) {
            userName = usernameWrapper.getEditText().getText().toString().trim();
            userPassword = passwordWrapper.getEditText().getText().toString().trim();
            if (userName.length() <= 0) {
                usernameWrapper.setError("Enter a valid Username");
            } else if (userPassword.length() < 3) {
                passwordWrapper.setError("Invalid Password");
            } else {
                usernameWrapper.setErrorEnabled(false);
                passwordWrapper.setErrorEnabled(false);

                if (dbhelper.UserLogin(userName, userPassword)) {
                    // save user data


                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("user", userName);
                    edit.putString("pass", userPassword);
                    edit.putString("user_level", "1");
                    edit.apply();

                    mProgress.setVisibility(View.VISIBLE);
                    signInBtn.setVisibility(View.GONE);

                    Cursor d = dbhelper.getAccessLevel(userName);
                    String full_name = d.getString(1);
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Successfully Logged In " + full_name);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();

                    if (usercount <= 1) {
                        if (dbhelper.fetchUsername("ODS").getCount() > 0) {

                            finish();
                            Intent login = new Intent(getApplicationContext(), SplashActivity.class);
                            startActivity(login);
                            return;
                        }
                    }

                    finish();
                    Intent login = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(login);
                } else {

                    if (_URL == null || _URL.equals("null/null")) {
                        Toast.makeText(LoginActivity.this, "Invalid Username/Password", Toast.LENGTH_LONG).show();
                    } else {
                        if (!new RestApiRequest(getApplicationContext()).isOnline()) {
                            Toast.makeText(this, "You are not online!!!!", Toast.LENGTH_SHORT).show();
                            // Log.v("Home", "############################You are not online!!!!");
                            return;
                        }
                        Login();
                    }
                    //Toast.makeText(LoginActivity.this,"Invalid Username/Password", Toast.LENGTH_LONG).show();

                }
                dbhelper.close();


            }
        }
    }

    public void Login() {
        progressDialog = ProgressDialog.show(LoginActivity.this,
                "Connecting",
                "Please Wait.. ");
        executor.execute(() -> {
            // do background work here
            try {
                restApiResponse = "";
                Log.i("_URL", _URL);
                restApiResponse = getTokenMain(_URL, userName, userPassword);

                JSONObject jsonObject = new JSONObject(restApiResponse);

                if (jsonObject.has("access_token") && !jsonObject.isNull("access_token")) {
                    access_token = jsonObject.getString("access_token");
                    token_type = jsonObject.getString("token_type");
                    expires_in = jsonObject.getString("expires_in");
                    userName = jsonObject.getString("userName");
                    issued = jsonObject.getString(".issued");
                    expires = jsonObject.getString(".expires");
                    Log.i("INFO", access_token + "" + token_type + "" + expires_in);

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("token", access_token);
                    edit.putString("expires_in", expires_in);
                    edit.putString("expires", expires);
                    edit.apply();

                } else {
                    error = jsonObject.getString("error");
                    desc = jsonObject.getString("error_description");

                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                // do UI changes after background work here
                _TOKEN = access_token;

                if (_TOKEN == null || _TOKEN.equals("")) {
                    progressDialog.dismiss();
                    if (restApiResponse.equals("-8080")) {
                        desc = "Failed to Connect to " + _URL;
                    }
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText(desc);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                } else {
                    progressDialog.dismiss();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Successfully Logged In ");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("user_level", "1");
                    edit.putString("user", userName);
                    edit.putString("user_fullname", userName);
                    edit.remove("pass");
                    edit.apply();

                    finish();
                    Intent login = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(login);
                }
            });
        });


    }

    public String getTokenMain(String URL, String Username, String Password) {

        try {
            String result;
            Response response;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "Grant_type=password&username=" + Username + "&password=" + Password + "");
            Request request = new Request.Builder()
                    .url(URL + "/token")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("RestApiRequest", response.body().string());
            result = responseBodyCopy.string();

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            String Server = "-8080";
            Log.e("SoapApiRequest", e.toString());
            Log.e("Server Response", e.toString());
            e.printStackTrace();
            return Server;
        }
    }

    public void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        String uname = prefs.getString("user", "");
        //  String pass = prefs.getString("pass", "");
        String pass = "";

        usernameWrapper.getEditText().setText(uname);

        try {
            if (uname.length() > 0 && pass.length() > 0) {
                DBHelper dbUser = new DBHelper(LoginActivity.this);


                if (dbUser.UserLogin(uname, pass)) {

                    if (usercount <= 1) {
                        if (dbhelper.fetchUsername("ODS").getCount() > 0) {
                            finish();
                            Intent login = new Intent(getApplicationContext(), SplashActivity.class);
                            startActivity(login);
                            return;
                        }

                    }

                    finish();
                    Intent login = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(login);
                }
                dbUser.close();
            }

        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

}
