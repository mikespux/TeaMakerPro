package com.easyway.pos.weighing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.easyway.pos.R;
import com.easyway.pos.activities.DeviceListActivity;
import com.easyway.pos.connector.P25ConnectionException;
import com.easyway.pos.connector.P25Connector;
import com.easyway.pos.data.DBHelper;
import com.easyway.pos.services.NewWeighingService;
import com.easyway.pos.synctocloud.RestApiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SortingWeighActivity extends AppCompatActivity {

    public static final String TAG = SortingWeighActivity.class.getSimpleName();
    Toolbar toolbar;


    // Declaration of Shared Preferences.
    static SharedPreferences prefs;
    //Font Declaration
    Typeface font;

    DBHelper dbhelper;
    SQLiteDatabase db;

    // Declaration Number Format.
    DecimalFormat formatter = new DecimalFormat("0000");

    // Declaration of Integers and Doubles.
    double bagWeight = 0.0;
    double streamedWeight = 0.0;


    // Declaration of TextViews.
    static TextView tvStreamingReading;

    static TextView tvScaleConn;

    static TextView tvPrinterConn;
    static int printconn = 1;
    static int preconnection = 1;


    public static Context _ctx;
    public static Activity _activity;

    public static final String TOAST = "toast";

    // Verification Modes.
    public static final String CARD = "Card";
    public static final String BOTH = "Both";
    public static final String MANUAL = "Manual";
    public static final String PALLET = "Pallet";

    // Scale Versions
    public static String SCALE_VERSION = "scaleVersion";
    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";
    public static final String DR_150 = "DR-150";


    //Device Details
    static String cachedDeviceAddress;
    static String mConnectedDeviceName = null;
    public static final String DEVICE_NAME = "device_name";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String DEVICE_TYPE = "device_type";

    //Socket represents the open connection.
    private static Messenger mWeighingService;
    private static boolean mWeighingServiceBound;

    static ProgressDialog mProcessDialog;
    public static final int TARE_SCALE = 0;
    public static final int ZERO_SCALE = 12;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int READING_PROBE = 6;

    public static final int REQUEST_DEVICEADDRESS = 101;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int COMPLETE_FAILURE = 404;


    public static ProgressDialog mConnectingDlg;
    public static BluetoothAdapter mBluetoothAdapter;
    public static P25Connector mConnector;


    TextView tvBatchNo;
    String DelivaryNoteNo;

    static Double GROSS_KG = 0.0;
    static double[] weighments;
    static String stableReadingCounter;

    static double tareWeight = 0.0, setTareWeight = 0.0, setMoisture = 0.0, totalMoisture = 0.0;
    static double netWeight = 0.0;


    static TextView tvStability, tvError, tvFarmerName, tvShowBatchNumber,
            tvShowGrossTotal, tvWeighingAccumWeigh, tvWeighingTareWeigh,
            tvUnitsCount, tvShowTotalKgs, tvGross, tvBagNo1, tvBagNo2,
            tvNetWeightAccepted, tvGrossAccepted, tvTareAccepted;

    EditText etShowGrossTotal;

    static TextView tvsavedReading, tvSavedNet,
            tvSavedTare, tvSavedUnits, tvSavedTotal;


    static Button btn_accept, btn_print, btn_next, btn_reconnect;
    LinearLayout lt_accept, lt_nprint;


    int bgCount = 1;


    static Double myGross = 0.0;

    public static Button btnPrinterReconnect;
    ImageView c_refresh, c_success, c_error;


    ProgressDialog progressDialog;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    String Index, Schedule, Shift, WeighingKit, SortingDate, Grade, Gross, Tare, WeighingTime;
    String restApiResponse, error = "";
    String Id, Title, tMessage;
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat dateOnlyFormat;
    String _URL, _TOKEN;
    AlertDialog.Builder dialogAccept;
    public static AlertDialog dialog_accept;
    private Boolean dialogShownOnce = false;


    //Layout Setup
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_sorting_weigh);
        setupToolbar();
        initializer();
    }

    //Toolbar Title()
    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(Objects.requireNonNull(getSupportActionBar())).setTitle(R.string.title_weigh_sorted);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Initialize and set all variables.
    public void initializer() {
        _ctx = this;
        _activity = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        dbhelper = new DBHelper(this);
        db = dbhelper.getReadableDatabase();
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        stableReadingCounter = prefs.getString("stabilityReadingCounter", "3");

        weighments = new double[Integer.parseInt(stableReadingCounter)];


        font = Typeface.createFromAsset(getApplicationContext().getAssets(), "LCDM2B__.TTF");
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
            _TOKEN = new RestApiRequest(getApplicationContext()).getToken();

        } else {
            long token_hours = new RestApiRequest(getApplicationContext()).token_hours();
            if (token_hours >= 23) {
                _TOKEN = new RestApiRequest(getApplicationContext()).getToken();

            }


        }


        tvScaleConn = this.findViewById(R.id.tvScaleConn);
        tvPrinterConn = this.findViewById(R.id.tvPrinterConn);


        btnPrinterReconnect = this.findViewById(R.id.btnPrinterReconnect);
        btnPrinterReconnect.setOnClickListener(v -> {
            preconnection = 1;
            connect();
        });

        tvStreamingReading = this.findViewById(R.id.tvStreamingReading);
        tvStreamingReading.setText("0.0");
        tvStreamingReading.setVisibility(View.GONE);

        try {
            cachedDeviceAddress = prefs.getString("scale_address", "");
            // Toast.makeText(getBaseContext(), "Current Scale:"+cachedDeviceAddress, Toast.LENGTH_LONG).show();

            //Not yet connected to service
            mWeighingServiceBound = false;
            registerReceiver(scaleReceiver, initIntentFilter());

            if (prefs.getBoolean("enablePrinting", false)) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        showWeigh();
    }

    // Used while initiating the activity.
    public void onStart() {
        super.onStart();


        if (prefs.getString("vModes", "Manual").equals(MANUAL)) {
            if (NewWeighingService.mState != NewWeighingService.STATE_CONNECTED) {
                mProcessDialog = new ProgressDialog(this);
                mProcessDialog.setTitle("Please Wait");
                mProcessDialog.setMessage("Attempting Connection to Scale ...");
                mProcessDialog.setCancelable(false);
                mProcessDialog.show();
            }
        }


    }


    public void showWeigh() {


        tvError = findViewById(R.id.tvError);
        tvStability = findViewById(R.id.tvStability);

        tvShowBatchNumber = findViewById(R.id.tvShowBatchNumber);
        tvShowBatchNumber.setText(prefs.getString("prodBatchNumber", ""));

        tvBagNo1 = findViewById(R.id.tvBagNo1);
        tvBagNo1.setText(String.valueOf(bgCount));

        tvWeighingAccumWeigh = findViewById(R.id.tvWeighingAccumWeigh);
        tvWeighingAccumWeigh.setTypeface(font);

        tvWeighingTareWeigh = findViewById(R.id.tvWeighingTareWeigh);
        tvWeighingTareWeigh.setTypeface(font);
        // tvWeighingTareWeigh.setText(String.valueOf(setTareWeight));

        tvShowTotalKgs = findViewById(R.id.tvShowTotalKgs);
        tvShowTotalKgs.setTypeface(font);

        tvsavedReading = findViewById(R.id.tvvGross);
        tvSavedNet = findViewById(R.id.tvvTotalKgs);
        tvSavedTare = findViewById(R.id.tvTareWeight);
        tvSavedUnits = findViewById(R.id.tvvcount);
        tvSavedTotal = findViewById(R.id.tvAccumWeight);

        tvUnitsCount = findViewById(R.id.tvUnitsCount);
        tvUnitsCount.setTypeface(font);
        tvUnitsCount.setText(String.valueOf(bgCount));

        tvShowGrossTotal = findViewById(R.id.tvShowGrossTotal);
        tvShowGrossTotal.setTypeface(font);

        tvGrossAccepted = findViewById(R.id.tvGrossAccepted);
        tvGrossAccepted.setTypeface(font);

        tvTareAccepted = findViewById(R.id.tvTareAccepted);
        tvTareAccepted.setTypeface(font);

        tvNetWeightAccepted = findViewById(R.id.tvNetWeightAccepted);
        tvNetWeightAccepted.setTypeface(font);

        etShowGrossTotal = findViewById(R.id.etShowGrossTotal);
        etShowGrossTotal.setTypeface(font);

        c_error = findViewById(R.id.c_error);
        c_success = findViewById(R.id.c_success);
        c_refresh = findViewById(R.id.c_refresh);
        Glide.with(getApplicationContext()).load(R.drawable.ic_refresh).asGif().into(c_refresh);

        lt_accept = findViewById(R.id.lt_accept);
        lt_nprint = findViewById(R.id.lt_nprint);


        btn_accept = findViewById(R.id.btn_accept);
        btn_accept.setOnClickListener(v -> {


            if (tvShowGrossTotal.getText().equals("0.0")) {
                Context context = getApplicationContext();
                LayoutInflater inflater1 = getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Gross Total Cannot be 0.0, Please Request For Gross Reading");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_SHORT);
                customtoast.show();
                //Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
                return;

            }


            if (!allWeightsTheSame(weighments)) {
                Log.i("WeightCount", "Not Equal:" + Arrays.toString(weighments));
                tvStability.setVisibility(View.VISIBLE);
                // tvStability.setText("UnStable:"+  Arrays.toString(weighments));

                return;

            } else {
                Log.i("WeightCount", "Readings Equal:" + Arrays.toString(weighments));
                Context context = _activity;
                LayoutInflater inflater1 = _activity.getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.blue_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Reading Stable:" + Arrays.toString(weighments));
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                //customtoast.show();
                tvStability.setVisibility(View.GONE);
            }

            AcceptReading(v);


        });

        btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(v -> {

            myGross = Double.parseDouble(tvStreamingReading.getText().toString());
            if (myGross > 0) {

                Context context = getApplicationContext();
                LayoutInflater inflater12 = getLayoutInflater();
                View customToastroot = inflater12.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Remove Load!\nTo Continue ...");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_SHORT);
                customtoast.show();
                return;
            }
            c_refresh.setVisibility(View.GONE);
            c_success.setVisibility(View.GONE);
            c_error.setVisibility(View.GONE);

            lt_accept.setVisibility(View.VISIBLE);
            lt_nprint.setVisibility(View.GONE);

            tvShowGrossTotal.setVisibility(View.VISIBLE);
            tvWeighingTareWeigh.setVisibility(View.VISIBLE);
            tvShowTotalKgs.setVisibility(View.VISIBLE);

            tvGrossAccepted.setVisibility(View.GONE);
            tvTareAccepted.setVisibility(View.GONE);
            tvNetWeightAccepted.setVisibility(View.GONE);

            // bgCount = bgCount+1;
            // tvBagNo1.setText(String.valueOf(bgCount));


            tvsavedReading.setText("Reading");
            tvSavedNet.setText("Net Weight");
            tvSavedTare.setText("Tare Weight");
            tvSavedUnits.setText("Count");
            tvSavedTotal.setText("Total Kgs");

            tvsavedReading.setTextColor(Color.BLACK);
            tvSavedNet.setTextColor(Color.BLACK);
            tvSavedTare.setTextColor(Color.BLACK);
            tvSavedUnits.setTextColor(Color.BLACK);
            tvSavedTotal.setTextColor(Color.BLACK);

        });
        btn_print = findViewById(R.id.btn_print);

        if (!prefs.getBoolean("enablePrinting", false)) {
            if (!prefs.getBoolean("enableSMS", false)) {
                btn_print.setVisibility(View.GONE);
                //Toast.makeText(getBaseContext(), "SMS not enabled on Settings", Toast.LENGTH_LONG).show();

            } else {
                btn_print.setVisibility(View.GONE);
                btn_print.setText("Send SMS");
            }
        }

        btn_print.setOnClickListener(v -> {


        });
        btn_reconnect = findViewById(R.id.btn_reconnect);
        btn_reconnect.setOnClickListener(v -> {
            preconnection = 2;
            connect();
        });


        //first get scale version
        if (prefs.getString("scaleVersion", "Manual").equals(MANUAL)) {
            etShowGrossTotal.setVisibility(View.VISIBLE);
            tvShowGrossTotal.setVisibility(View.GONE);
        } else {

            etShowGrossTotal.setVisibility(View.GONE);
            tvShowGrossTotal.setVisibility(View.VISIBLE);
        }


    }

    public static boolean allWeightsTheSame(double[] array) {
        if (array.length == 0) {
            return true;
        } else {
            double first = array[0];
            for (double element : array) {
                if (element != first) {
                    return false;
                }
            }
            return true;
        }
    }


    public void AcceptReading(View v) {

        dialogAccept = new AlertDialog.Builder(v.getContext());
        // Setting Dialog Title
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_grossweight, null);
        dialogAccept.setView(dialogView);
        dialogAccept.setCancelable(false);
        dialogAccept.setTitle("Accept Reading?");
        // Setting Dialog Message
        //dialogAccept.setMessage("Are you sure you want to accept the gross reading?");
        tvBagNo2 = dialogView.findViewById(R.id.tvBagNo2);
        tvBagNo2.setText(String.valueOf(bgCount));
        tvGross = dialogView.findViewById(R.id.txtGross);
        tvGross.setTypeface(font);
        if (tvShowGrossTotal.getText().equals("0.0")) {

            tvGross.setText("0 KG");

        } else {

            tvGross.setText(tvShowTotalKgs.getText().toString() + " KG");
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("Gross", tvShowGrossTotal.getText().toString());
            edit.putString("Net", tvShowTotalKgs.getText().toString());
            edit.putString("Accum", tvWeighingAccumWeigh.getText().toString());
            edit.putString("tareAcc", tvWeighingTareWeigh.getText().toString());
            edit.apply();

        }

        ///Setting Negative "Yes" Button
        dialogAccept.setNegativeButton("YES",
                (dialog, which) -> {

                    dbhelper = new DBHelper(v.getContext());
                    db = dbhelper.getReadableDatabase();

                    c_refresh.setVisibility(View.VISIBLE);
                    c_success.setVisibility(View.GONE);
                    c_error.setVisibility(View.GONE);

                    progressDialog = ProgressDialog.show(v.getContext(),
                            "Saving Weight.",
                            "Please Wait.. ");
                    executor.execute(() -> {

                        db = dbhelper.getReadableDatabase();
                        Calendar cal = Calendar.getInstance();
                        final DecimalFormat df = new DecimalFormat("#0.0#");
                        double Accum, NewAccum;
                        Accum = Double.parseDouble(prefs.getString("Net", "")) + Double.parseDouble(prefs.getString("Accum", ""));


                        double GrossT, Net, TareT;
                        Net = Double.parseDouble(prefs.getString("Net", ""));
                        TareT = Double.parseDouble(tvUnitsCount.getText().toString()) * Double.parseDouble(prefs.getString("tareAcc", ""));
                        GrossT = Net + Double.parseDouble(tvWeighingTareWeigh.getText().toString());


                        Schedule = prefs.getString("Schedule", "");
                        Shift = prefs.getString("serverShiftNo", "");
                        WeighingKit = prefs.getString("terminalID", "");
                        SortingDate = dateOnlyFormat.format(cal.getTime());
                        Grade = prefs.getString("gradeCode", "");
                        Gross = df.format(GrossT);
                        Tare = tvWeighingTareWeigh.getText().toString();
                        WeighingTime = dateTimeFormat.format(cal.getTime());

                        JSONObject MTeaObject = new JSONObject();
                        try {
                            MTeaObject.put("Index", 0);
                            MTeaObject.put("Schedule", Schedule);
                            MTeaObject.put("Shift", Shift);
                            MTeaObject.put("WeighingKit", WeighingKit);
                            MTeaObject.put("SortingDate", SortingDate);
                            MTeaObject.put("Grade", Grade);
                            MTeaObject.put("Gross", Gross);
                            MTeaObject.put("Tare", Tare);
                            MTeaObject.put("WeighingTime", WeighingTime);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.i("MadeTeaInfo", MTeaObject.toString());
                        restApiResponse = new RestApiRequest(getApplicationContext()).MadeTea(MTeaObject.toString());
                        error = restApiResponse;
                        Log.i("restApiResponse", restApiResponse);
                        try {

                            JSONObject jsonObject = new JSONObject(restApiResponse);
                            tMessage = jsonObject.getString("Message");
                            if (tMessage.equals("Authorization has been denied for this request.")) {
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
                                tMessage = jsonObject.getString("Message");

                                Log.i("INFO", "ID: " + Id + " Title" + Title + " Message" + tMessage);
                                try {
                                    if (Integer.parseInt(Id) > 0) {
                                        runOnUiThread(() -> {
                                            c_refresh.setVisibility(View.VISIBLE);
                                            c_success.setVisibility(View.GONE);
                                            c_error.setVisibility(View.GONE);
                                        });
                                        Cursor CheckMadeTea = dbhelper.CheckMadeTea(Id);
                                        if (!(CheckMadeTea.getCount() > 0)) {
                                            dbhelper.AddMadeTea(Schedule, Shift, WeighingKit, SortingDate, Grade, Gross, Tare, WeighingTime, Id);
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
                                tMessage = restApiResponse;
                                return;

                            }
                        } catch (JSONException e) {

                            Id = "-1";
                            Title = "";
                            tMessage = _URL + "\n" + e;
                            e.printStackTrace();
                            return;
                        }


                        handler.post(() -> {
                            // do UI changes after background work here

                            if (error.equals("-8080")) {
                                progressDialog.dismiss();

                                Context context = getApplicationContext();
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
                                    Context context = getApplicationContext();
                                    LayoutInflater inflater1 = getLayoutInflater();
                                    View customToastroot = inflater1.inflate(R.layout.white_red_toast, null);
                                    TextView text = customToastroot.findViewById(R.id.toast);
                                    text.setText("Saved Successfully: " + Gross + " Kg");
                                    Toast customtoast = new Toast(context);
                                    customtoast.setView(customToastroot);
                                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                    customtoast.setDuration(Toast.LENGTH_LONG);
                                    // customtoast.show();

                                    tvShowGrossTotal.setVisibility(View.GONE);
                                    tvShowTotalKgs.setVisibility(View.GONE);
                                    tvWeighingTareWeigh.setVisibility(View.GONE);

                                    tvGrossAccepted.setVisibility(View.VISIBLE);
                                    tvGrossAccepted.setText(Gross);
                                    tvTareAccepted.setVisibility(View.VISIBLE);
                                    tvTareAccepted.setText(prefs.getString("tareAcc", ""));
                                    tvNetWeightAccepted.setVisibility(View.VISIBLE);

                                    tvNetWeightAccepted.setText(prefs.getString("Net", ""));
                                    tvWeighingAccumWeigh.setText(df.format(Accum));

                                    lt_accept.setVisibility(View.GONE);
                                    lt_nprint.setVisibility(View.VISIBLE);


                                    tvsavedReading.setText("Saved Reading");
                                    tvSavedNet.setText("Saved Net");
                                    tvSavedTare.setText("Saved Tare");
                                    tvSavedUnits.setText("Saved Count");
                                    tvSavedTotal.setText("Saved Total");

                                    tvsavedReading.setTextColor(getResources().getColor(R.color.colorPinkDark));
                                    tvSavedNet.setTextColor(getResources().getColor(R.color.colorPinkDark));
                                    tvSavedTare.setTextColor(getResources().getColor(R.color.colorPinkDark));
                                    tvSavedUnits.setTextColor(getResources().getColor(R.color.colorPinkDark));
                                    tvSavedTotal.setTextColor(getResources().getColor(R.color.colorPinkDark));
                                    dialogShownOnce = false;
                                    c_refresh.setVisibility(View.GONE);
                                    c_success.setVisibility(View.VISIBLE);
                                    c_error.setVisibility(View.GONE);
                                    return;
                                }
                                if (Integer.parseInt(Id) < 0) {

                                    progressDialog.dismiss();
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putString("error", Id + "\n {" + Title + "} \n" + tMessage);
                                    edit.apply();
                                    Log.e("Error", Id + " {" + Title + "} " + tMessage);

                                    c_refresh.setVisibility(View.GONE);
                                    c_success.setVisibility(View.GONE);
                                    c_error.setVisibility(View.VISIBLE);

                                    Toast.makeText(getApplicationContext(), tMessage, Toast.LENGTH_LONG).show();
                                }

                            } catch (NumberFormatException e) {
                                c_refresh.setVisibility(View.GONE);
                                c_success.setVisibility(View.GONE);
                                c_error.setVisibility(View.VISIBLE);
                                progressDialog.dismiss();
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("error", error);
                                edit.apply();
                                Log.i("RestApiRequest", Id + " {" + Title + "} " + tMessage);


                            }


                        });
                    });


                });
        // Setting Positive "NO" Button
        dialogAccept.setPositiveButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialogShownOnce = false;
                        dialog.cancel();

                    }
                });
        // Showing Alert Message
        dialog_accept = dialogAccept.create();
        if (!dialog_accept.isShowing() && !dialogShownOnce) {
            dialog_accept.show();
            dialogShownOnce = true;
        }
    }


    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case TARE_SCALE:
                    break;
                case ZERO_SCALE:
                    break;
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case NewWeighingService.STATE_CONNECTED:
                            SortingWeighActivity.mProcessDialog.setMessage("Connected to Scale");
                            //Toast.makeText(_ctx.getApplicationContext(), "Connected ...", Toast.LENGTH_SHORT).show();
                            tvScaleConn.setText("Scale Connected");
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putString("scalec", "Scale Connected");
                            edit.apply();
                            if (tvScaleConn.getText().toString().equals("Scale Connected")) {

                                if (prefs.getBoolean("enablePrinting", false)) {

                                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                    if (mBluetoothAdapter == null) {
                                        showUnsupported();
                                    } else {
                                        mConnectingDlg = new ProgressDialog(_ctx.getApplicationContext());

                                        mConnectingDlg.setMessage("Printer Connecting...");
                                        mConnectingDlg.setCancelable(false);

                                        mConnector = new P25Connector(new P25Connector.P25ConnectionListener() {

                                            @Override
                                            public void onStartConnecting() {
                                                //mConnectingDlg.show();
                                                Context context = _ctx.getApplicationContext();
                                                LayoutInflater inflater = _activity.getLayoutInflater();
                                                View customToastroot = inflater.inflate(R.layout.blue_toast, null);
                                                TextView text = customToastroot.findViewById(R.id.toast);
                                                text.setText("Connecting Printer ...");
                                                Toast customtoast = new Toast(context);
                                                customtoast.setView(customToastroot);
                                                customtoast.setGravity(Gravity.BOTTOM, 0, 0);
                                                customtoast.setDuration(Toast.LENGTH_LONG);
                                                customtoast.show();
                                                btnPrinterReconnect.setVisibility(View.GONE);
                                                // Toast.makeText(_ctx.getApplicationContext(), "Connecting Printer ...", Toast.LENGTH_LONG).show();
                                            }

                                            @Override
                                            public void onConnectionSuccess() {
                                                // mConnectingDlg.dismiss();
                                                printconn = 1;
                                                // showConnected();
                                                tvPrinterConn.setVisibility(View.VISIBLE);
                                                btnPrinterReconnect.setVisibility(View.GONE);
                                                if (preconnection == 2) {
                                                    btn_print.setVisibility(View.VISIBLE);
                                                    btn_reconnect.setVisibility(View.GONE);
                                                }

                                                SharedPreferences.Editor edit = prefs.edit();
                                                edit.putString("printerc", "Printer Connected");
                                                edit.apply();
                                            }

                                            @Override
                                            public void onConnectionFailed(String error) {
                                                // mConnectingDlg.dismiss();
                                                printconn = 0;

                                                btnPrinterReconnect.setVisibility(View.VISIBLE);
                                                tvPrinterConn.setVisibility(View.GONE);

                                            }

                                            @Override
                                            public void onConnectionCancelled() {

                                                //  mConnectingDlg.dismiss();
                                                printconn = 0;
                                                btnPrinterReconnect.setVisibility(View.VISIBLE);
                                                tvPrinterConn.setVisibility(View.GONE);

                                            }

                                            @Override
                                            public void onDisconnected() {
                                                printconn = 0;
                                                // showDisonnected();

                                                Context context = _ctx.getApplicationContext();
                                                LayoutInflater inflater = _activity.getLayoutInflater();
                                                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                                TextView text = customToastroot.findViewById(R.id.toast);
                                                text.setText("Printer Disconnected");
                                                Toast customtoast = new Toast(context);
                                                customtoast.setView(customToastroot);
                                                customtoast.setGravity(Gravity.BOTTOM, 0, 0);
                                                customtoast.setDuration(Toast.LENGTH_LONG);
                                                customtoast.show();
                                                tvPrinterConn.setVisibility(View.GONE);
                                                SharedPreferences.Editor edit = prefs.edit();
                                                edit.putString("printerc", "Printer Disconnected");
                                                edit.apply();

                                            }
                                        });
                                        connect();
                                        IntentFilter filter = new IntentFilter();

                                        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                                        filter.addAction(BluetoothDevice.ACTION_FOUND);
                                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                                        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                                        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                                        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                                        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

                                        _activity.registerReceiver(printerReceiver, filter);
                                    }
                                }

                            }


                            break;
                        case NewWeighingService.STATE_CONNECTING:
                            mProcessDialog.setMessage("Attempting Connection to scale");

                            //Toast.makeText(getApplicationContext(), "Connecting ...", Toast.LENGTH_SHORT).show();
                            break;
                        case NewWeighingService.STATE_LISTEN:
                        case NewWeighingService.STATE_NONE:
                            break;
                    }
                    break;
                case REQUEST_DEVICEADDRESS: //Device Address Not found call DeviceListActivity
                    if (mProcessDialog != null && mProcessDialog.isShowing()) {
                        mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
                    }

                    _activity.finish();
                    Intent intentDeviceList = new Intent(_ctx.getApplicationContext(), DeviceListActivity.class);
                    _activity.startActivityForResult(intentDeviceList, 1);

                    break;
                case MESSAGE_READ:
                    try {
                        tvScaleConn.setText("Scale Connected");
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("scalec", "Scale Connected");
                        edit.apply();


                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);

                        Log.i(TAG, "Returned Message" + readMessage);


                        //Convert message to ascii byte array
                        byte[] messageBytes = stringToBytesASCII(readMessage);

                        String thisWeighment = "";
                        if (prefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15)) {
                            thisWeighment = getReading(messageBytes, readMessage);

                        } else {
                            thisWeighment = getReading(messageBytes);
                        }

                        final DecimalFormat df = new DecimalFormat("#0.0#");
                        final DecimalFormat f = new DecimalFormat("#.#");
                        double myDouble;

                        //thisWeighment = newFormatReading[0]; //overriding weighment
                        //tareWeight = Double.parseDouble(newFormatReading[1]);

                        Log.i(TAG, "New Format Reading is " + thisWeighment);
                        Log.i(TAG, "Weighment is " + thisWeighment);
                        Log.i(TAG, "Tare Weight is " + tareWeight);

                        if (thisWeighment != null && !thisWeighment.isEmpty()) {
                            try {
                                myDouble = Double.parseDouble(thisWeighment);
                            } catch (NumberFormatException e) {
                                Log.i(TAG, "thisWeighment " + thisWeighment);
                                myDouble = Double.parseDouble(thisWeighment.replace("W  ", ""));
                                Log.i(TAG, "NumberFormatException " + e.getMessage() + " " + myDouble);

                            }

                            GROSS_KG = myDouble;
                            tvStreamingReading.setText(df.format(GROSS_KG));

                            prefs = PreferenceManager.getDefaultSharedPreferences(_ctx.getApplicationContext());

                            int WeightCount = prefs.getInt("WeightCount", 0);
                            if (WeightCount <= Integer.parseInt(stableReadingCounter)) {

                                weighments[WeightCount] = Double.parseDouble(df.format(GROSS_KG));

                                Log.i("WeightCount", "Weight[" + WeightCount + "]: " + weighments[WeightCount]);

                                if (WeightCount == (Integer.parseInt(stableReadingCounter) - 1)) {

                                    Log.i("WeightCount", "List:" + Arrays.toString(weighments));

                                }
                            }


                            if (myDouble == 100.0) {

                                Context context = _ctx.getApplicationContext();
                                LayoutInflater inflater = _activity.getLayoutInflater();
                                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                TextView text = customToastroot.findViewById(R.id.toast);
                                text.setText("OVER WEIGHT");
                                Toast customtoast = new Toast(context);
                                customtoast.setView(customToastroot);
                                customtoast.setGravity(Gravity.TOP, 0, 0);
                                customtoast.setDuration(Toast.LENGTH_SHORT);
                                //  customtoast.show();
                                tvWeighingTareWeigh.setText("-");
                                tvShowGrossTotal.setText("-");
                                tvShowTotalKgs.setText("-");
                                tvError.setVisibility(View.VISIBLE);
                                tvStability.setVisibility(View.GONE);
                                btn_accept.setEnabled(false);

                                return;
                            } else {

                                tvError.setVisibility(View.GONE);
                                btn_accept.setEnabled(true);

                            }

                            edit.putString("tvGross", df.format(myDouble));
                            edit.apply();

                            tvShowGrossTotal.setText(df.format(myDouble));
                            setMoisture = Double.parseDouble(prefs.getString("moisture", "0.0"));

                            if (prefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15) || prefs.getString("scaleVersion", "EW11").equals(EASYWEIGH_VERSION_11)) {
                                totalMoisture = Double.parseDouble(f.format((setMoisture / 100) * myDouble));
                                tvWeighingTareWeigh.setText(df.format(totalMoisture + tareWeight));
                                netWeight = myDouble - totalMoisture;

                            } else {
                                setTareWeight = Double.parseDouble(prefs.getString("tareWeight", "0.0"));
                                totalMoisture = Double.parseDouble(f.format((setMoisture / 100) * myDouble)) + setTareWeight;
                                tvWeighingTareWeigh.setText(df.format(totalMoisture));
                                netWeight = myDouble - totalMoisture;
                                df.format(netWeight);
                            }

                            if (netWeight <= 0.0) {
                                edit.putString("tvNetWeight", "0.0");
                                edit.apply();
                                tvShowTotalKgs.setText("0.0");
                                tvStability.setVisibility(View.GONE);
                            } else {
                                edit.putString("tvNetWeight", df.format(netWeight));
                                edit.apply();
                                tvShowTotalKgs.setText(df.format(netWeight));
                            }


                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }

                    break;
                case READING_PROBE:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    //Convert message to ascii byte array
                    byte[] messageBytes = stringToBytesASCII(readMessage);

                    String thisWeighment = "";
                    if (prefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15)) {
                        thisWeighment = getReading(messageBytes, readMessage);
                    } else {
                        thisWeighment = getReading(messageBytes);
                    }

                    if (!thisWeighment.equals("0.0")) {
                        //resend tare command

                        Message msg2 = Message.obtain(null, NewWeighingService.TARE_SCALE);
                        Message msg3 = Message.obtain(null, NewWeighingService.ZERO_SCALE);

                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    mProcessDialog.setMessage("Connected to " + mConnectedDeviceName);

                    if (mProcessDialog != null && mProcessDialog.isShowing()) {
                        mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
                    }

                    // now send R for 3 seconds

                    try {
                        Message msg2 = Message.obtain(null, NewWeighingService.INIT_WEIGHING);
                        mWeighingService.send(msg2);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                case MESSAGE_TOAST:
                    //mProcessDialog.setMessage("Unable To Connect to Device");
                    if (mProcessDialog == null) {
                        mProcessDialog = new ProgressDialog(_ctx.getApplicationContext());
                    }

                    mProcessDialog.setMessage(msg.getData().getString(TOAST));

                    if (mProcessDialog != null && mProcessDialog.isShowing()) {
                        mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
                    }
                    if (msg.getData().getString(TOAST).equals("Unable to connect scale")) {
                        Context context = _ctx.getApplicationContext();
                        LayoutInflater inflater = _activity.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText(msg.getData().getString(TOAST));
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        if (mBluetoothAdapter != null) {
                            if (mBluetoothAdapter.isDiscovering()) {
                                mBluetoothAdapter.cancelDiscovery();
                            }
                        }

                        if (mConnector != null) {
                            try {
                                mConnector.disconnect();
                            } catch (P25ConnectionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (msg.getData().getString(TOAST).equals("Scale Disconnected")) {
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("scalec", "Scale Disconnected");
                        edit.apply();
                        Context context = _ctx.getApplicationContext();
                        LayoutInflater inflater = _activity.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                        TextView text = customToastroot.findViewById(R.id.toast);
                        text.setText(msg.getData().getString(TOAST));
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        if (mBluetoothAdapter != null) {
                            if (mBluetoothAdapter.isDiscovering()) {
                                mBluetoothAdapter.cancelDiscovery();
                            }
                        }

                        if (mConnector != null) {
                            try {
                                mConnector.disconnect();
                            } catch (P25ConnectionException e) {
                                e.printStackTrace();
                            }
                        }
                        tvScaleConn.setVisibility(View.GONE);
                        // SessionSave();

                    }
                    // mProcessDialog.show();
                    // Toast.makeText(_ctx.getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_LONG).show();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(1500);
                                return;
                            } catch (InterruptedException localInterruptedException) {
                                localInterruptedException.printStackTrace();
                                return;
                            } finally {
                                _activity.finish();

                            }
                        }
                    }
                            .start();
                    break;
                case COMPLETE_FAILURE:
                    //Something is terribly wrong
                    Toast.makeText(_ctx.getApplicationContext(), "Something is terribly Wrong", Toast.LENGTH_SHORT).show();
                    _activity.finish();
                    break;
            }
        }
    };

    private static final Messenger mMessenger = new Messenger(mHandler);

    private static void showToast(String message) {
        Toast.makeText(_ctx.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private static void showUnsupported() {
        showToast("Bluetooth is unsupported by this device");
    }

    private static void connect() {

        //BluetoothDevice device = mDeviceList.get(mDeviceSp.getSelectedItemPosition());
        String mDevice = prefs.getString("mDevice", "");
        // String mDevice ="00:11:22:33:44:55";
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDevice);

        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            try {
                createBond(device);
            } catch (Exception e) {
                showToast("Failed to pair device");

                return;
            }
        }

        try {
            if (!mConnector.isConnected()) {
                mConnector.connect(device);
            } else {
                mConnector.disconnect();

                //showDisonnected();
            }
        } catch (P25ConnectionException e) {
            e.printStackTrace();
        }
    }

    private static void createBond(BluetoothDevice device) throws Exception {

        try {
            Class<?> cl = Class.forName("android.bluetooth.BluetoothDevice");
            Class<?>[] par = {};

            Method method = cl.getMethod("createBond", par);

            method.invoke(device);

        } catch (Exception e) {
            e.printStackTrace();

            throw e;
        }
    }

    private void sendData(byte[] bytes) {
        try {
            if (mConnector.isConnected()) {
                mConnector.sendData(bytes);
            } else {
                printconn = 0;
            }
        } catch (P25ConnectionException e) {
            e.printStackTrace();
        }
    }

    private static final BroadcastReceiver printerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED) {
                    showToast("Paired");

                    connect();
                }
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                printconn = 1;

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                printconn = 0;
                btnPrinterReconnect.setVisibility(View.VISIBLE);
                tvPrinterConn.setVisibility(View.GONE);

            }
        }
    };


    public static byte[] stringToBytesASCII(String str) {
        byte[] b = new byte[str.length()];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) str.charAt(i);
        }
        return b;
    }

    static String getReading(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length);
        for (int i = 0; i < data.length; ++i) {
            if (data[i] < 6) {
                throw new IllegalArgumentException();
            } else if (data[i] >= 46 && data[i] <= 57) {
                sb.append((char) data[i]); //I believe this is an accurate reading
            }
        }
        //return sb.toString();
        return sb.toString().replaceAll("I", "");
    }

    static String getReading(byte[] data, String message) {
        String returnValue = "";
        try {
            for (int i = 0; i < data.length; ++i) {
                if (message.length() >= 10 && message.length() <= 11) {
                    //   throw new IllegalArgumentException();

                } else if (data[i] >= 46 && data[i] <= 57) {
                    String[] parts = message.trim().split(",");

                    returnValue = parts[0];
                    if (parts.length == 2) {
                        tareWeight = Double.parseDouble(parts[1]);
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    // Sets up communication with {@link NewWeighingService}
    private static final ServiceConnection scaleConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mWeighingServiceBound = true;

            Bundle myBundle = new Bundle();
            myBundle.putInt(DEVICE_TYPE, 1);

            Message msg = Message.obtain(null, NewWeighingService.MSG_REG_CLIENT);

            msg.setData(myBundle);

            msg.replyTo = mMessenger;

            mWeighingService = new Messenger(service);

            try {
                mWeighingService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to register client to service.");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWeighingService = null;
            mWeighingServiceBound = false;
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (prefs.getBoolean("enablePrinting", false)) {

            if (mConnector != null) {

                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                _activity.registerReceiver(printerReceiver, filter);

                if (printconn == 0) {
                    tvPrinterConn.setVisibility(View.GONE);
                    btnPrinterReconnect.setVisibility(View.VISIBLE);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("printerc", "Printer Disconnected");
                    edit.apply();
                } else {
                    tvPrinterConn.setVisibility(View.VISIBLE);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("printerc", "Printer Connected");
                    edit.apply();
                    btnPrinterReconnect.setVisibility(View.GONE);
                }
            }

        }

        setMoisture = Double.parseDouble(prefs.getString("moisture", "0.0"));

        if (mWeighingServiceBound) {
            Message msg = Message.obtain(null, NewWeighingService.READING_PROBE);
            try {
                mWeighingService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        } else {
            initialize();
        }
    }

    public static void initialize() {
        Log.d(TAG, "setup Service()");

        try {
            Intent intent = new Intent(_ctx, NewWeighingService.class);

            Bundle myBundle = new Bundle();
            Log.i(TAG, "Am Passing this address to Service " + cachedDeviceAddress);
            myBundle.putString(EXTRA_DEVICE_ADDRESS, cachedDeviceAddress);

            Log.i(TAG, "Scale Version " + prefs.getString("scaleVersion", "EW15"));
            //get scale version
            if (prefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15)) {
                myBundle.putString(SCALE_VERSION, EASYWEIGH_VERSION_15);
            } else if (prefs.getString("scaleVersion", "EW11").equals(EASYWEIGH_VERSION_11)) {
                myBundle.putString(SCALE_VERSION, EASYWEIGH_VERSION_11);
            } else if (prefs.getString("scaleVersion", "DR-150").equals(DR_150)) {
                myBundle.putString(SCALE_VERSION, DR_150);
            }

            intent.putExtras(myBundle); //add Bundle to intent

            _ctx.startService(intent);

            _ctx.bindService(intent, scaleConnection, Context.BIND_AUTO_CREATE);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    // Intent filter and broadcast receive to handle Bluetooth on event.
    private IntentFilter initIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    private final BroadcastReceiver scaleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_ON) {
                    initialize();
                }
            }
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    cachedDeviceAddress = prefs.getString("scale_address", "");

                    if (!cachedDeviceAddress.equals("")) {

                        //Send Message to Servive with new address

                        Message msg = Message.obtain(null, NewWeighingService.RETRY);
                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_DEVICE_ADDRESS, cachedDeviceAddress);

                        try {
                            msg.setData(bundle);
                            mWeighingService.send(msg);
                        } catch (RemoteException e) {
                            Log.w(TAG, "Unable to register client to service.");
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                try {
                    // When the request to enable Bluetooth returns
                    if (resultCode == Activity.RESULT_OK) {
                        // Bluetooth is now enabled, so set up a chat session
                        //initialize();
                        cachedDeviceAddress = prefs.getString("scale_address", "");

                        Message msg = Message.obtain(null, NewWeighingService.RETRY);
                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_DEVICE_ADDRESS, cachedDeviceAddress);

                        if (cachedDeviceAddress != null || !cachedDeviceAddress.isEmpty()) { //first check we have an address
                            try {
                                mProcessDialog.setMessage("Attempting Connection ...");
                                mProcessDialog.show();
                                msg.setData(bundle);
                                mWeighingService.send(msg);
                            } catch (RemoteException e) {
                                Log.w(TAG, "Unable to register client to service.");
                                e.printStackTrace();
                            }
                        } else { //Try Each and every other address in DB
                            mProcessDialog.setMessage("Unable to connect to Default Device");
                            SortingWeighActivity.this.finish();
                            Intent intentDeviceList = new Intent(getApplicationContext(), DeviceListActivity.class);
                            startActivityForResult(intentDeviceList, 1);
                        }

                    } else {
                        // User did not enable Bluetooth or an error occurred
                        Log.d(TAG, "BT not enabled");
                        Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                        //mProcessDialog.dismiss();
                        finish();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "After Request BT " + e);
                }

                break;
        }
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind from WeighingActivity service and Unregister receiver
        try {
            if (mWeighingServiceBound) {
                unbindService(scaleConnection);
                unregisterReceiver(scaleReceiver);
                unregisterReceiver(printerReceiver);
                if (mProcessDialog != null && mProcessDialog.isShowing()) {
                    mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {

        finish();

        super.onBackPressed();
    }


}