package com.easyway.pos.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.easyway.pos.R;
import com.easyway.pos.data.DBHelper;
import com.easyway.pos.data.Database;
import com.easyway.pos.masters.SyncMastersActivity;
import com.easyway.pos.synctocloud.MasterApiRequest;
import com.easyway.pos.synctocloud.RestApiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class CompanyDetailsActivity extends AppCompatActivity {
    public static final String TAG = "Activate";
    Intent mIntent;
    public Toolbar toolbar;
    EditText co_prefix, co_name, co_letterbox, co_postcode, co_postname, co_postregion, co_telephone;
    String Sco_prefix, Sco_name, Sco_letterbox, Sco_postcode, Sco_postname, Sco_postregion, Sco_telephone;
    DBHelper db;
    Button btn_svCompany;
    String CRecordIndex, CoPrefix, CoName, CoLetterBox, CoPostCode, CoPostName, coPostRegion, CoTelephone,
            CoFax, CoEmail, CoRegistrationNumber, CoPIN, CoVAT, CoCountry, CoCity, CoWebsite;
    String _URL, _TOKEN;
    SharedPreferences prefs;

    String restApiResponse;
    String Id, Title, Message;
    String s_terminal;
    String s_phone;
    EditText et_terminal, et_phone;
    Button btnActivate;
    AlertDialog dActivate;

    String CompanyID, DeviceID, PhoneNo, UserID, SerialNumber;

    String RecordIndex, InternalSerial, ExternalSerial, AllocFactory;

    String factories;
    String factoryid = null;
    ArrayList<String> factorydata = new ArrayList<>();
    ArrayAdapter<String> factoryadapter;
    Spinner spinnerFactory;
    String FRecordIndex, FryPrefix, FryTitle, FryCapacity, FryCoName, FryPostalAddress, poCode, poName, FryPostOffice, FryTelephone;
    DBHelper dbHelper;
    boolean refresh = false;


    int response;
    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companydetails);
        setupToolbar();
        initializer();

    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Company Details");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void initializer() {
        dbHelper = new DBHelper(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (isInternetOn()) {
            createNetErrorDialog();
            return;
        }
        refresh = prefs.getBoolean("prefs_refresh", false);
        if (refresh) {
            _TOKEN = prefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = new RestApiRequest(getApplicationContext()).getToken();

            } else {
                long token_hours = new RestApiRequest(getApplicationContext()).token_hours();
                if (token_hours >= 23) {
                    _TOKEN = new RestApiRequest(getApplicationContext()).getToken();

                }
            }

        } else {
            _TOKEN = prefs.getString("token", null);
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
        co_prefix = findViewById(R.id.co_prefix);
        co_name = findViewById(R.id.co_name);
        co_letterbox = findViewById(R.id.co_letterbox);
        co_postcode = findViewById(R.id.co_postcode);
        co_postname = findViewById(R.id.co_postname);
        co_postregion = findViewById(R.id.co_postregion);
        co_telephone = findViewById(R.id.co_telephone);
        if (refresh) {
            AllocatedFactoryDetails();
        } else {
            CompanyDetails();

        }
        db = new DBHelper(getApplicationContext());

        btn_svCompany = findViewById(R.id.btn_svCompany);


        btn_svCompany.setOnClickListener(v -> {
            if (isInternetOn()) {
                createNetErrorDialog();
                return;
            }
            Sco_prefix = co_prefix.getText().toString();
            Sco_name = co_name.getText().toString();
            Sco_letterbox = co_letterbox.getText().toString();
            Sco_postcode = co_postcode.getText().toString();
            Sco_postname = co_postname.getText().toString();
            Sco_postregion = co_postregion.getText().toString();
            Sco_telephone = co_telephone.getText().toString();
            if (Sco_prefix.equals("") || Sco_name.equals("")) {
                Toast.makeText(getApplicationContext(), "Please enter Company Prefix and Company Name!!", Toast.LENGTH_LONG).show();
                return;
            }
            SharedPreferences.Editor edit = prefs.edit();

            edit.putString("company_prefix", Sco_prefix);
            edit.putString("company_name", Sco_name);
            edit.putString("company_letterbox", Sco_letterbox);
            edit.putString("company_postalcode", Sco_postcode);
            edit.putString("company_postalname", Sco_postname);
            edit.putString("company_postregion", Sco_postregion);
            edit.putString("company_posttel", Sco_telephone);
            edit.putString("licenseKey", Sco_prefix);
            edit.apply();
            if (refresh) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Details Saved Successfully");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
            } else {
                showActivateDialog();
            }


        });


    }

    public void CompanyDetails() {

        progressDialog = ProgressDialog.show(CompanyDetailsActivity.this,
                "Connecting",
                "Please Wait.. ");

        executor.execute(() -> {

            //Background work here
            try {

                CRecordIndex = "0";

                restApiResponse = "";
                CoPrefix = prefs.getString("coprefix", "");

                restApiResponse = new MasterApiRequest(getApplicationContext()).getCompany(CoPrefix);
                response = prefs.getInt("companyresponse", 0);
                if (response == 200) {
                    //  Log.e(TAG, "Response from url: " + jsonStr);
                    try {


                        JSONArray dataArray = new JSONArray(restApiResponse);
                        for (int i = 0, l = dataArray.length(); i < l; i++) {
                            JSONObject jsonObject = dataArray.getJSONObject(i);
                            if (jsonObject.has("RecordIndex") && !jsonObject.isNull("RecordIndex")) {
                                // Do something with object.

                                CRecordIndex = jsonObject.getString("RecordIndex");
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("CRecordIndex", CRecordIndex);
                                edit.putString("CompanyIndex", CRecordIndex);
                                edit.apply();
                                CoPrefix = jsonObject.getString("CoPrefix");
                                CoName = jsonObject.getString("CoName");
                                CoLetterBox = jsonObject.getString("CoLetterBox");
                                CoPostCode = jsonObject.getString("CoPostCode");
                                CoPostName = jsonObject.getString("CoPostName");
                                coPostRegion = jsonObject.getString("coPostRegion");
                                CoTelephone = jsonObject.getString("CoTelephone");
                                CoFax = jsonObject.getString("CoFax");

                            }
                        }


                    } catch (final JSONException e) {
                        Log.e("TAG", "Json parsing error: " + e.getMessage());

                    }
                }

                if (refresh) {
                    DeviceID = prefs.getString("terminalID", "");


                    restApiResponse = new MasterApiRequest(getApplicationContext()).ValidateDevice(DeviceID);
                    response = prefs.getInt("validatedevice", 0);
                    if (response == 200) {
                        try {


                            JSONArray dataArrayDevice = new JSONArray(restApiResponse);
                            if (dataArrayDevice.length() > 0) {
                                for (int i = 0, l = dataArrayDevice.length(); i < l; i++) {
                                    JSONObject jsonObjectDevice = dataArrayDevice.getJSONObject(i);
                                    if (jsonObjectDevice.has("RecordIndex") && !jsonObjectDevice.isNull("RecordIndex")) {
                                        // Do something with object.

                                        RecordIndex = jsonObjectDevice.getString("RecordIndex");
                                        InternalSerial = jsonObjectDevice.getString("InternalSerial");
                                        ExternalSerial = jsonObjectDevice.getString("ExternalSerial");
                                        AllocFactory = jsonObjectDevice.getString("AllocFactory");
                                        SharedPreferences.Editor edit = prefs.edit();
                                        edit.putString("Factory", jsonObjectDevice.getString("FryTitle"));
                                        edit.apply();

                                        Log.i("INFO", "RecordIndex: " + RecordIndex + " InternalSerial: " + InternalSerial + " AllocEstate: " + AllocFactory);

                                    }
                                }
                            } else {

                                CRecordIndex = "-1";
                                Title = "";
                                Message = "Registered Device Not Found.";
                                Log.i("INFO", "Message: " + Message);
                                return;
                            }


                        } catch (final JSONException e) {
                            Log.e("TAG", "Json parsing error: " + e.getMessage());

                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                //UI Thread work here
                if (Integer.parseInt(CRecordIndex) > 0) {
                    co_prefix.setText(CoPrefix);
                    co_name.setText(CoName);
                    co_letterbox.setText(CoLetterBox);
                    co_postcode.setText(CoPostCode);
                    co_postname.setText(CoPostName);
                    co_postregion.setText(coPostRegion);
                    co_telephone.setText(CoTelephone);

                    progressDialog.dismiss();

                } else {
                    if (refresh) {
                        if (Integer.parseInt(CRecordIndex) == -1) {
                            progressDialog.dismiss();
                            Context context = getApplicationContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText(Message);
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();

                            co_prefix.setText(CoPrefix);
                            co_name.setText(CoName);
                            co_letterbox.setText(CoLetterBox);
                            co_postcode.setText(CoPostCode);
                            co_postname.setText(CoPostName);
                            co_postregion.setText(coPostRegion);
                            co_telephone.setText(CoTelephone);
                            return;
                        }
                    }
                    progressDialog.dismiss();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Company Details Not Loaded");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    if (refresh) {
                        finish();
                    } else {
                        finish();
                        mIntent = new Intent(getApplicationContext(), CompanyURLConfigActivity.class);
                        startActivity(mIntent);
                        Toast.makeText(getApplicationContext(), _URL, Toast.LENGTH_LONG).show();
                    }

                }
            });
        });
    }

    @SuppressLint("Range")
    public void ValidateDevice() {
        progressDialog = ProgressDialog.show(CompanyDetailsActivity.this,
                "Validating",
                "Please Wait.. ");
        executor.execute(() -> {

            //Background work here
            try {
                restApiResponse = "";

                DeviceID = s_terminal;
                restApiResponse = new MasterApiRequest(getApplicationContext()).ValidateDevice(DeviceID);
                response = prefs.getInt("validatedevice", 0);
                if (response == 200) {
                    try {


                        JSONArray dataArray = new JSONArray(restApiResponse);
                        if (dataArray.length() > 0) {
                            for (int i = 0, l = dataArray.length(); i < l; i++) {
                                JSONObject jsonObject = dataArray.getJSONObject(i);
                                if (jsonObject.has("RecordIndex") && !jsonObject.isNull("RecordIndex")) {
                                    // Do something with object.

                                    RecordIndex = jsonObject.getString("RecordIndex");
                                    InternalSerial = jsonObject.getString("InternalSerial");
                                    ExternalSerial = jsonObject.getString("ExternalSerial");
                                    AllocFactory = jsonObject.getString("AllocFactory");
                                    Log.i("INFO", "RecordIndex: " + RecordIndex + " InternalSerial: " + InternalSerial + " AllocFactory: " + AllocFactory);

                                }
                            }
                        } else {
                            AllocFactory = "-1";
                            Id = "-1";
                            Title = "";
                            Message = "This Device is Not Found!";
                            Log.i("INFO", "Message: " + Message);
                        }

                        if (AllocFactory == null || AllocFactory.equals("")) {

                            Id = "-1";
                            Title = "";
                            Message = "Device is not allocated to Estate";
                        } else if (Integer.parseInt(AllocFactory) > 0) {
                            Id = RecordIndex;
                        }

                    } catch (final JSONException e) {
                        Log.e("TAG", "Json parsing error: " + e.getMessage());

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            handler.post(() -> {
                if (response == 200) {

                    //UI Thread work here
                    try {

                        if (Integer.parseInt(Id) > 0) {
                            progressDialog.dismiss();

                            try {

                                if (Integer.parseInt(Id) > 0) {

                                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                                    final Cursor account = db.query(Database.FACTORY_TABLE_NAME, null,
                                            " FryCloudID = ?", new String[]{AllocFactory}, null, null, null);
                                    //startManagingCursor(accounts);
                                    if (account.moveToFirst()) {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(CompanyDetailsActivity.this);
                                        builder.setTitle("Ready to proceed?")
                                                .setMessage(Html.fromHtml("<font color='#2b419a'>This Phone is Allocated to </font><b>" +
                                                        account.getString(account.getColumnIndex(Database.FRY_TITLE)) + "</b>"))
                                                .setCancelable(false)
                                                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                                    @SuppressLint("Range")
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        SharedPreferences.Editor edit = prefs.edit();
                                                        edit.putString("Factory", account.getString(account.getColumnIndex(Database.FRY_TITLE)));
                                                        edit.putString("FryPrefix", account.getString(account.getColumnIndex(Database.FRY_PREFIX)));
                                                        edit.putString("AllocFactory", AllocFactory);
                                                        edit.apply();
                                                        Activate();
                                                        AllocatedFactoryDetails();

                                                    }
                                                })
                                                .setPositiveButton("No", (dialog, id) -> dialog.cancel());

                                        final AlertDialog btnback = builder.create();
                                        btnback.show();


                                    }

                                } else {

                                    Toast.makeText(getBaseContext(), Message, Toast.LENGTH_LONG).show();
                                }


                            } catch (NumberFormatException e) {

                                if (restApiResponse.equals("-8080")) {
                                    Toast.makeText(getBaseContext(), "Failed To Connect to Server", Toast.LENGTH_LONG).show();
                                }

                            }


                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getBaseContext(), Message, Toast.LENGTH_LONG).show();

                        }


                    } catch (NumberFormatException e) {
                        progressDialog.dismiss();
                        if (restApiResponse.equals("-8080")) {
                            Toast.makeText(getBaseContext(), "Failed To Connect to Server", Toast.LENGTH_LONG).show();
                        }

                    }
                }
            });
        });

    }

    public static String getIMEIDeviceId(Context context) {

        String deviceId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
            }
            assert mTelephony != null;
            if (mTelephony.getDeviceId() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceId = mTelephony.getImei();
                } else {
                    deviceId = mTelephony.getDeviceId();
                }
            } else {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        Log.d("deviceId", deviceId);
        return deviceId;
    }

    public void showActivateDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_activate_device, null);
        dialogBuilder.setView(dialogView);
        // dialogBuilder.setTitle("New Event");

        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("Activate Device");

        spinnerFactory = dialogView.findViewById(R.id.spinnerFactory);
        spinnerFactory.setVisibility(View.GONE);
        LoadFactories();
        et_terminal = dialogView.findViewById(R.id.et_terminal);
        et_phone = dialogView.findViewById(R.id.et_phone);
        btnActivate = dialogView.findViewById(R.id.btnActivate);


        btnActivate.setOnClickListener(v -> {

            try {

                s_terminal = et_terminal.getText().toString();
                s_phone = et_phone.getText().toString();

                if (s_terminal.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter Terminal ID", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("terminalID", s_terminal);
                edit.putString("PhoneNo", s_phone);
                edit.apply();

                ValidateDevice();
            } catch (Exception e) {

                Toast.makeText(getApplicationContext(), "Saving  Failed", Toast.LENGTH_LONG).show();


            }

        });

        dialogBuilder.setPositiveButton("Cancel", (dialog, whichButton) -> {
            //do something with edt.getText().toString();


        });

        dActivate = dialogBuilder.create();
        dActivate.show();
    }

    public void Activate() {
        progressDialog = ProgressDialog.show(CompanyDetailsActivity.this,
                "Activating",
                "Please Wait.. ");
        executor.execute(() -> {
            //Background work here
            try {
                restApiResponse = "";

                CompanyID = Sco_prefix;

                DeviceID = s_terminal;

                PhoneNo = s_phone;


                restApiResponse = ActivateDevice(CompanyID, DeviceID, PhoneNo);
                JSONObject jsonObject = new JSONObject(restApiResponse);
                Id = jsonObject.getString("Id");
                Title = jsonObject.getString("Title");
                Message = jsonObject.getString("Message");
                Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);


            } catch (final JSONException e) {
                Log.e("TAG", "Json parsing error: " + e.getMessage());

            }


            handler.post(() -> {
                //UI Thread work here
                try {

                    if (Integer.parseInt(Id) > 0) {

                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("serverTerminalID", Id);
                        edit.apply();

                        Toast.makeText(getBaseContext(), "Device Activation " + Message, Toast.LENGTH_LONG).show();


                        progressDialog.dismiss();
                        dActivate.dismiss();
                        finish();
                        mIntent = new Intent(getApplicationContext(), SyncMastersActivity.class);
                        startActivity(mIntent);


                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getBaseContext(), Message, Toast.LENGTH_LONG).show();
                    }


                } catch (NumberFormatException e) {
                    progressDialog.dismiss();
                    if (restApiResponse.equals("-8080")) {
                        Toast.makeText(getBaseContext(), "Failed To Connect to Server", Toast.LENGTH_LONG).show();
                    }

                }
            });
        });

    }

    public String ActivateDevice(String CompanyID, String DeviceID, String PhoneNo) {

        try {

            //   CompanyID= prefs.getString("company_prefix", "");
            UserID = prefs.getString("user", "");
            //  DeviceID= prefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
            SerialNumber = getIMEIDeviceId(getApplicationContext());
            //SerialNumber= getSerialNumber();
            // PhoneNo= "";

            Log.d(TAG, SerialNumber);
            String result = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();

            JSONObject IOTjsonObject = new JSONObject();
            IOTjsonObject.put("CompanyID", CompanyID);
            IOTjsonObject.put("UserID", UserID);
            IOTjsonObject.put("DeviceID", DeviceID);
            IOTjsonObject.put("SerialNumber", SerialNumber);
            IOTjsonObject.put("MISDN", PhoneNo);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, IOTjsonObject.toString());
            Request request = new Request.Builder()
                    .url(_URL + "/api/Purchases/Activatedevice")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("RestApiRequest", response.body().string());
            result = responseBodyCopy.string();

            return result;
        } catch (IOException | JSONException e) {
            String Server = "-8080";
            Log.e("SoapApiRequest", e.toString());
            Log.e("Server Response", e.toString());
            e.printStackTrace();
            return Server;
        }

    }

    public void LoadFactories() {
        progressDialog = ProgressDialog.show(CompanyDetailsActivity.this,
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


                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor factories = db.query(true, Database.FACTORY_TABLE_NAME, null, null, null, null, null, null, null, null);
                    if (factories.getCount() == 0) {
                        String DefaultFactory = "INSERT INTO " + Database.FACTORY_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.FRY_PREFIX + ", "
                                + Database.FRY_TITLE + ", "
                                + Database.FRY_ClOUDID + ") Values ('0','0', 'Select ...','0')";
                        db.execSQL(DefaultFactory);
                    }

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
                        Cursor checkFactory = dbHelper.CheckFactory(FryPrefix);
                        //Check for duplicate FryPrefix
                        if (!(checkFactory.getCount() > 0)) {
                            dbHelper.AddFactories(FryPrefix, FryTitle, FRecordIndex);
                        }


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }

            }

            handler.post(() -> {
                //UI Thread work here

                progressDialog.dismiss();
                FactoryList();
            });
        });

    }

    private void FactoryList() {
        factorydata.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select FryPrefix,FryTitle from factory ", null);
        if (c != null) {

            if (c.moveToFirst()) {
                do {
                    factories = c.getString(c.getColumnIndexOrThrow("FryTitle"));
                    factorydata.add(factories);

                } while (c.moveToNext());
            }
        }
        assert c != null;
        c.close();

        factoryadapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, factorydata);
        factoryadapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerFactory.setAdapter(factoryadapter);
        spinnerFactory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String factoryName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor c = db.rawQuery("select FryCloudID from factory where FryTitle= '" + factoryName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    factoryid = c.getString(c.getColumnIndexOrThrow("FryCloudID"));


                }
                assert c != null;
                c.close();
                // db.close();
                // dbhelper.close();
                TextView tv = (TextView) view;
                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }


    public void AllocatedFactoryDetails() {
        if (refresh) {
            progressDialog = ProgressDialog.show(CompanyDetailsActivity.this,
                    "Please Wait",
                    "Connecting... ");
        }
        executor.execute(() -> {
            //Background work here
            CRecordIndex = prefs.getString("CompanyIndex", null);
            AllocFactory = prefs.getString("AllocFactory", null);
            FRecordIndex = "0";

            restApiResponse = new MasterApiRequest(getApplicationContext()).getAllocatedFactory(CRecordIndex, AllocFactory);
            response = prefs.getInt("factoriesresponse", 0);
            if (response == 200) {

                try {


                    JSONArray arrayKnownAs = new JSONArray(restApiResponse);
                    // Do something with object.
                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {
                        JSONObject obj = arrayKnownAs.getJSONObject(i);

                        FRecordIndex = obj.getString("RecordIndex");
                        FryPrefix = obj.getString("FryPrefix");
                        FryTitle = obj.getString("FryTitle");
                        FryCapacity = obj.getString("FryCapacity");
                        FryCoName = obj.getString("FryCoName");
                        FryPostalAddress = obj.getString("FryPostalAddress");
                        poCode = obj.getString("poCode");
                        if (poCode.equals("null")) {
                            poCode = "";
                        }
                        poName = obj.getString("poName");
                        if (poName.equals("null")) {
                            poName = "";
                        }
                        FryPostOffice = obj.getString("FryPostOffice");
                        FryTelephone = obj.getString("FryTelephone");


                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("FRecordIndex", FRecordIndex);
                        edit.putString("FryPrefix", FryPrefix);
                        edit.putString("company_name", FryCoName);
                        edit.putString("company_letterbox", FryPostalAddress);
                        edit.putString("company_postalcode", poCode);
                        edit.putString("company_postalname", poName);
                        edit.putString("company_posttel", FryTelephone);
                        edit.apply();


                    }


                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());

                }

            }

            handler.post(() -> {
                //UI Thread work here
                if (response == 200) {
                    if (refresh) {
                        if (Integer.parseInt(FRecordIndex) > 0) {

                            co_prefix.setText(prefs.getString("company_prefix", ""));
                            co_name.setText(prefs.getString("company_name", ""));
                            co_letterbox.setText(prefs.getString("company_letterbox", ""));
                            co_postcode.setText(prefs.getString("company_postalcode", ""));
                            co_postname.setText(prefs.getString("company_postalname", ""));
                            co_postregion.setText(prefs.getString("company_postregion", ""));
                            co_telephone.setText(prefs.getString("company_posttel", ""));

                            progressDialog.dismiss();

                        } else {

                            progressDialog.dismiss();
                            Context context = getApplicationContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = customToastroot.findViewById(R.id.toast);
                            text.setText("Details Not Loaded");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            finish();

                        }

                    }
                } else {

                    progressDialog.dismiss();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Details Not Loaded");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    finish();

                }
            });
        });
    }

    public boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec = (ConnectivityManager) getBaseContext().getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {


            return false;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {


            return true;
        }
        return true;
    }

    protected void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(CompanyDetailsActivity.this);
        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>You need internet connection to upload data. Please turn on mobile network or Wi-Fi in Settings.</font>"))
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

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        if (refresh) {
            finish();
        } else {
            finish();
            mIntent = new Intent(getApplicationContext(), CompanyURLConfigActivity.class);
            startActivity(mIntent);
        }
        return;
    }


}
