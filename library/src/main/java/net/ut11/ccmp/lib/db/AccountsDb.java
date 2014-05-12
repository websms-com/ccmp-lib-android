package net.ut11.ccmp.lib.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.provider.BaseColumns;

import net.ut11.ccmp.lib.util.AccountCache;
import net.ut11.ccmp.lib.util.Logger;

public class AccountsDb extends BaseDb {

	private static final String TABLE_NAME = "accounts";

    public static Account getAccount(long id) {
        Account account = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, Columns._ID + "=?", new String[]{ String.valueOf(id) }, null, null, null);

        if (c != null && c.moveToNext()) {
            account = getAccountFromCursor(c);
            c.close();
        }

        return account;
    }

	public static void insert(long id, boolean isReplyable, String avatarUrl, String displayName, long timeStamp) {
        boolean insert = true;
        Account account = AccountCache.getAccount(id);
        if (account != null && timeStamp == account.getTimeStamp()) {
            insert = false;
        }

        if (insert) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = getValues(id, isReplyable, avatarUrl, displayName, timeStamp);
            long _id = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (Logger.DEBUG) Logger.debug("Inserted account with id : " + _id);

            if (account != null) {
                AccountCache.removeFromCache(id);
            }
        }
	}

    public static void addAvatar(long accountId, byte[] avatar) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        putNull(values, Columns.AVATAR, avatar);

        db.update(TABLE_NAME, values, Columns._ID + " = ?", new String[]{String.valueOf(accountId)});
    }

    private static Account getAccountFromCursor(Cursor c) {
        Account ret = new Account();
        ret.setId(c.getLong(c.getColumnIndexOrThrow(Columns._ID)));
        ret.setReplyable(c.getInt(c.getColumnIndexOrThrow(Columns.IS_REPLYABLE)) == 1);
        ret.setAvatarUrl(c.getString(c.getColumnIndexOrThrow(Columns.AVATAR_URL)));
        ret.setDisplayName(c.getString(c.getColumnIndexOrThrow(Columns.DISPLAY_NAME)));
        ret.setTimeStamp(c.getLong(c.getColumnIndexOrThrow(Columns.TIME_STAMP)));
        byte[] avatar = c.getBlob(c.getColumnIndexOrThrow(Columns.AVATAR));
        if (avatar != null) {
            ret.setAvatar(BitmapFactory.decodeByteArray(avatar, 0, avatar.length));
        }

        return ret;
    }

	private static ContentValues getValues(long id, boolean isReplyable, String avatarUrl, String displayName, long timeStamp) {

        return getValues(id, isReplyable, avatarUrl, displayName, timeStamp, null);
	}

    private static ContentValues getValues(long id, boolean isReplyable, String avatarUrl, String displayName, long timeStamp, byte[] avatar) {
        ContentValues values = new ContentValues();
        putNull(values, Columns._ID, id);
        putNull(values, Columns.AVATAR_URL, avatarUrl);
        putNull(values, Columns.DISPLAY_NAME, displayName);
        putNull(values, Columns.TIME_STAMP, timeStamp);
        putNull(values, Columns.IS_REPLYABLE, isReplyable ? 1 : 0);
        if (avatar != null) putNull(values, Columns.AVATAR, avatar);

        return values;
    }

	public static class Columns implements BaseColumns {

		private static final String _ID = "_id";
        private static final String AVATAR = "avatar";
        private static final String AVATAR_URL = "avatar_url";
        private static final String DISPLAY_NAME = "display_name";
        private static final String TIME_STAMP = "time_stamp";
        private static final String IS_REPLYABLE = "is_replyable";
	}
}
