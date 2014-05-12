package net.ut11.ccmp.lib.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import net.ut11.ccmp.lib.LibApp;

public abstract class BaseDb {

	private static LibDbHelper helper = null;

	private static LibDbHelper getHelper() {
		if (helper == null) {
			helper = LibDbHelper.getInstance(LibApp.getContext());
		}
		return helper;
	}

	protected static SQLiteDatabase getWritableDatabase() {
		return getHelper().getWritableDatabase();
	}

	protected static SQLiteDatabase getReadableDatabase() {
		return getHelper().getReadableDatabase();
	}

	protected static void putNull(ContentValues values, String key, Object value) {
		if (value == null) {
			values.putNull(key);
		} else {
			if (value instanceof String) {
				values.put(key, (String) value);
			} else if (value instanceof Integer) {
				values.put(key, (Integer) value);
			} else if (value instanceof Long) {
				values.put(key, (Long) value);
			} else if (value instanceof byte[]) {
				values.put(key, (byte[]) value);
			} else if (value instanceof Uri) {
				values.put(key, value.toString());
			} else if (value.getClass().isEnum()) {
				values.put(key, value.toString());
			} else {
				throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
			}
		}
	}
}
