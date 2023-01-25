package com.easyway.pos.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.easyway.pos.R;
import com.easyway.pos.connector.P25ConnectionException;
import com.easyway.pos.connector.P25Connector;
import com.easyway.pos.printerdata.PocketPos;
import com.easyway.pos.printerutils.DataConstants;
import com.easyway.pos.printerutils.DateUtil;
import com.easyway.pos.printerutils.FontDefine;
import com.easyway.pos.printerutils.Printer;
import com.easyway.pos.printerutils.Util;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

/**
 * Demo Blue Bamboo P25 Thermal Printer.
 *
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 */
public class PrintRecieptActivity extends Activity {
    private Button mConnectBtn;
    private Button mEnableBtn;
    private Button mPrintDemoBtn;
    private Button mPrintBarcodeBtn;
    private Button mPrintImageBtn;
    private Button mPrintReceiptBtn;
    private Button mPrintTextBtn;
    private Spinner mDeviceSp;
    private ProgressDialog mProgressDlg;
    private ProgressDialog mConnectingDlg;
    private BluetoothAdapter mBluetoothAdapter;
    private P25Connector mConnector;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    BluetoothDevice mDevice;
    Intent mIntent;
    SharedPreferences prefs;

    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";
    public static final String WEIGH_AND_TARE = "Discrete";
    public static final String FILLING = "Incremental";
    public static final String MANUAL = "Manual";
    private Snackbar snackbar;
    static SharedPreferences mSharedPrefs;
    String FarmerNo, MobileNo, TotalKg, NetWeight, RecieptNo, Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_printer);
        prefs = PreferenceManager.getDefaultSharedPreferences(PrintRecieptActivity.this);
        String mDevice = prefs.getString("mDevice", "");
        //showToast(mDevice);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mConnectBtn = findViewById(R.id.btn_connect);
        mEnableBtn = findViewById(R.id.btn_enable);
        mPrintDemoBtn = findViewById(R.id.btn_print_demo);
        mPrintBarcodeBtn = findViewById(R.id.btn_print_barcode);
        mPrintImageBtn = findViewById(R.id.btn_print_image);
        mPrintReceiptBtn = findViewById(R.id.btn_print_receipt);
        mPrintTextBtn = findViewById(R.id.btn_print_text);
        mDeviceSp = findViewById(R.id.sp_device);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        mDeviceSp.setVisibility(View.GONE);
        mPrintDemoBtn.setVisibility(View.GONE);
        mPrintBarcodeBtn.setVisibility(View.GONE);
        mPrintImageBtn.setVisibility(View.GONE);
        mConnectBtn.setVisibility(View.GONE);
        if (mSharedPrefs.getString("scaleVersion", "").equals(MANUAL)) {
            //enableBT();
        }
        if (mBluetoothAdapter == null) {
            showUnsupported();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                showDisabled();
            } else {
                //showEnabled();

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                if (pairedDevices != null) {
                    mDeviceList.addAll(pairedDevices);

                    updateDeviceList();
                }
            }


            mProgressDlg = new ProgressDialog(this);

            mProgressDlg.setMessage("Scanning...");
            mProgressDlg.setCancelable(false);
            mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    mBluetoothAdapter.cancelDiscovery();
                }
            });

            mConnectingDlg = new ProgressDialog(this);

            mConnectingDlg.setMessage("Printer Connecting...");
            mConnectingDlg.setCancelable(false);

            mConnector = new P25Connector(new P25Connector.P25ConnectionListener() {

                @Override
                public void onStartConnecting() {
                    mConnectingDlg.show();
                }

                @Override
                public void onConnectionSuccess() {
                    mConnectingDlg.dismiss();

                    showConnected();
                }


                @Override
                public void onConnectionFailed(String error) {
                    mConnectingDlg.dismiss();
                }

                @Override
                public void onConnectionCancelled() {

                    mConnectingDlg.dismiss();
                }

                @Override
                public void onDisconnected() {

                    showDisonnected();
                }
            });
            connect();
            //enable bluetooth
            mEnableBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                    startActivityForResult(intent, 1000);
                }
            });

            //connect/disconnect
            mConnectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    connect();
                }
            });

            //print demo text
            mPrintDemoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //printDemoContent();

                }
            });

            //print text
            mPrintTextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
					/*InputTextDialog dialog = new InputTextDialog(PrintRecieptActivity.this, new InputTextDialog.OnOkClickListener() {
						@Override
						public void onPrintClick(String text) {
							printText(text);
						}
					});

					dialog.show();*/
                    SMS();
                }
            });

            //print image bitmap
            mPrintImageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    //printImage();
                }
            });

            //print barcode 1D or 2D
            mPrintBarcodeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //printBarcode();
                }
            });

            //print struk
            mPrintReceiptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    printSingleReceipt();
                }
            });
        }

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(mReceiver, filter);


    }

    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scandevices, menu);
        MenuItem item = menu.findItem(R.id.action_scan);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_scan) {
            mBluetoothAdapter.startDiscovery();
        }
        if (item.getItemId() == R.id.action_connect) {
            connect();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPause() {
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

        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    private String[] getArray(ArrayList<BluetoothDevice> data) {
        String[] list = new String[0];

        if (data == null) return list;

        int size = data.size();
        list = new String[size];

        for (int i = 0; i < size; i++) {
            list[i] = data.get(i).getName();
        }

        return list;
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void updateDeviceList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, getArray(mDeviceList));

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

        mDeviceSp.setAdapter(adapter);
        mDeviceSp.setSelection(0);
    }

    private void showDisabled() {
        showToast("Bluetooth disabled");

        mEnableBtn.setVisibility(View.VISIBLE);
        mConnectBtn.setVisibility(View.GONE);
        mDeviceSp.setVisibility(View.GONE);
    }

    private void showEnabled() {
        showToast("Bluetooth enabled");

        mEnableBtn.setVisibility(View.GONE);
        mConnectBtn.setVisibility(View.VISIBLE);
        mDeviceSp.setVisibility(View.VISIBLE);
    }

    private void showUnsupported() {
        showToast("Bluetooth is unsupported by this device");

        mConnectBtn.setEnabled(false);
        mPrintDemoBtn.setEnabled(false);
        mPrintBarcodeBtn.setEnabled(false);
        mPrintImageBtn.setEnabled(false);
        mPrintReceiptBtn.setEnabled(false);
        mPrintTextBtn.setEnabled(false);
        mDeviceSp.setEnabled(false);
    }

    private void showConnected() {
        showToast("Printer Connected");

        mConnectBtn.setText("Disconnect");
        mDeviceSp.setVisibility(View.GONE);
        mPrintDemoBtn.setEnabled(true);
        mPrintDemoBtn.setVisibility(View.GONE);
        mPrintBarcodeBtn.setEnabled(true);
        mPrintBarcodeBtn.setVisibility(View.GONE);

        mPrintImageBtn.setEnabled(true);
        mPrintImageBtn.setVisibility(View.GONE);

        mPrintReceiptBtn.setEnabled(true);
        mConnectBtn.setVisibility(View.GONE);
        mPrintTextBtn.setEnabled(true);
        mPrintTextBtn.setText("Send SMS");

        mDeviceSp.setEnabled(false);
    }

    private void showDisonnected() {
        showToast("Printer Disconnected");

        mConnectBtn.setText("Reconnect");
        mConnectBtn.setVisibility(View.VISIBLE);
        mPrintDemoBtn.setEnabled(false);
        mPrintBarcodeBtn.setEnabled(false);
        mPrintImageBtn.setEnabled(false);
        mPrintReceiptBtn.setEnabled(false);
        mPrintTextBtn.setEnabled(false);

        mDeviceSp.setEnabled(true);
    }

    private void connect() {
        if (mDeviceList == null || mDeviceList.size() == 0) {
            return;
        }


        ///BluetoothDevice device = mDeviceList.get(mDeviceSp.getSelectedItemPosition());
        String mDevice = prefs.getString("mDevice", "");
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

                showDisonnected();
            }
        } catch (P25ConnectionException e) {
            e.printStackTrace();
        }
    }

    private void createBond(BluetoothDevice device) throws Exception {

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
            mConnector.sendData(bytes);
        } catch (P25ConnectionException e) {
            e.printStackTrace();
        }
    }


    private void printSingleReceipt() {
        long milis = System.currentTimeMillis();
        String date = DateUtil.timeMilisToString(milis, "MMM dd, yyyy");
        String time = DateUtil.timeMilisToString(milis, "hh:mm a");
        String titleStr = "\n*** RECEIPT SLIP ***" + "\n"
                + mSharedPrefs.getString("company_name", "") + "\n"
                + "P.O. Box " + mSharedPrefs.getString("company_letterbox", "") + "-" + mSharedPrefs.getString("company_postalcode", "") + ", " +
                mSharedPrefs.getString("company_postalname", "") + "\n" +
                Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n";

        StringBuilder contentSb = new StringBuilder();

        // String Name = prefs.getString("textName", "").substring(0,50);
        //contentSb.append("  \n\n" + mSharedPrefs.getString("company_name", "").toString() + "\r\n");
        //contentSb.append("   " +"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
        // mSharedPrefs.getString("company_postalname", "").toString() + "\r\n");
        //contentSb.append("  " + msg.getData().getString(FarmerScaleWeighActivity.ADDRESS_LINE2) + "\r\n");

        contentSb.append("  \n-----------------------------\n");

        contentSb.append("  RECEIPT    : " + prefs.getString("textReciept", "") + "\n");
        contentSb.append("  DATE       : " + prefs.getString("textTransDate", "") + "\n");
        contentSb.append("  TIME       : " + prefs.getString("textTransTime", "") + "\n");
        contentSb.append("  TERMINAL   : " + prefs.getString("textTerminal", "") + "\n");
        contentSb.append("  FARMER NO  : " + prefs.getString("textFarmerNo", "") + "\n");
        contentSb.append("  NAME       : " + prefs.getString("textName", "") + "\n");
        contentSb.append("  ROUTE      : " + prefs.getString("textRoute", "") + "\n");
        contentSb.append("  SHED       : " + prefs.getString("textShed", "") + "\n");
        contentSb.append("  TRIP       : " + prefs.getString("textTrip", "") + "\n");
        contentSb.append("  -----------------------------\n");
        //contentSb.append("  CAN        : " + msg.getData().getString(FarmerScaleWeighActivity.CAN) + "\r\n");
        contentSb.append("  BAGS       : " + prefs.getString("textBags", "") + "\n");
        contentSb.append("  GROSS WT   : " + prefs.getString("textGrossWt", "") + "\n");
        contentSb.append("  TARE WT    : " + prefs.getString("textTareWt", "") + "\n");
        contentSb.append("  NET WT     : " + prefs.getString("textNetWt", "") + "\n");
        contentSb.append("  TOTAL KGS  : " + prefs.getString("textTotalKgs", "") + "\n");
        //contentSb.append("  UNIT PRICE : " + prefs.getString("mDevice", "") + "\r\n");
        //contentSb.append("  AMOUNT     : " + prefs.getString("mDevice", "") + "\r\n");
        contentSb.append("  -----------------------------\n");
        contentSb.append("  You were served by " + prefs.getString("user", "") + "\n");
        // contentSb.append("  PRINT DATE : "+Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n");
        contentSb.append("  " + Util.nameLeftValueRightJustify("www.octagon.co.ke ", " 25471855111", DataConstants.RECEIPT_WIDTH) + "\n");
        contentSb.append("\n");
        contentSb.append("\n");


        byte[] titleByte = Printer.printfont(titleStr, FontDefine.FONT_32PX, FontDefine.Align_CENTER,
                (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

        byte[] content1Byte = Printer.printfont(contentSb.toString(), FontDefine.FONT_32PX, FontDefine.Align_LEFT,
                (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);


        byte[] totalByte = new byte[titleByte.length + content1Byte.length];

        int offset = 0;
        System.arraycopy(titleByte, 0, totalByte, offset, titleByte.length);
        offset += titleByte.length;

        System.arraycopy(content1Byte, 0, totalByte, offset, content1Byte.length);
        offset += content1Byte.length;

        byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totalByte, 0, totalByte.length);

        sendData(senddata);
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    //showEnabled();
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    showDisabled();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();

                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();

                updateDeviceList();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device);

                showToast("Found device " + device.getName());
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED) {
                    showToast("Paired");

                    connect();
                }
            }
        }
    };

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        ///FarmerScaleWeighActivity.initialize();
        //return;

        //unregisterReceiver(mReceiver);


    }

    public void SMS() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Send Sms?");
        dialogBuilder.setMessage("Do you want to send an SMS receipt?");
        dialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                RecieptNo = prefs.getString("textReciept", "");
                MobileNo = prefs.getString("rFarmerPhone", "");
                FarmerNo = prefs.getString("textFarmerNo", "");
                NetWeight = prefs.getString("textNetWt", "");
                TotalKg = prefs.getString("textTotalKgs", "");
                Date = prefs.getString("textTransDate", "");

                if (MobileNo.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Update Mobile Number", Toast.LENGTH_LONG).show();
                    return;
                }
                if (FarmerNo.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Update FarmerNo", Toast.LENGTH_LONG).show();
                    return;
                }
                Sendsms(MobileNo,
                        "ReceiptNo:" + RecieptNo +
                                "\n" + "FarmerNo:" + FarmerNo +
                                "\n" + "Collected: " + NetWeight + " Kgs." +
                                "\n" + "Total Kgs: " + TotalKg + " Kgs." +
                                "\n" + "On Date " + Date +
                                "\n" + "Thank you!!");
                Toast.makeText(getBaseContext(), "Message Sent Successfully!!", Toast.LENGTH_LONG).show();

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();

    }

    private void Sendsms(String address, String message) {
        // TODO Auto-generated method stub
        SmsManager smsMgr = SmsManager.getDefault();
        smsMgr.sendTextMessage(address, null, message, null, null);

    }
}