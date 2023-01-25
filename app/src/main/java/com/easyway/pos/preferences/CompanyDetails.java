package com.easyway.pos.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.easyway.pos.R;
import com.easyway.pos.activities.CompanyDetailsActivity;


public class CompanyDetails extends PreferenceFragmentCompat {

    static SharedPreferences mSharedPrefs, prefs;
    EditTextPreference device_terminal, device_phone, device_serial, device_factory, autoLogout;
    EditTextPreference co_prefix, co_name, co_letterbox, co_postcode, co_postname, co_postregion, co_telephone;
    Button btnRefresh;
    Preference button;

    public CompanyDetails() {
    }

    public static CompanyDetails newInstance() {
        CompanyDetails fragment = new CompanyDetails();
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.company_details, rootKey);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        device_terminal = findPreference("terminalID");
        device_terminal.setTitle(mSharedPrefs.getString("terminalID", "Terminal ID"));

        if (device_terminal.getText().equals("0")) {
            device_terminal.setTitle("Enter Terminal ID");
        }
        if (device_terminal.getText() == null) {
            device_terminal.setVisible(false);
        }

        device_phone = findPreference("PhoneNo");
        device_phone.setTitle(mSharedPrefs.getString("PhoneNo", "Phone"));
        if (device_phone.getText() == null || device_phone.getText().equals("")) {
            device_phone.setVisible(false);
        }
        device_factory = findPreference("Factory");
        device_factory.setTitle(mSharedPrefs.getString("Factory", "Factory"));
        if (device_factory.getText() == null || device_factory.getText().equals("")) {
            device_factory.setVisible(false);
        }
        device_serial = findPreference("device_serial");
        device_serial.setTitle(mSharedPrefs.getString("device_serial", "Serial No"));
        if (device_serial.getText() == null || device_serial.getText().equals("")) {
            device_serial.setVisible(false);
        }
        co_prefix = findPreference("company_prefix");
        co_prefix.setTitle(mSharedPrefs.getString("company_prefix", "Company Prefix"));
        if (co_prefix.getText().equals("")) {
            co_prefix.setVisible(false);
        }

        co_name = findPreference("company_name");
        co_name.setTitle(mSharedPrefs.getString("company_name", "Company Name"));
        if (co_name.getText().equals("")) {
            co_name.setVisible(false);
        }

        co_letterbox = findPreference("company_letterbox");
        co_letterbox.setTitle(mSharedPrefs.getString("company_letterbox", "LetterBox"));
        if (co_letterbox.getText().equals("")) {
            co_letterbox.setVisible(false);
        }
        co_postcode = findPreference("company_postalcode");
        co_postcode.setTitle(mSharedPrefs.getString("company_postalcode", "Postal Code"));
        if (co_postcode.getText().equals("")) {
            co_postcode.setVisible(false);
        }
        co_postname = findPreference("company_postalname");
        co_postname.setTitle(mSharedPrefs.getString("company_postalname", "Postal Name"));
        if (co_postname.getText().equals("")) {
            co_postname.setVisible(false);
        }
        co_postregion = findPreference("company_postregion");
        co_postregion.setTitle(mSharedPrefs.getString("company_postregion", "Region"));
        if (co_postregion.getText().equals("")) {
            co_postregion.setVisible(false);
        }
        co_telephone = findPreference("company_posttel");
        co_telephone.setTitle(mSharedPrefs.getString("company_posttel", "Telephone"));
        if (co_telephone.getText().equals("")) {
            co_telephone.setVisible(false);
        }

        autoLogout = findPreference("autoLogout");
        assert autoLogout != null;
        autoLogout.setSummary(mSharedPrefs.getString("autoLogout", "Enter Time in Minutes"));
        autoLogout.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        autoLogout.setOnPreferenceChangeListener((preference, newValue) -> {
            int val = Integer.parseInt(newValue.toString());
            if (val > 60) {
                Context context = getContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = customToastroot.findViewById(R.id.toast);
                text.setText("Please enter value between 0 and 60");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                return false;
            }
            autoLogout.setSummary(newValue.toString());
            return true;
        });
        button = getPreferenceManager().findPreference("btn_Refresh");
        if (button != null) {
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    SharedPreferences.Editor edit = mSharedPrefs.edit();

                    edit.putBoolean("prefs_refresh", true);
                    edit.apply();
                    refreshBtn();
                    return true;
                }
            });
        }

//         btnRefresh = getActivity().findViewById(R.id.btnRefresh);
//        btnRefresh.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                refreshBtn();
//            }
//        });
    }

    @Override
    public void onResume() {
        super.onResume();
        device_terminal = findPreference("terminalID");
        device_terminal.setTitle(mSharedPrefs.getString("terminalID", "Terminal ID"));

        if (device_terminal.getText().equals("0")) {
            device_terminal.setTitle("Enter Terminal ID");
        }
        if (device_terminal.getText() == null) {
            device_terminal.setVisible(false);
        }

        device_phone = findPreference("PhoneNo");
        device_phone.setTitle(mSharedPrefs.getString("PhoneNo", "Phone"));
        if (device_phone.getText() == null || device_phone.getText().equals("")) {
            device_phone.setVisible(false);
        }
        device_factory = findPreference("Factory");
        device_factory.setTitle(mSharedPrefs.getString("Factory", "Factory"));
        if (device_factory.getText() == null || device_factory.getText().equals("")) {
            device_factory.setVisible(false);
        }
        device_serial = findPreference("device_serial");
        device_serial.setTitle(mSharedPrefs.getString("device_serial", "Serial No"));
        if (device_serial.getText() == null || device_serial.getText().equals("")) {
            device_serial.setVisible(false);
        }
        co_prefix = findPreference("company_prefix");
        co_prefix.setTitle(mSharedPrefs.getString("company_prefix", "Company Prefix"));
        if (co_prefix.getText().equals("")) {
            co_prefix.setVisible(false);
        }

        co_name = findPreference("company_name");
        co_name.setTitle(mSharedPrefs.getString("company_name", "Company Name"));
        if (co_name.getText().equals("")) {
            co_name.setVisible(false);
        }

        co_letterbox = findPreference("company_letterbox");
        co_letterbox.setTitle(mSharedPrefs.getString("company_letterbox", "LetterBox"));
        if (co_letterbox.getText().equals("")) {
            co_letterbox.setVisible(false);
        }
        co_postcode = findPreference("company_postalcode");
        co_postcode.setTitle(mSharedPrefs.getString("company_postalcode", "Postal Code"));
        if (co_postcode.getText().equals("")) {
            co_postcode.setVisible(false);
        }
        co_postname = findPreference("company_postalname");
        co_postname.setTitle(mSharedPrefs.getString("company_postalname", "Postal Name"));
        if (co_postname.getText().equals("")) {
            co_postname.setVisible(false);
        }
        co_postregion = findPreference("company_postregion");
        co_postregion.setTitle(mSharedPrefs.getString("company_postregion", "Region"));
        if (co_postregion.getText().equals("")) {
            co_postregion.setVisible(false);
        }
        co_telephone = findPreference("company_posttel");
        co_telephone.setTitle(mSharedPrefs.getString("company_posttel", "Telephone"));
        if (co_telephone.getText().equals("")) {
            co_telephone.setVisible(false);
        }
    }

    public void refreshBtn() {
        Intent mIntent = new Intent(getActivity(), CompanyDetailsActivity.class);
        startActivity(mIntent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        return;
    }


}
