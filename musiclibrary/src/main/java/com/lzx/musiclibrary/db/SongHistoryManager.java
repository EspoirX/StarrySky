package com.lzx.musiclibrary.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放进度管理
 * Created by xian on 2018/3/31.
 */

public class SongHistoryManager {
    public static final String TABLE_HISTORY = "table_history";
    public static final String SONG_ID = "song_id";
    public static final String SONG_POSITION = "song_position";

    private DBHelper helper;

    public SongHistoryManager(Context context) {
        helper = DBHelper.getInstance(context);
    }

    private volatile static SongHistoryManager instance;

    public synchronized static SongHistoryManager getImpl(Context context) {
        if (instance == null) {
            synchronized (SongHistoryManager.class) {
                if (instance == null) {
                    instance = new SongHistoryManager(context);
                }
            }
        }
        return instance;
    }

    public boolean hasSongHistory(String songId) {
        synchronized (SongHistoryManager.class) {
            if (TextUtils.isEmpty(songId)) {
                return false;
            } else {
                boolean result = false;
                List<SongProgress> progressList = findAllSongProgress();
                for (SongProgress progress : progressList) {
                    if (songId.equals(progress.songId)) {
                        result = true;
                        break;
                    }
                }
                return result;
            }
        }
    }

    public void saveSongHistory(String songId, int progress) {
        if (TextUtils.isEmpty(songId)) {
            return;
        }
        synchronized (SongHistoryManager.class) {
            if (hasSongHistory(songId)) {
                updateMusicHistory(songId, progress);
            } else {
                insertMusicHistory(songId, progress);
            }
        }
    }

    public int findSongProgressById(String songId) {
        synchronized (SongHistoryManager.class) {
            if (TextUtils.isEmpty(songId)) {
                return -1;
            } else {
                int result = 0;
                List<SongProgress> progressList = findAllSongProgress();
                for (SongProgress progress : progressList) {
                    if (songId.equals(progress.songId)) {
                        result = progress.historyProgress;
                        break;
                    }
                }
                return result;
            }
        }
    }

    public int deleteSongProgressById(String songId) {
        synchronized (SongHistoryManager.class) {
            if (TextUtils.isEmpty(songId)) {
                return -1;
            } else if (!hasSongHistory(songId)) {
                return -1;
            } else {
                SQLiteDatabase db = helper.getReadableDatabase();
                if (db.isOpen()) {
                    return db.delete(TABLE_HISTORY, SONG_ID + " = ? ", new String[]{songId});
                } else {
                    return -1;
                }
            }
        }
    }

    public int clearAllSongProgress() {
        SQLiteDatabase db = helper.getReadableDatabase();
        if (db.isOpen()) {
            return db.delete(TABLE_HISTORY, null, null);
        } else {
            return -1;
        }
    }

    private void insertMusicHistory(String musicId, int progress) {
        synchronized (SongHistoryManager.class) {
            if (TextUtils.isEmpty(musicId)) {
                return;
            }
            SQLiteDatabase db = helper.getReadableDatabase();
            if (db.isOpen()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(SONG_ID, musicId);
                contentValues.put(SONG_POSITION, progress);
                db.insert(TABLE_HISTORY, null, contentValues);
            }
        }
    }

    private void updateMusicHistory(final String songId, final int progress) {
        synchronized (SongHistoryManager.class) {
            if (TextUtils.isEmpty(songId)) {
                return;
            }
            SQLiteDatabase db = helper.getWritableDatabase();
            if (db.isOpen()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(SONG_POSITION, String.valueOf(progress));
                int line = db.update(TABLE_HISTORY, contentValues, SONG_ID + " = ? ",
                        new String[]{songId});
            }
        }
    }

    private List<SongProgress> findAllSongProgress() {
        synchronized (SongHistoryManager.class) {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<SongProgress> list = new ArrayList<>();
            if (db.isOpen()) {
                try {
                    Cursor cursor = db.query(TABLE_HISTORY, null,
                            null, null,
                            null, null, null, null);
                    while (cursor.moveToNext()) {
                        SongProgress progress = new SongProgress();
                        progress.songId = cursor.getString(cursor.getColumnIndex(SONG_ID));
                        progress.historyProgress = cursor.getInt(cursor.getColumnIndex(SONG_POSITION));
                        list.add(progress);
                    }
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return list;
        }
    }

    private static class SongProgress {
        public String songId;
        public int historyProgress;
    }

}
