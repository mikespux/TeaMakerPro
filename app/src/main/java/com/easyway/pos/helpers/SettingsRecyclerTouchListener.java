package com.easyway.pos.helpers;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.easyway.pos.activities.SettingsActivity;


public class SettingsRecyclerTouchListener implements RecyclerView.OnItemTouchListener {
    private final GestureDetector gestureDetector;
    private final SettingsActivity.ClickListener clickListener;

    public SettingsRecyclerTouchListener(Context context, final RecyclerView recyclerView, final SettingsActivity.ClickListener clickListener) {
        Log.d("Ben", "constructor invoked");
        this.clickListener = clickListener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d("Ben", "onSingleTapUp" + e);
                return true;


            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null && clickListener != null) {
                    clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                }
                Log.d("Ben", "onLongPress" + e);


            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

        View child = rv.findChildViewUnder(e.getX(), e.getY());
        if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {

            clickListener.onClick(child, rv.getChildPosition(child));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        Log.d("Ben", "onTouchEvent" + e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}