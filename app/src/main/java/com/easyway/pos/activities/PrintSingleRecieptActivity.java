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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.easyway.pos.printerutils.StringUtil;
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
public class PrintSingleRecieptActivity extends Activity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_printer);
        prefs = PreferenceManager.getDefaultSharedPreferences(PrintSingleRecieptActivity.this);
        String mDevice = prefs.getString("mDevice", "");
        showToast(mDevice);

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
        if (mSharedPrefs.getString("scaleVersion", "").equals(MANUAL)) {
            //enableBT();
        }
        if (mBluetoothAdapter == null) {
            showUnsupported();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                showDisabled();
            } else {
                showEnabled();

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

            mConnectingDlg.setMessage("Connecting...");
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
                    printDemoContent();

                }
            });

            //print text
            mPrintTextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    InputTextDialog dialog = new InputTextDialog(PrintSingleRecieptActivity.this, new InputTextDialog.OnOkClickListener() {
                        @Override
                        public void onPrintClick(String text) {
                            printText(text);
                        }
                    });

                    dialog.show();
                }
            });

            //print image bitmap
            mPrintImageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    printImage();
                }
            });

            //print barcode 1D or 2D
            mPrintBarcodeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    printBarcode();
                }
            });

            //print struk
            mPrintReceiptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    printStruk();
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_scan) {
            mBluetoothAdapter.startDiscovery();
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
        showToast("Connected");

        mConnectBtn.setText("Disconnect");

        mPrintDemoBtn.setEnabled(true);
        mPrintBarcodeBtn.setEnabled(true);
        mPrintImageBtn.setEnabled(true);
        mPrintReceiptBtn.setEnabled(true);
        mPrintTextBtn.setEnabled(true);

        mDeviceSp.setEnabled(false);
    }

    private void showDisonnected() {
        showToast("Disconnected");

        mConnectBtn.setText("Connect");

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

    private void printStruk() {


        StringBuilder contentSb = new StringBuilder();


        contentSb.append("  " + mSharedPrefs.getString("company_name", "") + "\r\n\n");
        contentSb.append("   " + "P.O. Box " + mSharedPrefs.getString("company_letterbox", "") + "-" + mSharedPrefs.getString("company_postalcode", "") + ", " +
                mSharedPrefs.getString("company_postalname", "") + "\r\n");
        //contentSb.append("  " + msg.getData().getString(FarmerScaleWeighActivity.ADDRESS_LINE2) + "\r\n");

        contentSb.append("  --------------------------------------\r\n");

        contentSb.append("  RECEIPT    : " + prefs.getString("textReciept", "") + "\r\n");
        contentSb.append("  DATE       : " + prefs.getString("textTransDate", "") + "\r\n");
        contentSb.append("  TIME       : " + prefs.getString("textTransTime", "") + "\r\n");
        contentSb.append("  TERMINAL   : " + prefs.getString("textTerminal", "") + "\r\n");
        contentSb.append("  FARMER NO  : " + prefs.getString("textFarmerNo", "") + "\r\n");
        contentSb.append("  NAME       : " + prefs.getString("textName", "") + "\r\n");
        contentSb.append("  ROUTE      : " + prefs.getString("textRoute", "") + "\r\n");
        contentSb.append("  SHED       : " + prefs.getString("textShed", "") + "\r\n");
        contentSb.append("  TRIP       : " + prefs.getString("textTrip", "") + "\r\n");
        contentSb.append("  --------------------------------------\r\n");
        //contentSb.append("  CAN        : " + msg.getData().getString(FarmerScaleWeighActivity.CAN) + "\r\n");
        contentSb.append("  GROSS WT   : " + prefs.getString("textGrossWt", "") + "\r\n");
        contentSb.append("  TARE WT    : " + prefs.getString("textTareWt", "") + "\r\n");
        contentSb.append("  NET WT     : " + prefs.getString("textNetWt", "") + "\r\n");
        contentSb.append("  TOTAL KGS  : " + prefs.getString("textTotalKgs", "") + "\r\n");
        //contentSb.append("  UNIT PRICE : " + prefs.getString("mDevice", "") + "\r\n");
        //contentSb.append("  AMOUNT     : " + prefs.getString("mDevice", "") + "\r\n");
        contentSb.append("  --------------------------------------\r\n");
        contentSb.append("  CLERK NAME : " + prefs.getString("user", "") + "\r\n");
        contentSb.append("\r\n");
        contentSb.append("\r\n");

        long milis = System.currentTimeMillis();
        String date = DateUtil.timeMilisToString(milis, " dd-MM-yyyy / HH:mm") + "\n\n";


        byte[] content1Byte = Printer.printfont(contentSb.toString(), FontDefine.FONT_24PX, FontDefine.Align_LEFT,
                (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);


        byte[] dateByte = Printer.printfont(date, FontDefine.FONT_24PX, FontDefine.Align_LEFT, (byte) 0x1A,
                PocketPos.LANGUAGE_ENGLISH);


        byte[] totalByte = new byte[content1Byte.length + dateByte.length];

        int offset = 0;


        System.arraycopy(content1Byte, 0, totalByte, offset, content1Byte.length);
        offset += content1Byte.length;
        System.arraycopy(dateByte, 0, totalByte, offset, dateByte.length);
        offset += dateByte.length;

        byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totalByte, 0, totalByte.length);

        sendData(senddata);
    }

    private void printDemoContent() {

        /*********** print head*******/
        String receiptHead = "************************"
                + "   P25/M Test Print" + "\n"
                + "************************"
                + "\n";

        long milis = System.currentTimeMillis();

        String date = DateUtil.timeMilisToString(milis, "MMM dd, yyyy");
        String time = DateUtil.timeMilisToString(milis, "hh:mm a");

        String hwDevice = Build.MANUFACTURER;
        String hwModel = Build.MODEL;
        String osVer = Build.VERSION.RELEASE;
        String sdkVer = String.valueOf(Build.VERSION.SDK_INT);

        StringBuffer receiptHeadBuffer = new StringBuffer(100);

        receiptHeadBuffer.append(receiptHead);
        receiptHeadBuffer.append(Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n");

        receiptHeadBuffer.append(Util.nameLeftValueRightJustify("Device:", hwDevice, DataConstants.RECEIPT_WIDTH) + "\n");

        receiptHeadBuffer.append(Util.nameLeftValueRightJustify("Model:", hwModel, DataConstants.RECEIPT_WIDTH) + "\n");
        receiptHeadBuffer.append(Util.nameLeftValueRightJustify("OS ver:", osVer, DataConstants.RECEIPT_WIDTH) + "\n");
        receiptHeadBuffer.append(Util.nameLeftValueRightJustify("SDK:", sdkVer, DataConstants.RECEIPT_WIDTH));
        receiptHead = receiptHeadBuffer.toString();

        byte[] header = Printer.printfont(receiptHead + "\n", FontDefine.FONT_32PX, FontDefine.Align_CENTER, (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);


        /*********** print English text*******/
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < 128; i++)
            sb.append((char) i);
        String content = sb.toString().trim();

        byte[] englishchartext24 = Printer.printfont(content + "\n", FontDefine.FONT_24PX, FontDefine.Align_CENTER, (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);
        byte[] englishchartext32 = Printer.printfont(content + "\n", FontDefine.FONT_32PX, FontDefine.Align_CENTER, (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);
        byte[] englishchartext24underline = Printer.printfont(content + "\n", FontDefine.FONT_24PX_UNDERLINE, FontDefine.Align_CENTER, (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

        //2D Bar Code
        byte[] barcode = StringUtil.hexStringToBytes("1d 6b 02 0d 36 39 30 31 32 33 34 35 36 37 38 39 32");


        /*********** print Tail*******/
        String receiptTail = "Test Completed" + "\n"
                + "************************" + "\n";

        String receiptWeb = "** www.londatiga.net ** " + "\n\n\n";

        byte[] foot = Printer.printfont(receiptTail, FontDefine.FONT_32PX, FontDefine.Align_CENTER, (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);
        byte[] web = Printer.printfont(receiptWeb, FontDefine.FONT_32PX, FontDefine.Align_CENTER, (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

        byte[] totladata = new byte[header.length + englishchartext24.length + englishchartext32.length + englishchartext24underline.length +
                +barcode.length
                + foot.length + web.length
                ];
        int offset = 0;
        System.arraycopy(header, 0, totladata, offset, header.length);
        offset += header.length;

        System.arraycopy(englishchartext24, 0, totladata, offset, englishchartext24.length);
        offset += englishchartext24.length;

        System.arraycopy(englishchartext32, 0, totladata, offset, englishchartext32.length);
        offset += englishchartext32.length;

        System.arraycopy(englishchartext24underline, 0, totladata, offset, englishchartext24underline.length);
        offset += englishchartext24underline.length;

        System.arraycopy(barcode, 0, totladata, offset, barcode.length);
        offset += barcode.length;

        System.arraycopy(foot, 0, totladata, offset, foot.length);
        offset += foot.length;

        System.arraycopy(web, 0, totladata, offset, web.length);
        offset += web.length;

        byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totladata, 0, totladata.length);

        sendData(senddata);
    }

    private void printText(String text) {
        byte[] line = Printer.printfont(text + "\n\n", FontDefine.FONT_32PX, FontDefine.Align_CENTER, (byte) 0x1A,
                PocketPos.LANGUAGE_ENGLISH);
        byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, line, 0, line.length);

        sendData(senddata);
    }

    private void print1DBarcode() {
		/*String content	= "6901234567892";
	
		//1D barcode format (hex): 1d 6b 02 0d + barcode data
		
		byte[] formats	= {(byte) 0x1d, (byte) 0x6b, (byte) 0x02, (byte) 0x0d};
		byte[] contents	= content.getBytes();
		
		byte[] bytes	= new byte[formats.length + contents.length];
		
		System.arraycopy(formats, 0, bytes, 0, formats.length);
		System.arraycopy(contents, 0, bytes, formats.length, contents.length);
		
		sendData(bytes);
		
		byte[] newline 	= Printer.printfont("\n\n",FontDefine.FONT_32PX,FontDefine.Align_CENTER,(byte)0x1A,PocketPos.LANGUAGE_ENGLISH);
		
		sendData(newline);*/


    }

    public static byte[] stringToBytesASCII(String str) {
        byte[] b = new byte[str.length()];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) str.charAt(i);
        }
        return b;
    }

    String getReading(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length);
        for (int i = 0; i < data.length; ++i) {
            if (data[i] < 0) {
                throw new IllegalArgumentException();
            } else if (data[i] >= 46 && data[i] <= 57) {
                sb.append((char) data[i]); //I believe this is accurate reading
            }
        }
        return sb.toString();
    }

    private void print2DBarcode() {
        String content = "Lorenz Blog - www.londatiga.net";

        //2D barcode format (hex) : 1d 6b 10 00 00 00 00 00 1f + barcode data

        byte[] formats = {(byte) 0x1d, (byte) 0x6b, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x1f};
        byte[] contents = content.getBytes();
        byte[] bytes = new byte[formats.length + contents.length];

        System.arraycopy(formats, 0, bytes, 0, formats.length);
        System.arraycopy(contents, 0, bytes, formats.length, contents.length);

        sendData(bytes);

        byte[] newline = Printer.printfont("\n\n", FontDefine.FONT_32PX, FontDefine.Align_CENTER, (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

        sendData(newline);
    }

    private void printImage() {
        try {
            String content = "Lorenz Blog - www.londatiga.net";
            byte[] contents = content.getBytes();
            byte[] bytes = new byte[contents.length];
            sendData(bytes);

            byte[] newline = Printer.printfont("\n\n", FontDefine.FONT_32PX, FontDefine.Align_CENTER, (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

            sendData(newline);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printBarcode() {
        String[] types = {"1D Barcode", "2D Barcode"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Barcode")
                .setItems(types, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            print1DBarcode();
                        } else {
                            print2DBarcode();
                        }
                    }
                });

        builder.create().show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    showEnabled();
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
        finish();
    }
}