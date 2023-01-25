/*
 * Copyright ?2007-2011 Blue Bamboo Ltd.
 *
 */
package com.easyway.pos.printerutils;

/**
 * This class contains all kinds of constant type values.
 *
 * @author : Nelson
 */
public interface DataConstants {
    String NEW_LINE = "\n";
    int CREDIT_SWIPE_ACTION = 0;
    String DOT = ".";
    String SPACE = " ";

    /****  Alignment type***********************/
    int CENTER_ALIGN = 0;
    int RIGHT_ALIGN = 1;
    int LEFT_ALIGN = 2;

    /****  Main Menu Item  **************************/
    int SWIPE_OPTION_VALUE = 0;
    int PRINT_OPTION_VALUE = 1;
    int PIN_OPTION_VALUE = 2;
    int IMAGE_OPTION_VALUE = 3;
    int SMARTCARD_OPTION_VALUE = 4;
    int BAR_CODE_VALUE = 5;

    /*******  device type ***********************/
//	public static final int DEVICE_P25M = 0;
//	public static final int DEVICE_H50 = 1;
//	public static final int DEVICE_HISTORY = 2;
//	public static final int DEVICE_MANUAL = 3;

    /*******  Font Type ***********************/
    int FONT_SMALL_FONT = 0;//12*24
    int FONT_BIG_FONT = 1;//16*32
    int FONT_DOUBLE_SMALL_FONT = 2;
    int FONT_DOUBLE_BIG_FONT = 3;
    int FONT_UNDERLINE_SMALL_FONT = 4;
    int FONT_BIG_REVERSE_FONT = 5;

    /*******  language type ***********************/
    int ENGLISH = 0;
    int CHINESE = 1;


    int RECEIPT_WIDTH = 24;

    String RMS_CURRENT_DEVICE_NAME = "currentDeviceName";
    String RMS_DEVICE_LIST_KEY = "deviceNameList";
    String RMS_PRINTER_INFO = "printerInfo";

    int TYPE_BLUETOOTH_DEVICE = 2;

    int DEVICE_P25M = 0;
    int DEVICE_H50 = 1;
    int DEVICE_HISTORY = 2;
    int DEVICE_MANUAL = 3;
    int DEVICE_NONE = 4;
}
