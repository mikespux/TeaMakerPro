package com.easyway.pos.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.easyway.pos.R;


public class OtherSettings extends PreferenceFragmentCompat {

    static SharedPreferences mSharedPrefs, prefs;

    public OtherSettings() {
    }

    public static OtherSettings newInstance() {
        OtherSettings fragment = new OtherSettings();
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.other_settings, rootKey);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


    }


}
