package com.easyway.pos.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.easyway.pos.R;
import com.easyway.pos.preferences.CloudSetup;
import com.easyway.pos.preferences.CompanyDetails;
import com.easyway.pos.preferences.OtherSettings;
import com.easyway.pos.preferences.ScaleSetup;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 8/19/2015.
 */
public class SetupActivity extends AppCompatActivity {

    public Intent mIntent = null;
    public Toolbar toolbar;
    static SharedPreferences mSharedPrefs, prefs;
    ViewPager view_pager;
    TabLayout tab_layout;

    String cachedDeviceAddress;
    Button btn_pairscale, btn_pairprinter;
    AlertDialog b;
    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";
    public static final String MANUAL = "Manual";
    public static final String DR_150 = "DR-150";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        initComponent();


    }

    private void initComponent() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        view_pager = findViewById(R.id.view_pager);
        setupViewPager(view_pager);

        tab_layout = findViewById(R.id.tab_layout);
        tab_layout.setupWithViewPager(view_pager);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(CompanyDetails.newInstance(), "General Info");
        adapter.addFragment(CloudSetup.newInstance(), "Cloud Setup");
        adapter.addFragment(ScaleSetup.newInstance(), "Scale Setup");
        adapter.addFragment(OtherSettings.newInstance(), "Other Settings");
        viewPager.setAdapter(adapter);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    /**
     * method initializer
     */

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (mSharedPrefs.getString("terminalID", "").equals("0")) {
            Context context = getApplicationContext();
            LayoutInflater inflater = getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = customToastroot.findViewById(R.id.toast);
            text.setText("Please Enter Terminal ID");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }


        cachedDeviceAddress = pref.getString("scale_address", "");
        if (!cachedDeviceAddress.equals("")) {
            finish();
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_pair_devices, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Pair Devices");
        dialogBuilder.setCancelable(false);

        btn_pairscale = dialogView.findViewById(R.id.btn_pairscale);
        btn_pairscale.setOnClickListener(v -> {

            mIntent = new Intent(SetupActivity.this, PairedDeviceListActivity.class);
            startActivity(mIntent);


        });
        btn_pairprinter = dialogView.findViewById(R.id.btn_pairprinter);
        if (!mSharedPrefs.getBoolean("enablePrinting", false)) {
            btn_pairprinter.setVisibility(View.GONE);
        }
        btn_pairprinter.setOnClickListener(v -> {


            mIntent = new Intent(SetupActivity.this, PrintTestActivity.class);
            startActivity(mIntent);


        });

        dialogBuilder.setPositiveButton("Done", (dialog, whichButton) -> {
            //do something with edt.getText().toString();


        });
        dialogBuilder.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        b = dialogBuilder.create();
        b.show();

        b.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            if (mSharedPrefs.getString("scaleVersion", "").equals("")) {
                // snackbar.show();
                Context context = getApplicationContext();
                LayoutInflater inflater1 = getLayoutInflater();
                View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please Select Scale Model to Weigh");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                b.dismiss();
                //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                return;
            }
            if (mSharedPrefs.getString("scaleVersion", "").equals(EASYWEIGH_VERSION_15) ||
                    mSharedPrefs.getString("scaleVersion", "").equals(EASYWEIGH_VERSION_11) ||
                    mSharedPrefs.getString("scaleVersion", "").equals(DR_150)) {
                cachedDeviceAddress = prefs.getString("scale_address", "");
                if (cachedDeviceAddress.equals("")) {
                    // snackbar.show();
                    Context context = getApplicationContext();
                    LayoutInflater inflater1 = getLayoutInflater();
                    View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please pair scale");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    b.dismiss();
                    //Toast.makeText(getActivity(), "Please pair scale", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            if (mSharedPrefs.getBoolean("enablePrinting", false)) {

                if (prefs.getString("mDevice", "").equals("")) {
                    // snackbar.show();
                    Context context = getApplicationContext();
                    LayoutInflater inflater1 = getLayoutInflater();
                    View customToastroot = inflater1.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please Pair Printer...");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    b.dismiss();
                    //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            b.dismiss();
            finish();
            mIntent = new Intent(SetupActivity.this, MainActivity.class);
            startActivity(mIntent);
        });


    }


}
