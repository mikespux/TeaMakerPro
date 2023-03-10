package com.easyway.pos.activities;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.easyway.pos.R;

/**
 * Input text dialog.
 *
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 */
public class InputTextDialog {
    private final OnOkClickListener mListener;
    private final AlertDialog mDialog;

    public InputTextDialog(Context context, OnOkClickListener listener) {
        mListener = listener;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_input, null);
        final EditText textEt = view.findViewById(R.id.editText);

        Builder builder = new Builder(context);

        builder.setTitle("Input Text");
        builder.setView(view)
                .setPositiveButton("Print", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = textEt.getText().toString();

                        if (mListener != null && !text.equals("")) {
                            mListener.onPrintClick(text);
                        }
                    }
                });

        mDialog = builder.create();
    }

    public void show() {
        mDialog.show();
    }

    public interface OnOkClickListener {
        void onPrintClick(String text);
    }
}