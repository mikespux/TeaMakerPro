package com.easyway.pos.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.easyway.pos.R;
import com.easyway.pos.activities.MainActivity;
import com.easyway.pos.data.DBHelper;
import com.easyway.pos.data.Database;
import com.easyway.pos.synctocloud.RestApiRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ShiftFragment extends Fragment {

    public View mView;
    public Intent mIntent;


    DBHelper dbhelper;
    SQLiteDatabase db;

    static SharedPreferences prefs;
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat dateOnlyFormat;

    TextView dateDisplay, textTerminal, txtCompanyInfo;
    Button btnShiftOn, btnShiftOff;
    TextView textShiftNo, textShiftDate;
    String shift_date, start_time, working_hours, user_id, factoryid, Status, CloudID;
    String _URL, _TOKEN;
    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    String restApiResponse, serverShiftNo, error = "";
    String Id, Title, Message;

    EditText edtDate, edtWorkingHours;
    Button btnDate;
    Button btnOpenShift;
    AlertDialog dShift;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_shift, container, false);
        initializer();
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(R.string.app_name);
        return mView;
    }


    public void initializer() {

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();

        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:00:00", Locale.getDefault());
        dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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


        _TOKEN = prefs.getString("token", null);
        if (_TOKEN == null || _TOKEN.equals("")) {
            _TOKEN = new RestApiRequest(getActivity()).getToken();

        } else {
            long token_hours = new RestApiRequest(getActivity()).token_hours();
            if (token_hours >= 23) {
                _TOKEN = new RestApiRequest(getActivity()).getToken();

            }


        }

        textTerminal = mView.findViewById(R.id.textTerminal);
        textTerminal.setText(prefs.getString("terminalID", ""));
        txtCompanyInfo = mView.findViewById(R.id.txtCompanyInfo);
        Calendar cal = Calendar.getInstance();
        String year = String.format("%d", cal.get(Calendar.YEAR));
        txtCompanyInfo.setText(prefs.getString("company_name", "") + " ©" + year);
        dateDisplay = mView.findViewById(R.id.date);
        dateDisplay.setText(DateFormat.getDateInstance().format(new Date()));

        btnShiftOn = mView.findViewById(R.id.btnShiftOn);
        btnShiftOn.setOnClickListener(v -> diagOpenShift());

        btnShiftOff = mView.findViewById(R.id.btnShiftOff);
        btnShiftOff.setOnClickListener(v -> FinishShift());

        textShiftNo = mView.findViewById(R.id.textShiftNo);
        textShiftDate = mView.findViewById(R.id.textShiftDate);


        String selectQuery = "SELECT shift_date,CloudID  FROM " + Database.SHIFT_TABLE_NAME + " WHERE Status =0";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                shift_date = (cursor.getString(0));
                CloudID = (cursor.getString(1));

            } while (cursor.moveToNext());

            textShiftNo.setText(CloudID);
            textShiftDate.setText(shift_date);

            btnShiftOff.setVisibility(View.VISIBLE);
            btnShiftOn.setVisibility(View.GONE);
        } else {
            btnShiftOn.setVisibility(View.VISIBLE);
            btnShiftOff.setVisibility(View.GONE);
            textShiftNo.setText("No Shift Opened");
            textShiftDate.setText(dateOnlyFormat.format(cal.getTime()));
        }
        cursor.close();


    }

    public void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(getActivity(), (view, year, monthOfYear, dayOfMonth) -> {
            cal.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(getActivity(), (view1, hourOfDay, minute) -> {
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                Log.v("TAG", "The choosen one " + cal.getTime());
                edtDate.setText(dateTimeFormat.format(cal.getTime()));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    @SuppressLint("MissingInflatedId")
    public void diagOpenShift() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_open_shift, null);
        dialogBuilder.setView(dialogView);

        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("OPEN SHIFT");
        Calendar cal = Calendar.getInstance();
        edtDate = dialogView.findViewById(R.id.edtDate);
        edtWorkingHours = dialogView.findViewById(R.id.edtWorkingHours);

        btnDate = dialogView.findViewById(R.id.btnDate);
        edtDate.setText(dateTimeFormat.format(cal.getTime()));
        btnDate.setOnClickListener(v -> {

            showDateTimePicker();

        });

        btnOpenShift = dialogView.findViewById(R.id.btnOpenShift);
        btnOpenShift.setOnClickListener(v -> {
            if (edtDate.getText().toString().equals("")) {
                Context context = getActivity();
                LayoutInflater inflater1 = requireActivity().getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Sorry! Start Time Cannot be Empty!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                return;
            }
            if (edtWorkingHours.getText().toString().equals("")) {
                Context context = getActivity();
                LayoutInflater inflater1 = requireActivity().getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Sorry! Working Hours Cannot be Empty!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                return;
            }
            StartShift();
        });
        dialogBuilder.setPositiveButton("BACK", (dialog, whichButton) -> {
            //do something with edt.getText().toString();
            dialog.dismiss();

        });
        dShift = dialogBuilder.create();
        dShift.show();

    }

    public void StartShift() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Start Shift?");
        builder.setMessage("Are you sure you want to Start Shift?")
                .setCancelable(false)
                .setNegativeButton("Yes", (dialog, id) -> {
                    progressDialog = ProgressDialog.show(getActivity(),
                            "Starting Shift.",
                            "Please Wait.. ");
                    executor.execute(() -> {

                        db = dbhelper.getReadableDatabase();
                        Calendar cal = Calendar.getInstance();
                        shift_date = dateOnlyFormat.format(cal.getTime());
                        start_time = edtDate.getText().toString();
                        working_hours = edtWorkingHours.getText().toString();
                        factoryid = prefs.getString("AllocFactory", null);
                        user_id = prefs.getString("user", "");

                        JSONObject ShiftObject = new JSONObject();
                        try {
                            ShiftObject.put("Index", 0);
                            ShiftObject.put("Factory", factoryid);
                            ShiftObject.put("ShiftDate", shift_date);
                            ShiftObject.put("ShiftStart", start_time);
                            ShiftObject.put("ShiftHours", working_hours);
                            ShiftObject.put("UserID", user_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.i("ShiftInfo", ShiftObject.toString());
                        restApiResponse = new RestApiRequest(getActivity()).StartShift(ShiftObject.toString());
                        error = restApiResponse;
                        Log.i("restApiResponse", restApiResponse);
                        try {

                            JSONObject jsonObject = new JSONObject(restApiResponse);
                            Message = jsonObject.getString("Message");
                            if (Message.equals("Authorization has been denied for this request.")) {
                                Id = "-1";
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.remove("token");
                                edit.remove("expires_in");
                                edit.remove("expires");
                                edit.apply();
                                return;
                            }
                            if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                                Id = jsonObject.getString("Id");
                                Title = jsonObject.getString("Title");
                                Message = jsonObject.getString("Message");

                                Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);
                                try {
                                    if (Integer.parseInt(Id) > 0) {
                                        serverShiftNo = Id;
                                        Cursor CheckShift = dbhelper.CheckShift(Id);
                                        if (!(CheckShift.getCount() > 0)) {
                                            dbhelper.AddShift(shift_date, start_time, working_hours, user_id, factoryid, "0", Id);
                                            SharedPreferences.Editor edit = prefs.edit();
                                            edit.putString("serverShiftNo", serverShiftNo);
                                            edit.putString("ShiftStart", start_time);
                                            edit.putString("WorkingHours", working_hours);
                                            edit.apply();
                                        }
                                    }
                                    if (Integer.parseInt(Id) < 0) {
                                        error = Id;

                                    }
                                    //System.out.println(value);}
                                } catch (NumberFormatException e) {
                                    //value = 0; // your default value
                                    return;

                                }
                            } else {
                                Id = "-1";
                                Title = "";
                                Message = restApiResponse;
                                return;

                            }
                        } catch (JSONException e) {

                            Id = "-1";
                            Title = "";
                            Message = _URL + "\n" + e;
                            e.printStackTrace();
                            return;
                        }


                        handler.post(() -> {
                            // do UI changes after background work here

                            if (error.equals("-8080")) {
                                progressDialog.dismiss();

                                Context context = getActivity();
                                LayoutInflater inflater = getLayoutInflater();
                                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                TextView text = customToastroot.findViewById(R.id.toast);
                                text.setText("Server Not Available !!");
                                Toast customtoast = new Toast(context);
                                customtoast.setView(customToastroot);
                                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                customtoast.setDuration(Toast.LENGTH_LONG);
                                customtoast.show();
                                //Toast.makeText(mActivity, "Server Not Available !!", Toast.LENGTH_LONG).show();
                                // Log.i(TAG, "Server Not Available !!");
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("error", "Server Not Available !!");
                                edit.apply();
                                return;
                            }
                            try {

                                if (Integer.parseInt(Id) > 0) {
                                    progressDialog.dismiss();
                                    Context context = getActivity();
                                    LayoutInflater inflater = getLayoutInflater();
                                    View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                    TextView text = customToastroot.findViewById(R.id.toast);
                                    text.setText("Shift: " + Id + " Started Successfully");
                                    Toast customtoast = new Toast(context);
                                    customtoast.setView(customToastroot);
                                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                    customtoast.setDuration(Toast.LENGTH_LONG);
                                    customtoast.show();
                                    requireActivity().finish();
                                    mIntent = new Intent(getActivity(), MainActivity.class);
                                    startActivity(mIntent);

                                    return;
                                }
                                if (Integer.parseInt(Id) < 0) {

                                    progressDialog.dismiss();
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putString("error", Id + "\n {" + Title + "} \n" + Message);
                                    edit.apply();
                                    Log.e("Error", Id + " {" + Title + "} " + Message);

                                    Toast.makeText(getActivity(), Message, Toast.LENGTH_LONG).show();
                                }

                            } catch (NumberFormatException e) {

                                progressDialog.dismiss();
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("error", error);
                                edit.apply();
                                Log.i("RestApiRequest", Id + " {" + Title + "} " + Message);


                            }


                        });
                    });
                })
                .setPositiveButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void FinishShift() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Finish Shift?");
        builder.setMessage("Are you sure you want to Finish Shift?")
                .setCancelable(false)
                .setNegativeButton("Yes", (dialog, id) -> {
                    progressDialog = ProgressDialog.show(getActivity(),
                            "Finishing Shift.",
                            "Please Wait.. ");
                    executor.execute(() -> {


                        db = dbhelper.getReadableDatabase();
                        Calendar cal = Calendar.getInstance();
                        CloudID = prefs.getString("serverShiftNo", "0");
                        working_hours = prefs.getString("WorkingHours", "0");
                        shift_date = dateOnlyFormat.format(cal.getTime());
                        start_time = prefs.getString("ShiftStart", "0");
                        factoryid = prefs.getString("AllocFactory", null);
                        user_id = prefs.getString("user", "");

                        JSONObject ShiftObject = new JSONObject();
                        try {
                            ShiftObject.put("Index", CloudID);
                            ShiftObject.put("Factory", factoryid);
                            ShiftObject.put("ShiftDate", shift_date);
                            ShiftObject.put("ShiftStart", start_time);
                            ShiftObject.put("ShiftHours", working_hours);
                            ShiftObject.put("UserID", user_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.i("ShiftInfo", ShiftObject.toString());
                        restApiResponse = new RestApiRequest(getActivity()).FinishShift(ShiftObject.toString());
                        error = restApiResponse;
                        Log.i("restApiResponse", restApiResponse);
                        try {

                            JSONObject jsonObject = new JSONObject(restApiResponse);
                            Message = jsonObject.getString("Message");
                            if (Message.equals("Authorization has been denied for this request.")) {
                                Id = "-1";
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.remove("token");
                                edit.remove("expires_in");
                                edit.remove("expires");
                                edit.apply();
                                return;
                            }
                            if (jsonObject.has("Id") && !jsonObject.isNull("Id")) {
                                Id = jsonObject.getString("Id");
                                Title = jsonObject.getString("Title");
                                Message = jsonObject.getString("Message");

                                Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + Message);

                            } else {
                                Id = "-1";
                                Title = "";
                                Message = restApiResponse;
                                return;

                            }
                        } catch (JSONException e) {

                            Id = "-1";
                            Title = "";
                            Message = _URL + "\n" + e;
                            e.printStackTrace();
                            return;
                        }


                        handler.post(() -> {
                            // do UI changes after background work here

                            if (error.equals("-8080")) {
                                progressDialog.dismiss();

                                Context context = getActivity();
                                LayoutInflater inflater = getLayoutInflater();
                                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                TextView text = customToastroot.findViewById(R.id.toast);
                                text.setText("Server Not Available !!");
                                Toast customtoast = new Toast(context);
                                customtoast.setView(customToastroot);
                                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                customtoast.setDuration(Toast.LENGTH_LONG);
                                customtoast.show();
                                //Toast.makeText(mActivity, "Server Not Available !!", Toast.LENGTH_LONG).show();
                                // Log.i(TAG, "Server Not Available !!");
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("error", "Server Not Available !!");
                                edit.apply();
                                return;
                            }
                            try {

                                if (Integer.parseInt(Id) > 0) {


                                    ContentValues values = new ContentValues();

                                    values.put(Database.Status, 1);
                                    values.put(Database.WORKING_HOURS, working_hours);


                                    long rows = db.update(Database.SHIFT_TABLE_NAME, values,
                                            "CloudID = ? ", new String[]{CloudID});
                                    if (rows > 0) {
                                        progressDialog.dismiss();
                                        Context context = getActivity();
                                        LayoutInflater inflater = getLayoutInflater();
                                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                        TextView text = customToastroot.findViewById(R.id.toast);
                                        text.setText("Finished Shift: " + Id + " Successfully");
                                        Toast customtoast = new Toast(context);
                                        customtoast.setView(customToastroot);
                                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                        customtoast.setDuration(Toast.LENGTH_LONG);
                                        customtoast.show();
                                        requireActivity().finish();
                                        mIntent = new Intent(getActivity(), MainActivity.class);
                                        startActivity(mIntent);

                                    } else {
                                        Toast.makeText(getActivity(), "Sorry! Could not Close Batch!", Toast.LENGTH_LONG).show();
                                    }
                                    return;
                                }
                                if (Integer.parseInt(Id) < 0) {

                                    progressDialog.dismiss();
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putString("error", Id + "\n {" + Title + "} \n" + Message);
                                    edit.apply();
                                    Log.e("Error", Id + " {" + Title + "} " + Message);

                                    Toast.makeText(getActivity(), Message, Toast.LENGTH_LONG).show();
                                }

                            } catch (NumberFormatException e) {

                                progressDialog.dismiss();
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("error", error);
                                edit.apply();
                                Log.i("RestApiRequest", Id + " {" + Title + "} " + Message);


                            }


                        });
                    });
                })
                .setPositiveButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }


}

