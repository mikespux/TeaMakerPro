package com.easyway.pos.synctocloud;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.easyway.pos.data.DBHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MasterApiRequest {

    public String _URL;
    private String _TOKEN;
    Context _context;

    SharedPreferences mSharedPrefs;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    DBHelper dbHelper;
    SimpleDateFormat dateFormatA;
    String dateFormat;

    @SuppressLint("SimpleDateFormat")
    public MasterApiRequest(Context ctx) {
        _URL = null;
        _TOKEN = null;


        _context = ctx;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        dateFormatA = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        //  _URL = mSharedPrefs.getString("portalURL", null);

        if (mSharedPrefs.getString("internetAccessModes", null).equals("WF")) {
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

        _TOKEN = prefs.getString("token", null);
        if (_TOKEN == null || _TOKEN.equals("")) {
            _TOKEN = new RestApiRequest(_context).getToken();

        } else {
            long token_hours = new RestApiRequest(_context).token_hours();
            if (token_hours >= 23) {
                _TOKEN = new RestApiRequest(_context).getToken();

            }
        }
        dbHelper = new DBHelper(_context);


    }

    public String getCompany(String CoPrefix) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Companies?$filter=CoPrefix eq '" + CoPrefix + "'")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("companyresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getCompany", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getCompany", ex.toString());
            return ex.getMessage();
        }
    }

    public String ValidateDevice(String DeviceID) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Weighingkits?Estate=0&Factory=0&$select=RecordIndex,InternalSerial,ExternalSerial,AllocFactory&$filter=InternalSerial eq '" + DeviceID + "'")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("validatedevice", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getDevices", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getDevices", ex.toString());
            return ex.getMessage();
        }
    }

    public String getFactories(String CRecordIndex) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Factories?Co=" + CRecordIndex)
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("factoriesresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getFactories", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getFactories", ex.toString());
            return ex.getMessage();
        }
    }

    public String getAllocatedFactory(String CRecordIndex, String AllocFactory) {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/MasterData/Factories?Co=" + CRecordIndex + "&$filter=RecordIndex eq " + AllocFactory + "")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("factoriesresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getFactories", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getFactories", ex.toString());
            return ex.getMessage();
        }
    }

    public String getGrades() {

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(_URL + "/api/TeaTracking/Madeteagrades?$select=RecordIndex,FGRef,FGDescription")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("gradesresponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("getGrades", response.body().string());
            return responseBodyCopy.string();
        } catch (IOException ex) {
            Log.e("getGrades", ex.toString());
            return ex.getMessage();
        }
    }


}
