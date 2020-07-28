package com.example.tpbda;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DB_NAME = "Atm.db";
    private static final int DB_VERSION = 2;

    public DatabaseManager(Context context){

        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String strSql = "CREATE TABLE T_Score (" +
                " idScore integer primary key autoincrement, " +
                " name text not null, " +
                " score integer not null, " +
                " when_ integer not null" +
                ");";
        sqLiteDatabase.execSQL(strSql);
        Log.i("DATABASE", "onCreate invoked");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        this.onCreate(sqLiteDatabase);
    }
    public void insertScore(String name, int score){

        name = name.replace("'", "''");

        String strSql = "INSERT INTO T_Score(name, score, when_) " +
                " VALUES('" + name + "', " + score + ", " + new Date().getTime() + ")";

        this.getWritableDatabase().execSQL(strSql);
        Log.i("DATABASE", "onInsertScore invoked");
    }
    public ArrayList<String> getBank(){

        ArrayList<String> bank = new ArrayList<>();

        String strSql = "SELECT DISTINCT id, bank FROM atm WHERE bank != '' GROUP BY bank ORDER BY bank ASC";

        Cursor cursor =  this.getReadableDatabase().rawQuery(strSql, null);

        cursor.moveToFirst();

        while (! cursor.isAfterLast()){

            String element = new String(cursor.getString(1));

            bank.add(element);
            cursor.moveToNext();
        }
        cursor.close();

        return bank;
    }
    public ArrayList<LatLng> getAtmForBank(String _bank){

        ArrayList<LatLng> latlongAtm = new ArrayList<LatLng>();

        String strSql = "SELECT latitude, longitude FROM atm WHERE bank = '" + _bank + "'";

        Cursor cursor = this.getReadableDatabase().rawQuery(strSql, null);

        cursor.moveToFirst();

        while (! cursor.isAfterLast()){

            double latitude = cursor.getDouble(0);
            double longitude = cursor.getDouble(1);

            latlongAtm.add(new LatLng(latitude, longitude));

            cursor.moveToNext();
        }
        cursor.close();

        return latlongAtm;
    }
}
