package com.easyway.pos.preferences;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.easyway.pos.R;


public class CloudSetup extends PreferenceFragmentCompat {


    static SharedPreferences mSharedPrefs, prefs;

    CheckBoxPreference checkPrinting;
    CheckBoxPreference checkSMS;
    CheckBoxPreference checkRealtime;
    EditTextPreference portalURL, mdportalURL, coPort, coApp;
    ListPreference internetAccessModes;

    public CloudSetup() {
    }

    public static CloudSetup newInstance() {
        CloudSetup fragment = new CloudSetup();
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.cloud_setup, rootKey);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        checkPrinting = findPreference("enablePrinting");
        checkRealtime = findPreference("realtimeServices");
        checkSMS = findPreference("enableSMS");

        checkRealtime.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(androidx.preference.Preference preference) {
                if (!mSharedPrefs.getBoolean("realtimeServices", false) == true) {
                    //checkSMS.setChecked(false);


                } else {

                    if (checkRealtime.isChecked()) {

                    } else {
                        //checkPrinting.setChecked(false);
                    }

                }

                return true;
            }
        });


        portalURL = findPreference("portalURL");
        portalURL.setSummary(mSharedPrefs.getString("portalURL", "WI-FI URL"));
        portalURL.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                portalURL.setSummary(newValue.toString());
                return true;
            }
        });
        if (portalURL.getText().equals("")) {
            portalURL.setVisible(false);
        }

        mdportalURL = findPreference("mdportalURL");
        mdportalURL.setSummary(mSharedPrefs.getString("mdportalURL", "Mobile-Data URL"));
        mdportalURL.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mdportalURL.setSummary(newValue.toString());
                return true;
            }
        });
        if (mdportalURL.getText().equals("")) {
            mdportalURL.setVisible(false);
        }

        coPort = findPreference("coPort");
        coPort.setSummary(mSharedPrefs.getString("coPort", "Port"));
        coPort.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_NUMBER);
            }
        });
        coPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                coPort.setSummary(newValue.toString());
                return true;
            }
        });

        coApp = findPreference("coApp");
        coApp.setSummary(mSharedPrefs.getString("coApp", "Easyway.Net"));
        coApp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                coApp.setSummary(newValue.toString());
                return true;
            }
        });

        internetAccessModes = findPreference("internetAccessModes");
        internetAccessModes.setSummary(mSharedPrefs.getString("internetAccessModes", getResources().getString(R.string.internetAccessModeSummary)));
        internetAccessModes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                internetAccessModes.setSummary(newValue.toString());
                return true;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        return;
    }


}
