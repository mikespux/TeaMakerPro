package com.easyway.pos.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.easyway.pos.R;
import com.easyway.pos.data.DBHelper;
import com.google.android.material.tabs.TabLayout;

/**
 * Created by Abderrahim on 10/14/2015.
 */
public class TabsFragment extends Fragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;
    public int nb_items = 2;
    DBHelper dbhelper;
    SharedPreferences prefs;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbhelper = new DBHelper(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        view = inflater.inflate(R.layout.tabs_layout, null);
        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new TabsAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(2); // if you use 3 tabs
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
                tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
                tabLayout.setTabMode(TabLayout.MODE_FIXED);
                setupTabIcons();
            }
        });
        return view;
    }

    /**
     * method to setup tabs icons
     */
    private void setupTabIcons() {

        tabLayout.getTabAt(0).setText("SHIFTS");
        tabLayout.getTabAt(1).setText("PRODUCTION LOT");


    }


    class TabsAdapter extends FragmentPagerAdapter {
        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    hideKeyboard();
                    return new ShiftFragment();

                case 1:


                    return new ProductionLotFragment();


            }
            return null;
        }

        @Override
        public int getCount() {
            return nb_items;

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        View focusedView = getActivity().getCurrentFocus();

        if (focusedView != null) {

            try {
                assert inputManager != null;
                inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (AssertionError e) {
                e.printStackTrace();
            }
        }
    }


}