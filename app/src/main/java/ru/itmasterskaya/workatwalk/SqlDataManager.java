/*
 * Copyright abenefic (c) 2017.
 */

package ru.itmasterskaya.workatwalk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;
import java.util.HashMap;


public class SqlDataManager extends SQLiteOpenHelper {

    static final String STAT_LATITUDE = "Latitude";
    static final String STAT_LONGITUDE = "Longitude";
    static final String STAT_DATE = "StatDate";
    static final String STAT_BATTERY = "Battery";
    static final String MESSAGE_DATE = "MessageDate";
    static final String MESSAGE_THEME = "MessageTheme";
    static final String MESSAGE_TEXT = "MessageText";
    static final String NOTIFICATIONS_OBJECT = "Object";
    static final String SOAP_LOG_DATE = "SoapDate";
    static final String SOAP_LOG_COMMAND = "SoapCommand";
    static final String SOAP_LOG_REQUEST = "SoapRequest";
    static final String SOAP_LOG_RESULT = "SoapResult";
    private static final String DB_NAME = "innerDB";
    private static final String STATUS_TABLE = "StatusHistory";
    private static final String STAT_ID = "_ID";
    private static final String STAT_SENT = "IsSent";
    private static final String MESSAGE_TABLE = "MessageTable";
    private static final String MESSAGE_ID = "_ID";
    private static final String NOTIFICATIONS_TABLE = "Notifications";
    private static final String NOTIFICATIONS_ID = "_ID";
    private static final String NOTIFICATIONS_TIME = "Time";
    private static final String NOTIFICATIONS_TYPE = "Type";
    private static final String SOAP_LOG_TABLE = "SoapLog";
    private static final String SOAP_LOG_ID = "_ID";

    private static final String OBJECTS_DB = "ObjectsDD";
    private static final String OBJECTS_ID = "_ID";
    private static final String OBJECTS_IDENTIFICATOR = "Identificator";
    private static final String OBJECTS_KEY = "ObjectKey";
    private static final String OBJECTS_VALUE = "ObjectValue";

    private static final int version = 1;

    private static SqlDataManager instance;

    private SqlDataManager(Context context) {
        super(context, DB_NAME, null, version);
    }

    public static synchronized SqlDataManager getManager(Context context) {
        initialize(context);
        return instance;
    }

    private static void initialize(Context context) {
        if (instance == null) {
            instance = new SqlDataManager(context);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + STATUS_TABLE +
                " (" + STAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                STAT_BATTERY + " INTEGER, " + STAT_SENT + " INTEGER, " + STAT_DATE + " LONG, " + STAT_LATITUDE + " DOUBLE, " + STAT_LONGITUDE +
                " DOUBLE);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + NOTIFICATIONS_TABLE +
                " (" + NOTIFICATIONS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NOTIFICATIONS_OBJECT + " TEXT, " + NOTIFICATIONS_TYPE + " INTEGER, " + NOTIFICATIONS_TIME + " LONG," +
                "UNIQUE (" + NOTIFICATIONS_OBJECT + "," + NOTIFICATIONS_TYPE + "));");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + MESSAGE_TABLE +
                " (" + MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MESSAGE_DATE + " LONG, " + MESSAGE_THEME + " TEXT, " + MESSAGE_TEXT + " TEXT);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + SOAP_LOG_TABLE +
                " (" + SOAP_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SOAP_LOG_DATE + " LONG, " +
                SOAP_LOG_COMMAND + " TEXT, " +
                SOAP_LOG_REQUEST + " TEXT, " +
                SOAP_LOG_RESULT + " TEXT);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + OBJECTS_DB +
                " (" + OBJECTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                OBJECTS_KEY + " TEXT," +
                OBJECTS_VALUE + " TEXT," +
                OBJECTS_IDENTIFICATOR + " TEXT." +
                "UNIQUE (" + OBJECTS_IDENTIFICATOR + "," + OBJECTS_KEY + "));");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /*if (oldVersion == 11 && newVersion == 12) {
            upgradeTo12(db);
        }*/

    }

    private void writeObject(SQLiteDatabase db, String objectID, HashMap values, HashMap tables) {
        ContentValues cv = new ContentValues();
        cv.put(OBJECTS_IDENTIFICATOR, objectID);
        db.replace(OBJECTS_DB, null, cv);
    }

    void writeObject(String objectID, HashMap values, HashMap tables) {
        SQLiteDatabase db = getReadableDatabase();
        writeObject(db, objectID, values, tables);
    }

    private Cursor getAllNotifications(SQLiteDatabase db) {
        String[] projection = {
                NOTIFICATIONS_OBJECT,
                NOTIFICATIONS_TIME,
                NOTIFICATIONS_TYPE
        };

        String sortOrder =
                NOTIFICATIONS_ID + " ASC";
        return db.query(
                NOTIFICATIONS_TABLE,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }

    Cursor getHistory() {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                STAT_ID,
                STAT_DATE,
                STAT_BATTERY,
                STAT_LATITUDE,
                STAT_SENT,
                STAT_LONGITUDE
        };

        String sortOrder =
                STAT_DATE + " ASC";
        return db.query(
                STATUS_TABLE,  // The table to query
                projection,                               // The columns to return
                STAT_SENT + " = 0",                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }

    void writeNotifications(String objectID, long time, int type) {
        SQLiteDatabase db = getReadableDatabase();
        writeNotifications(db, objectID, time, type);
    }

    private void writeNotifications(SQLiteDatabase db, String objectID, long time, int type) {
        ContentValues cv = new ContentValues();
        cv.put(NOTIFICATIONS_OBJECT, objectID);
        cv.put(NOTIFICATIONS_TIME, time);
        cv.put(NOTIFICATIONS_TYPE, type);
        db.replace(NOTIFICATIONS_TABLE, null, cv);
    }

    void deleteNotifications(String objectID, int type) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(
                NOTIFICATIONS_TABLE,  // The table to query
                NOTIFICATIONS_OBJECT + " = ? and " + NOTIFICATIONS_TYPE + " = ?",
                new String[]{objectID, String.valueOf(type)}
        );
    }

    void writeToHistory(int battery, double latitude, double longitude) {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(STAT_DATE, Calendar.getInstance().getTimeInMillis());
        cv.put(STAT_BATTERY, battery);
        cv.put(STAT_LATITUDE, latitude);
        cv.put(STAT_LONGITUDE, longitude);
        cv.put(STAT_SENT, 0);
        db.insert(STATUS_TABLE, null, cv);
    }

    void markStatusAsSent(Cursor values) {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues cv = new ContentValues();
        if (values.moveToFirst()) {
            cv.put(STAT_DATE, values.getLong(values.getColumnIndexOrThrow(SqlDataManager.STAT_DATE)));
            cv.put(STAT_BATTERY, values.getInt(values.getColumnIndexOrThrow(SqlDataManager.STAT_BATTERY)));
            cv.put(STAT_LATITUDE, values.getDouble(values.getColumnIndexOrThrow(SqlDataManager.STAT_LATITUDE)));
            cv.put(STAT_LONGITUDE, values.getDouble(values.getColumnIndexOrThrow(SqlDataManager.STAT_LONGITUDE)));
            cv.put(STAT_SENT, 1);
            db.update(STATUS_TABLE, cv, "_ID = ?", new String[]{String.valueOf(values.getInt(values.getColumnIndexOrThrow(SqlDataManager.STAT_ID)))});
        }
        while (values.moveToNext()) {
            cv.put(STAT_DATE, values.getLong(values.getColumnIndexOrThrow(SqlDataManager.STAT_DATE)));
            cv.put(STAT_BATTERY, values.getInt(values.getColumnIndexOrThrow(SqlDataManager.STAT_BATTERY)));
            cv.put(STAT_LATITUDE, values.getDouble(values.getColumnIndexOrThrow(SqlDataManager.STAT_LATITUDE)));
            cv.put(STAT_LONGITUDE, values.getDouble(values.getColumnIndexOrThrow(SqlDataManager.STAT_LONGITUDE)));
            cv.put(STAT_SENT, 1);
            db.update(STATUS_TABLE, cv, "_ID = ?", new String[]{String.valueOf(values.getInt(values.getColumnIndexOrThrow(SqlDataManager.STAT_ID)))});
        }
        values.close();
    }

    void writeMessage(String message, String theme, String date) {
        SQLiteDatabase db = getReadableDatabase();
        writeMessage(db, message, theme, date);
    }

    private void writeMessage(SQLiteDatabase db, String message, String theme, String date) {
        ContentValues cv = new ContentValues();
        cv.put(MESSAGE_DATE, Long.parseLong(date));
        cv.put(MESSAGE_TEXT, message);
        cv.put(MESSAGE_THEME, theme);
        db.insert(MESSAGE_TABLE, null, cv);
    }

    void writeSoapLog(String command, String request, String result) {
        SQLiteDatabase db = getReadableDatabase();
        writeSoapLog(db, command, request, result);
    }

    private void writeSoapLog(SQLiteDatabase db, String command, String request, String result) {
        ContentValues cv = new ContentValues();
        cv.put(SOAP_LOG_DATE, Calendar.getInstance().getTimeInMillis());
        cv.put(SOAP_LOG_COMMAND, command);
        cv.put(SOAP_LOG_REQUEST, request);
        cv.put(SOAP_LOG_RESULT, result);
        db.insert(SOAP_LOG_TABLE, null, cv);
    }

    Cursor getMessages() {
        SQLiteDatabase db = getReadableDatabase();
        return getMessages(db);
    }

    private Cursor getMessages(SQLiteDatabase db) {

        String[] projection = {
                MESSAGE_ID,
                MESSAGE_DATE,
                MESSAGE_THEME,
                MESSAGE_TEXT
        };

        String sortOrder =
                MESSAGE_DATE + " DESC";
        return db.query(
                MESSAGE_TABLE,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }

    Cursor getSoapLog(String command) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                SOAP_LOG_DATE,
                SOAP_LOG_REQUEST,
                SOAP_LOG_COMMAND,
                SOAP_LOG_RESULT
        };

        String sortOrder =
                SOAP_LOG_DATE + " DESC LIMIT 100";
        String filter = null;
        String[] filterValue = null;
        if (command != null) {
            filter = SOAP_LOG_COMMAND + " = ?";
            filterValue = new String[]{command};
        }
        return db.query(
                SOAP_LOG_TABLE,  // The table to query
                projection,                               // The columns to return
                filter,                                // The columns for the WHERE clause
                filterValue,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }

    void clearLog() {
        SQLiteDatabase db = getReadableDatabase();
        db.delete(SOAP_LOG_TABLE, null, null);
    }


}
