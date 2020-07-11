package com.ustc.location.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DataBase extends SQLiteOpenHelper {
    private static final String DB_NAME = "client.db";//数据库名
    private static final String Table_Name = "anchors";//锚点表

    private static final String Creat_table = "create table anchors(mac string primary key,posX double,posY double)";

    public DataBase(Context context) {
        super(context, DB_NAME, null, 2);
    }

    SQLiteDatabase db;

    //往anchors表中插入信息
    public void insertAnchors(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        db.insert(Table_Name, null, values);
        db.close();

    }
    //查询锚点是否已在表中
    public void updateAnchor(String anchor_mac) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(Table_Name,null,"mac=?",new String[]{anchor_mac},null,null,null);
        if(c!=null && c.getCount() >= 1){
            db.delete (Table_Name,"mac=?",new String[]{anchor_mac});
            db.close();
        }
        db.close();
    }
    //查询所有锚点信息
    public Cursor queryAnchors() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Table_Name, null, null, null, null, null, null);
        return cursor;

    }

    public void onCreate(SQLiteDatabase db) {

        this.db = db;
        db.execSQL(Creat_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL(Creat_table);
                break;
            default:

        }

    }

    //打开外键
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}
