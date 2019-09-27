package com.lzx.starrysky.ext

import android.database.Cursor
import android.provider.MediaStore

inline val Cursor.albumId: String
    get() = getString(getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID))

inline val Cursor.titleKey: String
    get() = getString(getColumnIndex(MediaStore.Audio.AudioColumns.TITLE_KEY))

inline val Cursor.artistKey: String
    get() = getString(getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST_KEY))

inline val Cursor.albumKey: String
    get() = getString(getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_KEY))

inline val Cursor.artist: String
    get() = getString(getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST))

inline val Cursor.album: String
    get() = getString(getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM))

inline val Cursor.data: String
    get() = getString(getColumnIndex(MediaStore.Audio.AudioColumns.DATA))

inline val Cursor.displayName: String
    get() = getString(getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME))

inline val Cursor.title: String
    get() = getString(getColumnIndex(MediaStore.Audio.AudioColumns.TITLE))

inline val Cursor.mimeType: String
    get() = getString(getColumnIndex(MediaStore.Audio.AudioColumns.MIME_TYPE))

inline val Cursor.year: String
    get() = getLong(getColumnIndex(MediaStore.Audio.AudioColumns.YEAR)).toString()

inline val Cursor.duration: Long
    get() = getLong(getColumnIndex(MediaStore.Audio.AudioColumns.DURATION))

inline val Cursor.size: String
    get() = getLong(getColumnIndex(MediaStore.Audio.AudioColumns.SIZE)).toString()

inline val Cursor.dateAdded: String
    get() = getLong(getColumnIndex(MediaStore.Audio.AudioColumns.DATE_ADDED)).toString()

inline val Cursor.dateModified: String
    get() = getLong(getColumnIndex(MediaStore.Audio.AudioColumns.DATE_MODIFIED)).toString()

