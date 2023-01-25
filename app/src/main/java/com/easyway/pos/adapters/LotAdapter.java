package com.easyway.pos.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.easyway.pos.R;
import com.easyway.pos.data.DBHelper;
import com.easyway.pos.data.Database;
import com.easyway.pos.models.LotList;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class LotAdapter extends BaseAdapter {

    private final boolean on_attach = true;
    @NonNull
    Context mContext;
    ArrayList<LotList> listings;
    private ArrayList<LotList> arraylist = null;
    // for loading  filter data
    private final int lastPosition = -1;
    DBHelper dbhelper;
    SQLiteDatabase db;

    public LotAdapter(@NonNull Context context, ArrayList<LotList> listings) {
        this.mContext = context;
        this.listings = listings;
        dbhelper = new DBHelper(mContext);
        db = dbhelper.getReadableDatabase();
    }


    @Override
    public int getCount() {
        return listings.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LotList item = listings.get(position);
        final DecimalFormat df = new DecimalFormat("#0.0#");
        this.arraylist = new ArrayList<>();
        this.arraylist.addAll(listings);
        notifyDataSetChanged();


        // view holder pattern
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.lot_item, null);

            TextView txtAccountId, tvRnumber, tvDate, tvLotID, tvFactoryWt, tvFieldWt, tvVariance, tvMadeTea, tvOpen, tvClose, tvWeigh, tvView;

            txtAccountId = convertView.findViewById(R.id.txtAccountId);
            tvRnumber = convertView.findViewById(R.id.tvRnumber);
            tvDate = convertView.findViewById(R.id.tvDate);
            tvLotID = convertView.findViewById(R.id.tvLotID);

            tvFactoryWt = convertView.findViewById(R.id.tvFactoryWt);
            tvFieldWt = convertView.findViewById(R.id.tvFieldWt);
            tvVariance = convertView.findViewById(R.id.tvVariance);
            tvMadeTea = convertView.findViewById(R.id.tvMadeTea);

            tvOpen = convertView.findViewById(R.id.tvOpen);
            tvClose = convertView.findViewById(R.id.tvClose);
            tvWeigh = convertView.findViewById(R.id.tvWeigh);
            tvView = convertView.findViewById(R.id.tvView);


            final ViewHolder viewHolder = new ViewHolder(txtAccountId, tvRnumber, tvDate, tvLotID, tvFactoryWt, tvFieldWt, tvVariance, tvMadeTea, tvOpen, tvClose, tvWeigh, tvView);
            convertView.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat dateFormatB = new SimpleDateFormat("yyyy-MM-dd");
        Date receipt_date = null;
        try {
            receipt_date = dateFormat.parse(item.prodDate());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        viewHolder.txtAccountId.setText(item.RecordIndex());
        viewHolder.tvRnumber.setText(item.prodBatchNumber());
        viewHolder.tvDate.setText(dateFormatB.format(receipt_date));
        viewHolder.tvLotID.setText(item.RecordIndex());

        viewHolder.tvFactoryWt.setText(item.prodTotalCrop());
        viewHolder.tvFieldWt.setText(item.prodBatFieldWeight());

        viewHolder.tvVariance.setText(df.format(Double.parseDouble(item.prodTotalCrop()) - Double.parseDouble(item.prodBatFieldWeight())));


        String selectQuery = "SELECT *  FROM " + Database.LOT_TABLE_NAME + " WHERE CloudID =" + item.RecordIndex();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            viewHolder.tvOpen.setVisibility(View.GONE);
            viewHolder.tvClose.setVisibility(View.VISIBLE);
            viewHolder.tvWeigh.setVisibility(View.VISIBLE);
            viewHolder.tvView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvOpen.setVisibility(View.VISIBLE);
            viewHolder.tvClose.setVisibility(View.GONE);
            viewHolder.tvWeigh.setVisibility(View.GONE);
            viewHolder.tvView.setVisibility(View.GONE);


        }

        cursor.close();


        Cursor c = db.rawQuery("select " +
                "" + Database.LOT_ID +
                ",COUNT(" + Database.ROW_ID + ")" +
                ",SUM(" + Database.TARE + ")" +
                ",SUM(" + Database.GROSS + ") FROM " +
                Database.SORTING_TABLE_NAME + " WHERE "
                + Database.LOT_ID + " ='" + item.RecordIndex() + "'", null);
        if (c != null) {

            c.moveToFirst();
            viewHolder.tvMadeTea.setText(df.format(c.getDouble(3)));
            c.close();
        }

        // setAnimation(convertView,position);
        return convertView;
    }


    private static class ViewHolder {


        TextView txtAccountId, tvRnumber, tvDate, tvLotID, tvFactoryWt, tvFieldWt, tvVariance, tvMadeTea, tvOpen, tvClose, tvWeigh, tvView;

        public ViewHolder(TextView txtAccountId, TextView tvRnumber, TextView tvDate, TextView tvLotID, TextView tvFactoryWt, TextView tvFieldWt, TextView tvVariance, TextView tvMadeTea, TextView tvOpen, TextView tvClose, TextView tvWeigh, TextView tvView) {

            this.txtAccountId = txtAccountId;
            this.tvRnumber = tvRnumber;
            this.tvDate = tvDate;
            this.tvLotID = tvLotID;

            this.tvFactoryWt = tvFactoryWt;
            this.tvFieldWt = tvFieldWt;
            this.tvVariance = tvVariance;
            this.tvMadeTea = tvMadeTea;

            this.tvOpen = tvOpen;
            this.tvClose = tvClose;
            this.tvWeigh = tvWeigh;
            this.tvView = tvView;


        }

    }

}