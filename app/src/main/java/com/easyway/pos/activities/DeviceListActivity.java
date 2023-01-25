package com.easyway.pos.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easyway.pos.R;

import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParser;

import java.util.Set;
import java.util.UUID;

public class DeviceListActivity extends Activity {
    private static final boolean f2D = true;
    public static String EXTRA_DEVICE_ADDRESS = null;
    private static final String TAG = "DeviceList";
    UUID deviceUUID;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter mBtAdapter;

    private final OnItemClickListener mDeviceClickListener;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private final BroadcastReceiver mReceiver;

    /* renamed from: com.octagon.pos.activity.DeviceListActivity.1 */
    class C00731 implements OnItemClickListener {


        public void onItemClick(AdapterView<?> adapterView, View v, int arg2, long arg3) {
            DeviceListActivity.this.mBtAdapter.cancelDiscovery();
            try {
                String info = ((TextView) v).getText().toString();
                String address = info.substring(info.length() - 17);
                Intent intent = new Intent();
                if (address.equals(XmlPullParser.NO_NAMESPACE)) {
                    DeviceListActivity.this.setResult(-1);
                    return;
                }
                Log.i(DeviceListActivity.TAG, "Value of address " + address);
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(DeviceListActivity.this);
                Editor editor = pref.edit();

                // Saving long
                editor.putString("address", address);  // Saving string

                // Save the changes in SharedPreferences
                editor.commit(); // commit changes


                DeviceListActivity.this.deviceUUID = UUID.randomUUID();
                switch (10) {
                    case XmlPullParser.DOCDECL /*10*/:
                        try {
                            // String lastUsedDevice = DeviceListActivity.this.mDbHelper.getLastUsedDevice();
                            int returnValue = 2;
                            if (returnValue >= 1) {
                                Toast.makeText(DeviceListActivity.this.getApplicationContext(), "Device " + address + " Successfully Paired", Toast.LENGTH_LONG).show();
                                intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
                                DeviceListActivity.this.setResult(-1, intent);
                                finish();
                                return;
                            } else if (returnValue != -2) {
                                return;
                            } else {
                                return;
                            }


                        } catch (Exception dbExecption) {
                            Log.e(DeviceListActivity.TAG, "DbException " + dbExecption.getMessage());
                            return;
                        }
                    case 20:
                        DeviceListActivity.this.deviceUUID = UUID.randomUUID();
                        return;
                    default:
                        return;
                }
                // Log.e(DeviceListActivity.TAG, "Error Getting Address " + e.getMessage().toString());
            } catch (Exception e) {
                Log.e(DeviceListActivity.TAG, "Error Getting Address " + e.getMessage());
            }
        }
    }

    /* renamed from: com.octagon.pos.activity.DeviceListActivity.2 */
    class C00742 extends BroadcastReceiver {
        C00742() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.FOUND".equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (device.getBondState() != 12) {
                    Log.i(DeviceListActivity.TAG, "Device Am Looking for is " + DeviceListActivity.this.getResources().getText(R.string.MyDevice).toString());
                    try {
                        if (device.getName().equals(DeviceListActivity.this.getResources().getText(R.string.MyDevice).toString())) {
                            DeviceListActivity.this.mNewDevicesArrayAdapter.add(device.getName() + StringUtils.LF + device.getAddress());
                        }
                    } catch (Exception crap) {
                        Log.e(DeviceListActivity.TAG, crap.toString());
                    }
                }
            } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                DeviceListActivity.this.setProgressBarIndeterminateVisibility(false);
                DeviceListActivity.this.setTitle(R.string.select_device);
                if (DeviceListActivity.this.mNewDevicesArrayAdapter.getCount() == 0) {
                    DeviceListActivity.this.mNewDevicesArrayAdapter.add(DeviceListActivity.this.getResources().getText(R.string.none_found).toString());
                    DeviceListActivity.this.exit();
                }
            }
        }
    }

    /* renamed from: com.octagon.pos.activity.DeviceListActivity.3 */
    class C00753 implements OnClickListener {
        C00753() {
        }

        public void onClick(View v) {
            DeviceListActivity.this.doDiscovery();
            v.setVisibility(View.VISIBLE);
        }
    }

    public DeviceListActivity() {
        this.mBluetoothAdapter = null;
        this.mDeviceClickListener = new C00731();
        this.mReceiver = new C00742();
    }

    static {
        EXTRA_DEVICE_ADDRESS = "device_address";
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(5);
        setContentView(R.layout.dialog_device_list);
        setResult(0);
        findViewById(R.id.button_scan).setOnClickListener(new C00753());
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.dialog_device_name);
        this.mNewDevicesArrayAdapter = new ArrayAdapter(this, R.layout.dialog_device_name);
        ListView pairedListView = findViewById(R.id.paired_devices);
        pairedListView.setAdapter(this.mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(this.mDeviceClickListener);
        ListView newDevicesListView = findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(this.mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(this.mDeviceClickListener);
        registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.FOUND"));
        registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_FINISHED"));
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = this.mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(getResources().getText(R.string.MyDevice).toString())) {
                    this.mPairedDevicesArrayAdapter.add(device.getName() + StringUtils.LF + device.getAddress());
                }
                this.mPairedDevicesArrayAdapter.add(device.getName() + StringUtils.LF + device.getAddress());
            }
            return;
        }
        this.mPairedDevicesArrayAdapter.add(getResources().getText(R.string.none_paired).toString());
    }

    protected void onStart() {
        super.onStart();
        if (!this.mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 3);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mBtAdapter != null) {
            this.mBtAdapter.cancelDiscovery();
        }
        unregisterReceiver(this.mReceiver);
    }

    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");
        setProgressBarIndeterminateVisibility(f2D);
        setTitle(R.string.scanning);
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        if (this.mBtAdapter.isDiscovering()) {
            this.mBtAdapter.cancelDiscovery();
        }
        this.mBtAdapter.startDiscovery();
    }

    public void exit() {
        setResult(0);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != -1) {
            Log.d(TAG, "BT not enabled");
            Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
