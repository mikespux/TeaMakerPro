package com.easyway.pos.data;


public class Database {

    //Constants
    public static final String ROW_ID = "_id";
    public static final String CloudID = "CloudID";
    public static final String Status = "Status";

    //Factory Table
    public static final String FACTORY_TABLE_NAME = "factory";
    public static final String FRY_PREFIX = "FryPrefix";
    public static final String FRY_TITLE = "FryTitle";
    public static final String FRY_ClOUDID = "FryCloudID";

    // Grades Table
    public static final String GRADES_TABLE_NAME = "grades";
    public static final String GRADE_CODE = "grade_code";
    public static final String GRADE_NAME = "grade_name";


    // Shift Table
    public static final String SHIFT_TABLE_NAME = "shifts";
    public static final String SHIFT_DATE = "shift_date";
    public static final String START_TIME = "start_time";
    public static final String WORKING_HOURS = "working_hours";
    public static final String USER_ID = "user_id";
    public static final String FACTORY_ID = "factoryid";

    // Lot Table
    public static final String LOT_TABLE_NAME = "lots";
    public static final String LOT_NO = "lot_no";
    public static final String LOT_DATE = "lot_date";
    public static final String START_OF_MANUFACTURE = "start_of_manufacture";
    public static final String END_OF_MANUFACTURE = "end_of_manufacture";
    public static final String CLOSING_TIME = "closing_time";
    public static final String TOTAL_MADE_TEA = "total_made_tea";

    // Sorting Table
    public static final String SORTING_TABLE_NAME = "sorting";
    public static final String LOT_ID = "lot_id";
    public static final String SHIFT_ID = "shift_id";
    public static final String GRADE_ID = "grade_id";
    public static final String TERMINAL_ID = "terminal_id";
    public static final String SORTING_DATE = "sorting_date";
    public static final String SORTING_TIME = "sorting_time";
    public static final String GROSS = "gross";
    public static final String TARE = "tare";
    public static final String QUANTITY = "quantity";


    //OperatorsMaster Table
    public static final String OPERATORSMASTER_TABLE_NAME = "OperatorsMaster";
    public static final String USERFULLNAME = "UserIdentifier";
    public static final String USERNAME = "ClerkName";
    public static final String ACCESSLEVEL = "AccessLevel";
    public static final String USERPWD = "UserPwd";
    public static final String PWDSETDATE = "PwdSetDate";
    public static final String PWDEXPDATE = "PwdExpDate";
    public static final String USERCLOUDID = "UserCloudID";


}
