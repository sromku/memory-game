package com.snatik.matches.common;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings;

/**
 * Created by Dimitris on 25/8/2017.
 */
public class SQLiteDB extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "Memory_DB";
    public static final String TABLE_NAME = "Times";
    public static final String COLUMN_TIME = "StageTime";
    public static final String COLUMN_THEME = "Theme";
    public static final String COLUMN_DIFFICULTY = "Difficulty";
    public static String delQuery = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    public SQLiteDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db){

    }

    public void createTable() {
        SQLiteDatabase db = getWritableDatabase();
        String initQuery = "CREATE TABLE IF NOT EXISTS " +TABLE_NAME + " ( " + COLUMN_TIME + " INTEGER, " +
                COLUMN_THEME + " INTEGER, "  + COLUMN_DIFFICULTY + " INTEGER, PRIMARY KEY( " +COLUMN_THEME +", " +
                COLUMN_DIFFICULTY + ") );";

        System.out.println(db.isOpen());
        db.execSQL(initQuery);
        System.out.println(DB_NAME);

        System.out.println("EVERYTHING WENT ALRIGHT!!");
    }

    public void dropTable () {

        SQLiteDatabase db = getWritableDatabase();
        String delQuery = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

        db.execSQL(delQuery);

        db.close();
    }


    public void saveToTable(int theme,int difficulty, int passedSeconds) {

        SQLiteDB db = new SQLiteDB(Shared.context,null,null,1);
        int bestTime = db.getBestTimeForStage(theme,difficulty);

        if( bestTime != 0) {
            if (passedSeconds < bestTime) {
                String query = "UPDATE " + TABLE_NAME + "SET " +COLUMN_TIME + " = " +
                        ""+ passedSeconds + " WHERE " +COLUMN_THEME + " = " + theme +
                        " AND " + COLUMN_DIFFICULTY + " = " + difficulty + ";";
                db.getWritableDatabase().execSQL(query);
                System.out.println("INSERTED A LOWER TIME THAN EXISTING ONE");
            }
        } else {
            String query = "INSERT INTO " + TABLE_NAME + " VALUES ( " + passedSeconds+ "" +
                    ", " + theme + ", " + difficulty + ");";
            db.getWritableDatabase().execSQL(query);
            System.out.println("INSERTED A NEW VALUE FOR STAGE AND DIFFICULTY");
        }

        db.close();

    }


    public void deleteFromTable(int theme, int difficulty) {
        SQLiteDatabase db = getWritableDatabase();

        String delquery = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_THEME +"" +
                " = " + theme + " AND " + COLUMN_DIFFICULTY + " = " +difficulty
                +";";

        db.execSQL(delquery);

        System.out.println("DELETION COMPLETED SUCCESSFULLY!");

    }


    public int getBestTimeForStage(int theme, int difficulty) {

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT " + COLUMN_TIME + " FROM " + TABLE_NAME + " WHERE " +
                COLUMN_THEME + " = " + theme + " AND " + COLUMN_DIFFICULTY + " = " +
                difficulty + ";";

        Cursor c = db.rawQuery(query,null);

        if (c != null &&  c.moveToFirst()) {
            System.out.println("BETTER TIME THAN SAVED");
            c.getColumnIndex(COLUMN_TIME);
            return c.getInt(c.getColumnIndex(COLUMN_TIME));

        } else {
            return 0;
        }
    }


    /*public String checkForTable () {

        String result = null;
        SQLiteDatabase db = getWritableDatabase();

        String query = "SELECT name FROM sqlite_master WHERE type = 'table' AND name = " + TABLE_NAME + ";";
        db.execSQL(query);
        Cursor c = db.rawQuery(query,null);
        c.moveToFirst();
        if (c != null) {
           result = c.getString(c.getColumnIndex("name"));
        }

        db.close();
        return result;
    } */

    /*public String insertValues() {
        SQLiteDatabase db = getWritableDatabase();
        String query = "INSERT INTO " + TABLE_NAME +" VALUES (20, 2, 4);";
        db.execSQL(query);
        System.out.println("VALUES INSERTED ! ! ! !");
        String query1 = "SELECT " + COLUMN_THEME + "  FROM " + TABLE_NAME + " WHERE " + COLUMN_DIFFICULTY + " = 4;";
        Cursor c = db.rawQuery(query1,null);
        c.moveToFirst();
        return c.getString(c.getColumnIndex(COLUMN_THEME));
    } */

    @Override public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        System.out.println("HI");
        sqLiteDatabase.execSQL(delQuery);
        onCreate(sqLiteDatabase);
    }



}
