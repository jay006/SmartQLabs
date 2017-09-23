package com.example.joker.sqltest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by joker on 24/8/17.
 */

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "QitDatabase.db";
    private static final String TABLE_NAME = "QUEUE";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_SHOP_NAME = "SHOP_NAME";
    static final String COLUMN_SHOP_INFO = "SHOP_INFO";
    private static final String COLUMN_URL = "URL";
    private static final String COLUMN_LAT = "LAT";
    private static final String COLUMN_LONG = "LONG";
    private static final String COLUMN_TIME = "TIME";
    private static final String COLUMN_PBY = "PBY";
    private static final String COLUMN_STORE_ID = "STORE_ID";
    private static final String COLUMN_STORE_NO = "STORE_NO";
    private static final String COLUMN_COUNTER_NO = "COUNTER_NO";
    private static final String COLUMN_QUEUE_NO = "QUEUE_NO";
    private static final String COLUMN_SANU_ID = "SANU_ID";
    private static final String COLUMN_CODE = "CODE";
    private static final String COLUMN_OTP = "OTP";
    private static final String COLUMN_QCODE = "QCODE";

    private SQLiteDatabase database;

    //super constructor
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //it will be called when database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table if not exists " + TABLE_NAME + " ( " +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_SHOP_NAME + " VARCHAR(45) NOT NULL," +
                COLUMN_SHOP_INFO + " VARCHAR(45) NOT NULL," +
                COLUMN_URL + " VARCHAR(100) NOT NULL," +
                COLUMN_LAT + " VARCHAR(100) NOT NULL," +
                COLUMN_LONG + " VARCHAR(100) NOT NULL," +
                COLUMN_TIME + " INTEGER NOT NULL," +
                COLUMN_PBY + " INTEGER NOT NULL," +
                COLUMN_STORE_ID + " VARCHAR(45) NOT NULL," +
                COLUMN_STORE_NO + " INTEGER NOT NULL," +
                COLUMN_COUNTER_NO + " INTEGER NOT NULL," +
                COLUMN_QUEUE_NO + " INTEGER NOT NULL," +
                COLUMN_SANU_ID + " VARCHAR(45) NOT NULL," +
                COLUMN_CODE + " INTEGER NOT NULL," +
                COLUMN_OTP + " VARCHAR(10) NOT NULL,"+
                COLUMN_QCODE + " VARCHAR(10) NOT NULL "+
                ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }


    //inserting records to the tabel with help of contentValues
    public void insertRecord(QueueModel queueModel) {
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_SHOP_NAME, queueModel.getShop_name());
        contentValues.put(COLUMN_SHOP_INFO, queueModel.getShop_info());
        contentValues.put(COLUMN_URL, queueModel.getUrl());
        contentValues.put(COLUMN_LAT, queueModel.getLat());
        contentValues.put(COLUMN_LONG, queueModel.getLog());
        contentValues.put(COLUMN_TIME, queueModel.getTime());
        contentValues.put(COLUMN_PBY, queueModel.getPby());
        contentValues.put(COLUMN_STORE_ID, queueModel.getStore_id());
        contentValues.put(COLUMN_STORE_NO, queueModel.getStore_no());
        contentValues.put(COLUMN_COUNTER_NO, queueModel.getCounter_no());
        contentValues.put(COLUMN_QUEUE_NO, queueModel.getQueue_no());
        contentValues.put(COLUMN_SANU_ID, queueModel.getSanuid());
        contentValues.put(COLUMN_OTP , queueModel.getOtp());
        contentValues.put(COLUMN_QCODE , queueModel.getqCode());
        contentValues.put(COLUMN_CODE, 0);

        database.insert(TABLE_NAME, null, contentValues);
        contentValues.clear();
        database.close();
    }

    //update Rows
    public void updateRecord(QueueModel queue) {
        database = this.getReadableDatabase();
        database.execSQL("update " + TABLE_NAME + " set " + COLUMN_TIME + " = '" + queue.getTime() + "', " + COLUMN_QUEUE_NO + " = '" + queue.getQueue_no() + "', " + COLUMN_CODE + " = '" + queue.getCode() + "',"+COLUMN_COUNTER_NO+" ='"+queue.getCounter_no()+"'  where " + COLUMN_ID + " = '" + queue.getID() + "'");
        database.close();
    }


    //delete Rows
    public void deleteRecord(QueueModel queue) {
        database = this.getReadableDatabase();
        database.execSQL("delete from " + TABLE_NAME + " where " + COLUMN_ID + " = '" + queue.getID() + "'");
        database.close();
    }

    //get all records
    public ArrayList<QueueModel> getAllRecords() {

        database = this.getReadableDatabase();
        // This is an alternate method for select * from Table <table_name>
        // Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_CODE + "=" + 0, null);
        ArrayList<QueueModel> contacts = new ArrayList<>();
        QueueModel contact;
        if (cursor.getCount() > 0) {

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                contact = new QueueModel();
                contact.setID(cursor.getString(0));
                contact.setShop_name(cursor.getString(1));
                contact.setShop_info(cursor.getString(2));
                contact.setUrl(cursor.getString(3));
                contact.setLat(cursor.getString(4));
                contact.setLog(cursor.getString(5));
                contact.setTime(cursor.getString(6));
                contact.setPby(cursor.getString(7));
                contact.setStore_id(cursor.getString(8));
                contact.setStore_no(cursor.getString(9));
                contact.setCounter_no(cursor.getString(10));
                contact.setQueue_no(cursor.getString(11));
                contact.setSanuid(cursor.getString(12));
                contact.setCode(cursor.getInt(13));
                contact.setOtp(cursor.getString(14));
                contact.setqCode(cursor.getString(15));

                contacts.add(contact);


            }

        }

        cursor.close();
        database.close();

        return contacts;

    }

}
