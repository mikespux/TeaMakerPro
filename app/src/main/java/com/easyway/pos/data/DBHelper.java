package com.easyway.pos.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DBHelper extends SQLiteOpenHelper {
    private final Context _context;
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "EasywayPt.db";
    SharedPreferences mSharedPrefs;

    public DBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
        _context = ctx;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

    }

    public void createTables(SQLiteDatabase database) {


        //Factory Table
        String factory_table_sql = "create table " + Database.FACTORY_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.FRY_PREFIX + " TEXT," +
                Database.FRY_TITLE + " TEXT," +
                Database.FRY_ClOUDID + " TEXT)";

        //Grades Table
        String grade_table_sql = "create table " + Database.GRADES_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.GRADE_CODE + " TEXT," +
                Database.GRADE_NAME + " TEXT," +
                Database.CloudID + " TEXT)";

        //Shift Table
        String shift_table_sql = "create table " + Database.SHIFT_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.SHIFT_DATE + " TEXT," +
                Database.START_TIME + " TEXT," +
                Database.WORKING_HOURS + " TEXT," +
                Database.USER_ID + " TEXT," +
                Database.FACTORY_ID + " TEXT," +
                Database.Status + " TEXT," +
                Database.CloudID + " TEXT)";


        //Lot Table
        String lot_table_sql = "create table " + Database.LOT_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.LOT_NO + " TEXT," +
                Database.LOT_DATE + " TEXT," +
                Database.START_OF_MANUFACTURE + " TEXT," +
                Database.END_OF_MANUFACTURE + " TEXT," +
                Database.TOTAL_MADE_TEA + " TEXT," +
                Database.CLOSING_TIME + " TEXT," +
                Database.Status + " TEXT," +
                Database.CloudID + " TEXT)";

        //Sorting Table
        String sorting_table_sql = "create table " + Database.SORTING_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.LOT_ID + " TEXT," +
                Database.SHIFT_ID + " TEXT," +
                Database.GRADE_ID + " TEXT," +
                Database.TERMINAL_ID + " TEXT," +
                Database.SORTING_DATE + " TEXT," +
                Database.SORTING_TIME + " TEXT," +
                Database.GROSS + " TEXT," +
                Database.TARE + " TEXT," +
                Database.QUANTITY + " TEXT," +
                Database.Status + " TEXT," +
                Database.CloudID + " TEXT)";


        //OperatorsMaster Table
        String operators_master_table_sql = "create table " + Database.OPERATORSMASTER_TABLE_NAME + "( " +
                Database.ROW_ID + " integer  primary key autoincrement," +
                Database.USERFULLNAME + " TEXT," +
                Database.USERNAME + " TEXT," +
                Database.ACCESSLEVEL + " TEXT," +
                Database.USERPWD + " TEXT," +
                Database.PWDSETDATE + " TEXT," +
                Database.PWDEXPDATE + " TEXT," +
                Database.USERCLOUDID + " TEXT)";


        String DefaultUsers = "INSERT INTO " + Database.OPERATORSMASTER_TABLE_NAME + " ("
                + Database.USERFULLNAME + ", "
                + Database.USERNAME + ", "
                + Database.USERPWD + ", "
                + Database.ACCESSLEVEL + ") Values ('OCTAGON', 'ODS', '1234', '1')";


        String DefaultGrade = "INSERT INTO " + Database.GRADES_TABLE_NAME + " ("
                + Database.ROW_ID + ", "
                + Database.GRADE_CODE + ", "
                + Database.GRADE_NAME + ") Values ('0','0' ,'Select ...')";


        try {

            database.execSQL(factory_table_sql);
            database.execSQL(grade_table_sql);
            database.execSQL(shift_table_sql);
            database.execSQL(lot_table_sql);
            database.execSQL(sorting_table_sql);


            database.execSQL(operators_master_table_sql);


            Log.d("EasywayProduction", "Tables created!");
            //Defaults
            database.execSQL(DefaultUsers);
            database.execSQL(DefaultGrade);


        } catch (Exception ex) {
            Log.d("EasywayProduction", "Error in DBHelper.onCreate() : " + ex.getMessage());
        }
    }


    /////////////////////////////////////////////////////////////////////
    //USERS FUNCTIONS///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddUsers(String s_etFullName, String s_etNewUserId, String s_etPassword, String s_spUserLevel) {
        Calendar cal = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 30);
        Date expDate = c.getTime();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.USERFULLNAME, s_etFullName);
        initialValues.put(Database.USERNAME, s_etNewUserId);
        initialValues.put(Database.USERPWD, s_etPassword);
        initialValues.put(Database.ACCESSLEVEL, s_spUserLevel);
        initialValues.put(Database.PWDSETDATE, dateTimeFormat.format(cal.getTime()));
        //initialValues.put(Database.PWDSETDATE, "2021-07-01 10:00:12");
        initialValues.put(Database.PWDEXPDATE, dateTimeFormat.format(expDate));
        initialValues.put(Database.USERCLOUDID, 0);

        return db.insert(Database.OPERATORSMASTER_TABLE_NAME, null, initialValues);

    }

    public Cursor SearchUsername(String user) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor myCursor = db.query(Database.OPERATORSMASTER_TABLE_NAME,
                new String[]{Database.ROW_ID, Database.USERNAME, Database.USERFULLNAME}, Database.USERNAME + " LIKE ? OR " + Database.USERFULLNAME + " LIKE ?",
                new String[]{"%" + user + "%", "%" + user + "%"}, Database.USERNAME, null, Database.USERNAME + " ASC");

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        //db.close();
        return myCursor;
    }

    public Cursor fetchUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.OPERATORSMASTER_TABLE_NAME,
                new String[]{"_id", "ClerkName"},
                "ClerkName" + "='" + username + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    public boolean UserLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + Database.OPERATORSMASTER_TABLE_NAME
                + " WHERE ClerkName=? COLLATE NOCASE AND UserPwd=?", new String[]{username, password});
        if (mCursor != null) {
            return mCursor.getCount() > 0;

        }
        return false;
    }

    /**
     * cursor for viewing access level
     */
    public Cursor getAccessLevel(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] allColumns = new String[]{Database.ACCESSLEVEL, Database.USERFULLNAME, Database.USERPWD};
        Cursor c = db.query(Database.OPERATORSMASTER_TABLE_NAME, allColumns, "ClerkName COLLATE NOCASE" + "='" + username + "'", null, null, null, null,
                null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /**
     * cursor for viewing password
     */
    public Cursor getPassword(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] allColumns = new String[]{Database.USERPWD, Database.USERFULLNAME};
        Cursor c = db.query(Database.OPERATORSMASTER_TABLE_NAME, allColumns, "ClerkName COLLATE NOCASE" + "='" + username + "'", null, null, null, null,
                null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /**
     * cursor for viewing password set datr
     */
    public Cursor getPwdSetDate(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] allColumns = new String[]{Database.PWDSETDATE, Database.PWDEXPDATE};
        Cursor c = db.query(Database.OPERATORSMASTER_TABLE_NAME, allColumns, "ClerkName COLLATE NOCASE" + "='" + username + "'", null, null, null, null,
                null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public long expiry_days(String username) {
        Cursor psetdate = getPwdSetDate(username);
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date set_time = null;
        long difference;
        try {
            set_time = input.parse(psetdate.getString(0));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i("TOKEN_HOURS ", set_time + " Current Date " + new Date());
        assert set_time != null;
        difference = new Date().getTime() - set_time.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = difference / daysInMilli;
        difference = difference % daysInMilli;

        long elapsedHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;

        long elapsedMinutes = difference / minutesInMilli;
        difference = difference % minutesInMilli;

        long elapsedSeconds = difference / secondsInMilli;
        Log.i("======= Hours", " :: " + elapsedHours);
        Log.i("======= Minutes", " :: " + elapsedMinutes);
        Log.i("======= Days", " :: " + elapsedDays);
        System.out.printf("%d hours, %d minutes, %d seconds%n", elapsedHours, elapsedMinutes, elapsedSeconds);
        return elapsedDays;
    }

    /////////////////////////////////////////////////////////////////////
    //FACTORY FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddFactories(String s_fryprefix, String s_fryname, String s_recordindex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.FRY_PREFIX, s_fryprefix);
        initialValues.put(Database.FRY_TITLE, s_fryname);
        initialValues.put(Database.FRY_ClOUDID, s_recordindex);
        return db.insert(Database.FACTORY_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckFactory(String s_fryprefix) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.FACTORY_TABLE_NAME,
                new String[]{"_id", Database.FRY_PREFIX},
                Database.FRY_PREFIX + "='" + s_fryprefix + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    /////////////////////////////////////////////////////////////////////
    //GRADE FUNCTIONS/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public long AddGrade(String FGRef, String FGDescription, String RecordIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.GRADE_CODE, FGRef);
        initialValues.put(Database.GRADE_NAME, FGDescription);
        initialValues.put(Database.CloudID, RecordIndex);
        return db.insert(Database.GRADES_TABLE_NAME, null, initialValues);

    }

    public Cursor CheckGrade(String GRADE_CODE) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.GRADES_TABLE_NAME,
                new String[]{"_id", Database.GRADE_CODE},
                Database.GRADE_CODE + "='" + GRADE_CODE + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    /////////////////////////////////////////////////////////////////////
    //SHIFT FUNCTIONS///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public Cursor CheckShift(String Id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.SHIFT_TABLE_NAME,
                new String[]{"_id", Database.CloudID},
                Database.CloudID + "='" + Id + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    public void AddShift(String shift_date, String start_time, String working_hours, String user_id, String factoryid, String Status, String CloudID) {

        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.SHIFT_DATE, shift_date);
        initialValues.put(Database.START_TIME, start_time);
        initialValues.put(Database.WORKING_HOURS, working_hours);
        initialValues.put(Database.USER_ID, user_id);
        initialValues.put(Database.FACTORY_ID, factoryid);
        initialValues.put(Database.Status, Status);
        initialValues.put(Database.CloudID, CloudID);

        db.insert(Database.SHIFT_TABLE_NAME, null, initialValues);


    }


    /////////////////////////////////////////////////////////////////////
    //LOT FUNCTIONS///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public Cursor CheckLot(String Id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.LOT_TABLE_NAME,
                new String[]{"_id", Database.CloudID},
                Database.CloudID + "='" + Id + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    public void AddLot(String lot_no, String lot_date, String start_of_manufacture, String end_of_manufacture, String closing_time, String total_made_tea, String Status, String CloudID) {

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.LOT_NO, lot_no);
        initialValues.put(Database.LOT_DATE, lot_date);
        initialValues.put(Database.START_OF_MANUFACTURE, start_of_manufacture);
        initialValues.put(Database.END_OF_MANUFACTURE, end_of_manufacture);
        initialValues.put(Database.CLOSING_TIME, closing_time);
        initialValues.put(Database.TOTAL_MADE_TEA, total_made_tea);
        initialValues.put(Database.Status, Status);
        initialValues.put(Database.CloudID, CloudID);

        db.insert(Database.LOT_TABLE_NAME, null, initialValues);


    }

    /////////////////////////////////////////////////////////////////////
    //MADE TEA FUNCTIONS///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public Cursor CheckMadeTea(String Id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.query(Database.SORTING_TABLE_NAME,
                new String[]{"_id", Database.CloudID},
                Database.CloudID + "='" + Id + "'", null, null, null, null);

        if (myCursor != null) {
            myCursor.moveToFirst();
        }
        return myCursor;
    }

    public void AddMadeTea(String Schedule, String Shift, String WeighingKit, String SortingDate, String Grade, String Gross, String Tare, String WeighingTime, String CloudID) {

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(Database.LOT_ID, Schedule);
        initialValues.put(Database.SHIFT_ID, Shift);
        initialValues.put(Database.TERMINAL_ID, WeighingKit);
        initialValues.put(Database.SORTING_DATE, SortingDate);
        initialValues.put(Database.GRADE_ID, Grade);
        initialValues.put(Database.GROSS, Gross);
        initialValues.put(Database.TARE, Tare);

        final DecimalFormat df = new DecimalFormat("#0.0#");
        double Quantity;
        Quantity = Double.parseDouble(Gross) - Double.parseDouble(Tare);

        initialValues.put(Database.QUANTITY, df.format(Quantity));
        initialValues.put(Database.SORTING_TIME, WeighingTime);
        initialValues.put(Database.CloudID, CloudID);

        db.insert(Database.SORTING_TABLE_NAME, null, initialValues);


    }
}
