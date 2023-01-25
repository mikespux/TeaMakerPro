package com.easyway.pos.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.easyway.pos.R;
import com.easyway.pos.activities.MainActivity;
import com.easyway.pos.adapters.LotAdapter;
import com.easyway.pos.data.DBHelper;
import com.easyway.pos.data.Database;
import com.easyway.pos.helpers.StringWithTag;
import com.easyway.pos.models.LotList;
import com.easyway.pos.synctocloud.RestApiRequest;
import com.easyway.pos.weighing.SortingWeighActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.dmoral.toasty.Toasty;


public class ProductionLotFragment extends Fragment {

    public View mView;
    public Intent mIntent;


    DBHelper dbhelper;
    SQLiteDatabase db;

    static SharedPreferences prefs;
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat dateOnlyFormat;


    String _URL, _TOKEN;
    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    String getProdLot, FactoryID;
    int LotResponse;
    String RecordIndex, prodBatchNumber, prodDate, prodTotalCrop, prodBatFieldWeight;
    ArrayList<LotList> listLots = new ArrayList<>();
    LotAdapter lotAdapter;

    TextView textLotDate;
    String sdate;
    DatePickerDialog datePickerDialog;

    ListView listviewLots;
    TextView txtAccountId, tvDate, tvRnumber, tvFactoryWt, tvOpen, tvClose, tvWeigh, tvView;
    EditText edtDate, edtMadeTea;
    Button btnDate;
    Button btnOpenLot, btnCloseLot;
    String lot_no, lot_date, start_of_manufacture, end_of_manufacture, closing_time, total_made_tea, Status, CloudID;
    AlertDialog dPLot;

    String grade, gradeid;
    ArrayList<StringWithTag> gradedata = new ArrayList<>();
    ArrayAdapter<StringWithTag> gradeadapter;
    Spinner spGrade;

    View dialogView;


    String StartTime, FinishTime, WitheredLeaf, MadeTea, UserName;
    String restApiResponse, error = "";
    String Id, Title, Message;
    boolean view_receipt;
    Cursor accounts, weighments;
    ListView lvWeights;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_production_lots, container, false);
        initializer();
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(R.string.app_name);
        enableBT();
        return mView;
    }


    public void initializer() {

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();

        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
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
        textLotDate = mView.findViewById(R.id.tvDate);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-yyyy");
        sdate = dateOnlyFormat.format(cal.getTime());
        textLotDate.setText(format1.format(cal.getTime()));
        textLotDate.setOnClickListener(v -> {
            // calender class's instance and get current date , month and year from calender
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR); // current year
            int mMonth = c.get(Calendar.MONTH); // current month
            int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
            // date picker dialog
            datePickerDialog = new DatePickerDialog(getActivity(),
                    (view, year, monthOfYear, dayOfMonth) -> {
                        // set day of month , month and year value in the edit text
                        cal.setTimeInMillis(0);
                        cal.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
                        Date chosenDate = cal.getTime();
                        sdate = dateOnlyFormat.format(cal.getTime());
                        getProdLot(sdate);
                        textLotDate.setText(format1.format(chosenDate));

                    }, mYear, mMonth, mDay);
            datePickerDialog.show();

        });


    }

    private void Grade() {
        gradedata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();

        Cursor c = db.rawQuery("select CloudID,grade_code from grades ", null);
        if (c != null) {
            if (c.moveToFirst()) {
                if (view_receipt) {
                    gradedata.add(new StringWithTag("SUMMARY", "s"));
                } else {
                    gradedata.clear();
                }
                do {
                    grade = c.getString(c.getColumnIndexOrThrow("grade_code"));

                    if (c.getString(c.getColumnIndexOrThrow("CloudID")) == null) {
                        gradeid = "0";
                    } else {
                        gradeid = c.getString(c.getColumnIndexOrThrow("CloudID"));
                    }


                    gradedata.add(new StringWithTag(grade, gradeid));


                } while (c.moveToNext());
            }
        }

        gradeadapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, gradedata);
        gradeadapter.setDropDownViewResource(R.layout.spinner_item);
        gradeadapter.notifyDataSetChanged();
        spGrade.setAdapter(gradeadapter);
        c.close();
        spGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                gradeid = s.tag;
                grade = s.string;

                if (view_receipt) {
                    getWeighments(txtAccountId.getText().toString(), gradeid);
                }
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("gradeCode", gradeid);
                edit.apply();

                TextView tv = (TextView) view;


                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
               /* if(disabled.equals("true")) {
                    // Set the disable item text color
                    tv.setBackgroundColor(Color.parseColor("#E3E4ED"));

                }*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    public void showGrade() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_grade_list, null);
        dialogBuilder.setView(dialogView);

        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("GRADE");
        view_receipt = false;
        spGrade = dialogView.findViewById(R.id.spGrade);
        Grade();

        dialogBuilder.setPositiveButton("PROCEED", (dialog, whichButton) -> {

        });
        dialogBuilder.setNegativeButton("BACK", (dialog, whichButton) -> {
            getProdData();
            dialog.dismiss();
        });
        final AlertDialog b = dialogBuilder.create();
        b.show();
        b.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (spGrade.getSelectedItem().toString().equals("Select ...")) {
                Toast.makeText(getActivity(), "Please Select Grade", Toast.LENGTH_LONG).show();
                return;
            }
            // b.dismiss();
            mIntent = new Intent(getActivity(), SortingWeighActivity.class);
            startActivity(mIntent);

        });
    }

    public void getProdLot(String sdate) {
        progressDialog = ProgressDialog.show(getActivity(),
                "Fetching Production Lots.",
                "Please Wait.. ");
        executor.execute(() -> {
            try {

                listLots.clear();

                FactoryID = prefs.getString("AllocFactory", "0");
                getProdLot = new RestApiRequest(getActivity()).ProductionLots(FactoryID, sdate);
                LotResponse = prefs.getInt("LotResponse", 0);
                if (LotResponse == 200) {
                    RecordIndex = "0";

                    JSONArray arrayKnownAs = new JSONArray(getProdLot);


                    for (int i = 0, l = arrayKnownAs.length(); i < l; i++) {

                        JSONObject obj = arrayKnownAs.getJSONObject(i);

                        RecordIndex = obj.getString("RecordIndex");
                        prodBatchNumber = obj.getString("prodBatchNumber");
                        prodDate = obj.getString("prodDate");
                        prodTotalCrop = obj.getString("prodTotalCrop");
                        prodBatFieldWeight = obj.getString("prodBatFieldWeight");


                        Log.i("INFO", "prodBatchNumber: " + prodBatchNumber);
                        listLots.add(new LotList(RecordIndex, prodBatchNumber, prodDate, prodTotalCrop, prodBatFieldWeight));


                    }

                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
            requireActivity().runOnUiThread(() -> {

            });


            handler.post(() -> {

                // do UI changes after background work here
                LotResponse = prefs.getInt("LotResponse", 0);
                if (LotResponse == 200) {
                    progressDialog.dismiss();
                    if (Integer.parseInt(RecordIndex) > 0) {
                        getProdData();
                    } else {
                        Toasty.error(requireActivity(), "Production Lot Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });


    }

    public void getProdData() {
        listviewLots = mView.findViewById(R.id.listviewLots);


        lotAdapter = new LotAdapter(getActivity(), listLots);
        lotAdapter.notifyDataSetChanged();

        listviewLots.setAdapter(lotAdapter);
        listviewLots.performItemClick(listviewLots.getAdapter().getView(0, null, null), 0, listviewLots.getAdapter().getItemId(0));

        listviewLots.setOnItemClickListener((parent, view, position, id) -> {

            txtAccountId = view.findViewById(R.id.txtAccountId);
            tvDate = view.findViewById(R.id.tvDate);
            tvRnumber = view.findViewById(R.id.tvRnumber);
            tvFactoryWt = view.findViewById(R.id.tvFactoryWt);

            tvOpen = view.findViewById(R.id.tvOpen);
            tvClose = view.findViewById(R.id.tvClose);
            tvWeigh = view.findViewById(R.id.tvWeigh);
            tvView = view.findViewById(R.id.tvView);

            tvOpen.setOnClickListener(v -> diagOpenLot());
            tvClose.setOnClickListener(v -> {
                diagCloseLot(txtAccountId.getText().toString());
            });
            tvWeigh.setOnClickListener(v -> {
                String selectShift = "SELECT *  FROM " + Database.SHIFT_TABLE_NAME + " WHERE Status ='0'";
                Cursor cursorShift = db.rawQuery(selectShift, null);

                if (cursorShift.getCount() == 0) {
                    Context context = getActivity();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Open Shift to proceed to Weigh");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }
                cursorShift.close();
                String selectLot = "SELECT *  FROM " + Database.LOT_TABLE_NAME + " WHERE Status ='0'";
                Cursor cursorLot = db.rawQuery(selectLot, null);

                if (cursorLot.getCount() == 0) {
                    Context context = getActivity();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Open Lot to proceed to Weigh");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }
                cursorLot.close();
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("Schedule", txtAccountId.getText().toString());
                edit.putString("prodBatchNumber", tvRnumber.getText().toString());
                edit.putString("prodDate", tvRnumber.getText().toString());
                edit.apply();
                showGrade();

            });
            tvView.setOnClickListener(v -> {
                showRecieptDetails(txtAccountId.getText().toString());
            });

        });
        // Setting on Touch Listener for handling the touch inside ScrollView
        listviewLots.setOnTouchListener((v, event) -> {
            // Disallow the touch request for parent scroll on touch of child view
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
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
                sdate = dateTimeFormat.format(cal.getTime());
                edtDate.setText(dateTimeFormat.format(cal.getTime()));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    public void diagOpenLot() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_open_lot, null);
        dialogBuilder.setView(dialogView);

        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("OPEN LOT");
        Calendar cal = Calendar.getInstance();
        edtDate = dialogView.findViewById(R.id.edtDate);
        btnDate = dialogView.findViewById(R.id.btnDate);
        edtDate.setText(dateTimeFormat.format(cal.getTime()));
        btnDate.setOnClickListener(v -> {

            showDateTimePicker();

        });

        btnOpenLot = dialogView.findViewById(R.id.btnOpenLot);
        btnOpenLot.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Open Lot?");
            builder.setMessage("Are you sure you want to Start Production Lot?")
                    .setCancelable(false)
                    .setNegativeButton("Yes", (dialog, id) -> {

                        lot_no = tvRnumber.getText().toString();
                        lot_date = tvDate.getText().toString();
                        start_of_manufacture = edtDate.getText().toString();
                        end_of_manufacture = "";
                        closing_time = "";
                        total_made_tea = "";
                        Status = "0";
                        CloudID = txtAccountId.getText().toString();

                        Cursor CheckLot = dbhelper.CheckLot(CloudID);
                        if (!(CheckLot.getCount() > 0)) {
                            dbhelper.AddLot(lot_no, lot_date, start_of_manufacture, end_of_manufacture, closing_time, total_made_tea, Status, CloudID);
                            dPLot.dismiss();
                            getProdLot(lot_date);

                        }
                    })

                    .setPositiveButton("No", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        });
        dialogBuilder.setPositiveButton("BACK", (dialog, whichButton) -> {
            //do something with edt.getText().toString();
            dialog.dismiss();

        });
        dPLot = dialogBuilder.create();
        dPLot.show();

    }

    public void diagCloseLot(String LOT_ID) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_close_lot, null);
        dialogBuilder.setView(dialogView);

        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("CLOSE LOT");
        Calendar cal = Calendar.getInstance();
        edtDate = dialogView.findViewById(R.id.edtDate);
        btnDate = dialogView.findViewById(R.id.btnDate);
        edtDate.setText(dateTimeFormat.format(cal.getTime()));
        btnDate.setOnClickListener(v -> {

            showDateTimePicker();

        });
        edtMadeTea = dialogView.findViewById(R.id.edtMadeTea);
        final DecimalFormat df = new DecimalFormat("#0.0#");
        final DecimalFormat df1 = new DecimalFormat("##");


        Cursor weighments = db.rawQuery("select " +
                "" + Database.LOT_ID +
                ",COUNT(" + Database.ROW_ID + ")" +
                ",SUM(" + Database.TARE + ")" +
                ",SUM(" + Database.GROSS + ") FROM " +
                Database.SORTING_TABLE_NAME + " WHERE "
                + Database.LOT_ID + " ='" + LOT_ID + "'", null);

        if (weighments.getCount() > 0) {
            weighments.moveToFirst();

            edtMadeTea.setText(df.format(weighments.getDouble(3)));

        }
        weighments.close();
        btnCloseLot = dialogView.findViewById(R.id.btnCloseLot);
        btnCloseLot.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Close Lot?");
            builder.setMessage("Are you sure you want to End Production Lot?")
                    .setCancelable(false)
                    .setNegativeButton("Yes", (dialog, id) -> {

                        progressDialog = ProgressDialog.show(getActivity(),
                                "Closing Lot.",
                                "Please Wait.. ");
                        executor.execute(() -> {
                            db = dbhelper.getReadableDatabase();

                            StartTime = dateTimeFormat.format(cal.getTime());
                            FinishTime = edtDate.getText().toString();
                            WitheredLeaf = "0";
                            MadeTea = edtMadeTea.getText().toString();
                            UserName = prefs.getString("user", "");

                            JSONObject LotObject = new JSONObject();
                            try {
                                LotObject.put("Index", LOT_ID);
                                LotObject.put("StartTime", StartTime);
                                LotObject.put("FinishTime", FinishTime);
                                LotObject.put("WitheredLeaf", WitheredLeaf);
                                LotObject.put("MadeTea", MadeTea);
                                LotObject.put("UserName", UserName);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Log.i("ShiftInfo", LotObject.toString());
                            restApiResponse = new RestApiRequest(getActivity()).CompleteLot(LotObject.toString());
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
                                    LayoutInflater inflater1 = getLayoutInflater();
                                    View customToastroot = inflater1.inflate(R.layout.red_toast, null);
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
                                        LayoutInflater inflater1 = getLayoutInflater();
                                        View customToastroot = inflater1.inflate(R.layout.white_red_toast, null);
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
                                    if (Integer.parseInt(Id) <= 0) {

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
        });
        dialogBuilder.setPositiveButton("BACK", (dialog, whichButton) -> {
            //do something with edt.getText().toString();
            dialog.dismiss();

        });
        dPLot = dialogBuilder.create();
        dPLot.show();

    }

    public void showRecieptDetails(String LOT_ID) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_receipt_details, null);
        dialogBuilder.setView(dialogView);
        //dialogBuilder.setTitle("All Weighment Receipts");
        TextView toolbar = dialogView.findViewById(R.id.app_bar);
        toolbar.setText("All Weighment Receipts");
        view_receipt = true;

        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
        spGrade = dialogView.findViewById(R.id.spGrade);
        Grade();


        getWeighments(LOT_ID, "");


        dialogBuilder.setPositiveButton("Cancel", (dialog, whichButton) -> {

        });


//        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//
//            }
//        });


        final AlertDialog b = dialogBuilder.create();
        b.show();
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {

            b.dismiss();

        });
    }

    public void getWeighments(String LOT_ID, String gradeid) {
        lvWeights = dialogView.findViewById(R.id.lvWeights);
        if (gradeid.equals("") || gradeid.equals("0")) {


            accounts = db.rawQuery("select * from " + Database.SORTING_TABLE_NAME + "," + Database.GRADES_TABLE_NAME + " WHERE "
                    + Database.LOT_ID + " ='" + LOT_ID + "' and " + Database.GRADE_ID + "=grades.CloudID order by sorting_time DESC", null);

        } else if (gradeid.equals("s")) {
            accounts = db.rawQuery("select * ,SUM(" + Database.GROSS + ") as gross from " + Database.SORTING_TABLE_NAME + "," + Database.GRADES_TABLE_NAME + " WHERE "
                    + Database.LOT_ID + " ='" + LOT_ID + "' and " + Database.GRADE_ID + "=grades.CloudID group by " + Database.GRADE_CODE + " order by sorting_time DESC", null);
        } else {
            accounts = db.rawQuery("select * from " + Database.SORTING_TABLE_NAME + "," + Database.GRADES_TABLE_NAME + " WHERE "
                    + Database.LOT_ID + " ='" + LOT_ID + "' and " + Database.GRADE_ID + "=grades.CloudID and " + Database.GRADE_ID + "='" + gradeid + "' order by sorting_time DESC", null);
        }

        TextView textTotalKgs = dialogView.findViewById(R.id.textTotalKgs);

        if (accounts.getCount() == 0) {
            textTotalKgs.setText("0.0");
            lvWeights.setVisibility(View.GONE);
        } else {


            //Toast.makeText(this, "records found", Toast.LENGTH_LONG).show();}


            final DecimalFormat df = new DecimalFormat("#0.0#");

            if (gradeid.equals("") || gradeid.equals("0") || gradeid.equals("s")) {
                weighments = db.rawQuery("select " +
                        "" + Database.LOT_ID +
                        ",COUNT(" + Database.ROW_ID + ")" +
                        ",SUM(" + Database.TARE + ")" +
                        ",SUM(" + Database.GROSS + ") FROM " +
                        Database.SORTING_TABLE_NAME + " WHERE "
                        + Database.LOT_ID + " ='" + LOT_ID + "'", null);
            } else {
                weighments = db.rawQuery("select " +
                        "" + Database.LOT_ID +
                        ",COUNT(" + Database.ROW_ID + ")" +
                        ",SUM(" + Database.TARE + ")" +
                        ",SUM(" + Database.GROSS + ") FROM " +
                        Database.SORTING_TABLE_NAME + " WHERE "
                        + Database.LOT_ID + " ='" + LOT_ID + "'and " + Database.GRADE_ID + "='" + gradeid + "'", null);
            }

            if (weighments.getCount() == 0) {
                textTotalKgs.setText("0.0");
            } else {

                weighments.moveToFirst();


                textTotalKgs.setText(df.format(weighments.getDouble(3)) + " Kgs");

            }
            weighments.close();


        }
        while (accounts.moveToNext()) {
            String[] from = {Database.ROW_ID, Database.GRADE_CODE, Database.GROSS, Database.SORTING_TIME};
            int[] to = {R.id.txtAccountId, R.id.txtGrade, R.id.txtWeight, R.id.txtTime};


            SimpleCursorAdapter ca = new SimpleCursorAdapter(dialogView.getContext(), R.layout.receipt_bag_list, accounts, from, to);


            lvWeights.setAdapter(ca);
            lvWeights.setTextFilterEnabled(true);
            ca.notifyDataSetChanged();
            lvWeights.setVisibility(View.VISIBLE);
            // db.close();
            //dbhelper.close();
        }
    }

    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();

        }
    }
}

