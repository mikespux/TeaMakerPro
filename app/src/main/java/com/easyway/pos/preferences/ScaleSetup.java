package com.easyway.pos.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.easyway.pos.R;


public class ScaleSetup extends PreferenceFragmentCompat {

    static SharedPreferences mSharedPrefs, prefs;

    ListPreference vModes, scaleVersion, weighingAlgorithm;

    EditTextPreference tareWeight, bagWeight, stabilityReadingCounter, milliSeconds, moisture;

    public ScaleSetup() {
    }

    public static ScaleSetup newInstance() {
        ScaleSetup fragment = new ScaleSetup();
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.scale_setup, rootKey);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        vModes = findPreference("vModes");
        vModes.setSummary(mSharedPrefs.getString("vModes", getResources().getString(R.string.verificationModesSummary)));
        vModes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                vModes.setSummary(newValue.toString());
                return true;
            }
        });

        scaleVersion = findPreference("scaleVersion");
        scaleVersion.setSummary(mSharedPrefs.getString("scaleVersion", getResources().getString(R.string.scaleVersionSummary)));
        scaleVersion.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                scaleVersion.setSummary(newValue.toString());
                tareWeight.setVisible(newValue.toString().equals("DR-150"));
                return true;
            }
        });
        tareWeight = findPreference("tareWeight");
        tareWeight.setSummary(mSharedPrefs.getString("tareWeight", getResources().getString(R.string.prefTareSummary)));
        tareWeight.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        });
        tareWeight.setVisible(mSharedPrefs.getString("scaleVersion", getResources().getString(R.string.scaleVersionSummary)).equals("DR-150"));
        weighingAlgorithm = findPreference("weighingAlgorithm");
        weighingAlgorithm.setSummary(mSharedPrefs.getString("weighingAlgorithm", getResources().getString(R.string.weighingAlgorithmSummary)));
        weighingAlgorithm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                weighingAlgorithm.setSummary(newValue.toString());
                return true;
            }
        });

        bagWeight = findPreference("bagWeight");
        bagWeight.setSummary(mSharedPrefs.getString("bagWeight", getResources().getString(R.string.bagWeightSummary)));
        bagWeight.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        });


        bagWeight.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                bagWeight.setSummary(newValue.toString());
                return true;
            }
        });


        stabilityReadingCounter = findPreference("stabilityReadingCounter");
        stabilityReadingCounter.setSummary(mSharedPrefs.getString("stabilityReadingCounter", getResources().getString(R.string.stabilitySummary)));
        stabilityReadingCounter.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_NUMBER);
            }
        });
        stabilityReadingCounter.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                stabilityReadingCounter.setSummary(newValue.toString());
                return true;
            }
        });


        milliSeconds = findPreference("milliSeconds");
        milliSeconds.setSummary(mSharedPrefs.getString("milliSeconds", getResources().getString(R.string.stabilitySummaryT)));

        moisture = findPreference("moisture");
        moisture.setSummary(mSharedPrefs.getString("moisture", getResources().getString(R.string.prefMoistureSummary)));
        moisture.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        });

        moisture.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                double min = 0.0;
                double max = 25.9;
                double val = Double.parseDouble(newValue.toString());
                if ((val >= min) && (val <= max)) {
                    moisture.setSummary(newValue.toString());
                    //Log.d(LOGTAG, "Value saved: " + val);
                    return true;
                } else {
                    // invalid you can show invalid message
                    Context context = getContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = customToastroot.findViewById(R.id.toast);
                    text.setText("Please enter values between " + min + " and " + max);
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getApplicationContext(), "Please enter values between "+minTime +" and "+maxTime, Toast.LENGTH_LONG).show();
                    return false;
                }

            }
        });

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        return;
    }


}
