package com.lzx.musiclibrary.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by xian on 2018/3/31.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String name = "musiclibrary";
    private static final int version = 1;
    private static volatile DBHelper instance;

    public DBHelper(Context context) {
        super(context, name, null, version);
    }

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DBHelper.class) {
                if (instance == null) {
                    instance = new DBHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private final String TABLE_MUSIC_HISTORY = "create table "
            + SongHistoryManager.TABLE_HISTORY + " ( "
            + SongHistoryManager.SONG_ID + " text, "
            + SongHistoryManager.SONG_POSITION + " text);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_MUSIC_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.delete(TABLE_MUSIC_HISTORY, null, null);
    }
}
