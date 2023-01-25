package com.easyway.pos.synctocloud;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RestApiRequest {

    private final String TAG;
    public String _URL;
    private String _TOKEN;
    private final Context _context;
    private String _licenseKey;

    SharedPreferences mSharedPrefs;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;
    String CompanyID, UserID, DeviceID, SerialNumber;
    String access_token, token_type, expires_in, userName, issued, expires;
    SimpleDateFormat dateFormatA;
    String dateFormat;
    String Id, Title, Message;
    ConnectivityManager connectivityManager;
    boolean connected = false;

    public RestApiRequest(Context ctx) {
        _URL = null;
        _TOKEN = null;
        _licenseKey = null;

        TAG = "RestApiRequest";

        _context = ctx;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        prefs = PreferenceManager.getDefaultSharedPreferences(_context);

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
        // dateFormat="E, dd MMM yyyy HH:mm:ss Z";
        dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        dateFormatA = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        _TOKEN = mSharedPrefs.getString("token", null);
        _licenseKey = mSharedPrefs.getString("licenseKey", null);

        CompanyID = mSharedPrefs.getString("company_prefix", "");
        UserID = prefs.getString("user", "");
        DeviceID = mSharedPrefs.getString("terminalID", "");
        SerialNumber = getIMEIDeviceId();

    }

    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) _context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;


        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v(TAG, e.toString());
        }
        return connected;
    }

    public String getAllowedDevice(String URL, String Token, String Terminal) {

        try {
            String result;
            Response response;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("http://" + URL + "/api/MasterData/Weighingkits?Estate=0&Factory=0&$select=RecordIndex,InternalSerial,ExternalSerial,AllocFactory&$filter=InternalSerial eq '" + Terminal + "'")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + Token)
                    .build();


            response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("RestApiRequest", response.body().string());
            result = responseBodyCopy.string();

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long token_hours() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat input = new SimpleDateFormat(dateFormat);
        Date token_time = null;
        long difference;
        try {
            token_time = input.parse(mSharedPrefs.getString("start_time", dateFormatA.format(cal.getTime())));
            // token_time = input.parse("2020-11-30T22:50:45Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i("TOKEN_HOURS ", token_time + " Current Date " + new Date());
        difference = new Date().getTime() - token_time.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        //long elapsedDays = different / daysInMilli;
        //different = different % daysInMilli;

        long elapsedHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;

        long elapsedMinutes = difference / minutesInMilli;
        difference = difference % minutesInMilli;

        long elapsedSeconds = difference / secondsInMilli;
        Log.e("======= Hours", " :: " + elapsedHours);
        Log.e("======= min", " :: " + elapsedMinutes);
        System.out.printf("%d hours, %d minutes, %d seconds%n", elapsedHours, elapsedMinutes, elapsedSeconds);
        return elapsedHours;
    }

    public String getToken() {

        try {

            String result;
            Response response;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();

            JSONObject IOTjsonObject = new JSONObject();
            IOTjsonObject.put("CompanyID", CompanyID);
            IOTjsonObject.put("UserID", UserID);
            IOTjsonObject.put("DeviceID", DeviceID);
            IOTjsonObject.put("SerialNumber", SerialNumber);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, IOTjsonObject.toString());
            Request request = new Request.Builder()
                    .url(_URL + "/api/Purchases/Token")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("RestApiRequest", response.body().string());
            result = responseBodyCopy.string();

            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("access_token") && !jsonObject.isNull("access_token")) {
                access_token = jsonObject.getString("access_token");
                token_type = jsonObject.getString("token_type");
                expires_in = jsonObject.getString("expires_in");
                userName = jsonObject.getString("userName");
                issued = jsonObject.getString(".issued");
                expires = jsonObject.getString(".expires");
                Log.i("INFO", access_token + "" + token_type + "" + expires_in);
                Calendar cal = Calendar.getInstance();
                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.putString("token", access_token);
                edit.putString("expires_in", expires_in);
                edit.putString("expires", expires);
                edit.putString("start_time", dateFormatA.format(cal.getTime()));
                edit.apply();
                return access_token;
            } else if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                Id = jsonObject.getString("Id");
                Title = jsonObject.getString("Title");
                Message = result;

                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.remove("token");
                edit.remove("expires_in");
                edit.remove("expires");
                edit.apply();
                return Id;
            }
            return result;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String getIMEIDeviceId() {

        String deviceId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(_context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) _context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (_context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
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
                deviceId = Settings.Secure.getString(_context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        Log.d("deviceId", deviceId);
        return deviceId;
    }

    public String changePin(String Pin) {

        try {
            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();
                }
            }

            String result;
            Response response;
            OkHttpClient client = new OkHttpClient().newBuilder().build();

            JSONObject IOTjsonObject = new JSONObject();
            IOTjsonObject.put("CompanyID", CompanyID);
            IOTjsonObject.put("UserID", UserID);
            IOTjsonObject.put("DeviceID", DeviceID);
            IOTjsonObject.put("SerialNumber", SerialNumber);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, IOTjsonObject.toString());
            Request request = new Request.Builder()
                    .url(_URL + "/api/Purchases/Changepin?pin=" + Pin)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "application/json")
                    .build();

            response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("RestApiRequest", response.body().string());
            result = responseBodyCopy.string();

            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                Id = jsonObject.getString("Id");
                Title = jsonObject.getString("Title");
                Message = jsonObject.getString("Message");
                Log.i("INFO", Id + "" + Title + "" + Message);
                Calendar cal = Calendar.getInstance();
                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.putString("Title", Title);
                edit.putString("Message", Message);
                edit.apply();
                return Id;

            }
            return result;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String resetPin(String User, String Pin) {

        try {

            String result;
            Response response;
            OkHttpClient client = new OkHttpClient().newBuilder().build();

            JSONObject IOTjsonObject = new JSONObject();
            IOTjsonObject.put("CompanyID", CompanyID);
            IOTjsonObject.put("UserID", User);
            IOTjsonObject.put("DeviceID", DeviceID);
            IOTjsonObject.put("SerialNumber", SerialNumber);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, IOTjsonObject.toString());
            Request request = new Request.Builder()
                    .url(_URL + "/api/Purchases/Changepin?pin=" + Pin)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "application/json")
                    .build();

            response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            assert response.body() != null;
            Log.i("RestApiRequest", response.body().string());
            result = responseBodyCopy.string();

            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                Id = jsonObject.getString("Id");
                Title = jsonObject.getString("Title");
                Message = jsonObject.getString("Message");
                Log.i("INFO", Id + "" + Title + "" + Message);
                Calendar cal = Calendar.getInstance();
                SharedPreferences.Editor edit = mSharedPrefs.edit();
                edit.putString("Title", Title);
                edit.putString("Message", Message);
                edit.apply();
                return Id;

            }
            return result;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String StartShift(String shiftInfo) {

        try {

            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();
                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, shiftInfo);

            Request request = new Request.Builder()
                    .url(_URL + "/api/TeaTracking/Startshift")
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("StartShift", response.body().string());
            return responseBodyCopy.string();


        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("StartShift", ex.toString());
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String FinishShift(String shiftInfo) {

        try {

            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();
                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, shiftInfo);

            Request request = new Request.Builder()
                    .url(_URL + "/api/TeaTracking/FinishShift")
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("FinishShift", response.body().string());
            return responseBodyCopy.string();


        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("FinishShift", ex.toString());
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String ProductionLots(String factory, String LotDate) {

        try {

            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();
                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(_URL + "/api/TeaTracking/Getdailylot?factory=" + factory + "&date=" + LotDate + "&$select=RecordIndex,prodBatchNumber,prodDate,prodTotalCrop,prodBatFieldWeight")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .build();
            Response response = client.newCall(request).execute();
            edit = prefs.edit();
            edit.putInt("LotResponse", response.code());
            edit.apply();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("Lots", response.body().string());
            return responseBodyCopy.string();


        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("Lots", ex.toString());
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String MadeTea(String madeTeaInfo) {

        try {

            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();
                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, madeTeaInfo);

            Request request = new Request.Builder()
                    .url(_URL + "/api/TeaTracking/Madetea")
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("MadeTea", response.body().string());
            return responseBodyCopy.string();


        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("MadeTea", ex.toString());
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }

    public String CompleteLot(String lotInfo) {

        try {

            _TOKEN = mSharedPrefs.getString("token", null);
            if (_TOKEN == null || _TOKEN.equals("")) {
                _TOKEN = getToken();
            } else {
                if (token_hours() >= 23) {
                    _TOKEN = getToken();
                }
            }

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, lotInfo);

            Request request = new Request.Builder()
                    .url(_URL + "/api/TeaTracking/CompleteLot")
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + _TOKEN)
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            Log.i("CompleteLot", response.body().string());
            return responseBodyCopy.string();


        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("CompleteLot", ex.toString());
            String Server = "-8080";
            Log.e("Server Response", Server);
            return Server;
        }
    }
}
