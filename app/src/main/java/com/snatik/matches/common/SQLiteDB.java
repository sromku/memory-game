package com.snatik.matches.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Dimitris on 25/8/2017.
 */
public class SQLiteDB extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "Memory_DB";
    public static final String TABLE_NAME = "Times";
    public static final String COLUMN_TIME = "StageTime";
    public static final String COLUMN_ID = "Id";
    public static final String COLUMN_THEME = "Theme";
    public static final String COLUMN_DIFFICULTY = "Difficulty";

    public SQLiteDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        String initQuery = "CREATE TABLE " +DB_NAME + " ( " + COLUMN_TIME + " INTEGER, "
                + COLUMN_ID + "INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_THEME + " TEXT, "
        + COLUMN_DIFFICULTY + "INTEGER );";

        db.execSQL(initQuery);
        db.close();

        System.out.println("EVERYTHING WENT ALRIGHT!!");
    }

    public void dropTable (SQLiteDatabase db, String DB_NAME) {
        String delQuery = "DROP TABLE " + DB_NAME + ";";

        db.execSQL(delQuery);
    }

    /* public void insertBestTime(int passedSeconds, int theme, int difficulty) {

    } */

    @Override public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
