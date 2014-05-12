package net.ut11.ccmp.lib.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import net.ut11.ccmp.lib.util.Logger;

public class AttachmentsDb extends BaseDb {

	private static final String TABLE_NAME = "attachments";

    public static Attachment getAttachment(long id) {
        Attachment attachment = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, Columns._ID + "=?", new String[]{ String.valueOf(id) }, null, null, null);

        if (c != null && c.moveToNext()) {
            attachment = getAttachmentFromCursor(c);
            c.close();
        }

        return attachment;
    }

	public static void insert(long id, String uri, String name, String mimeType, long size) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = getValues(id, uri, name, mimeType, size);
        long _id = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (Logger.DEBUG) Logger.debug("Inserted attachment with id : " + _id);
	}

    public static void deleteAttachment(long attachmentId) {
        SQLiteDatabase db = getWritableDatabase();

        if (attachmentId > 0) {
            db.delete(TABLE_NAME, Columns._ID + "=?", new String[]{ String.valueOf(attachmentId) });
        }
    }

    private static Attachment getAttachmentFromCursor(Cursor c) {
        Attachment ret = new Attachment();
        ret.setId(c.getLong(c.getColumnIndexOrThrow(Columns._ID)));
        ret.setMimeType(c.getString(c.getColumnIndexOrThrow(Columns.MIME_TYPE)));
        ret.setUri(c.getString(c.getColumnIndexOrThrow(Columns.URI)));
        ret.setName(c.getString(c.getColumnIndexOrThrow(Columns.NAME)));
        ret.setSize(c.getLong(c.getColumnIndexOrThrow(Columns.SIZE)));

        return ret;
    }

    private static ContentValues getValues(long id, String uri, String name, String mimeType, long size) {
        ContentValues values = new ContentValues();
        putNull(values, Columns._ID, id);
        putNull(values, Columns.URI, uri);
        putNull(values, Columns.NAME, name);
        putNull(values, Columns.MIME_TYPE, mimeType);
        putNull(values, Columns.SIZE, size);

        return values;
    }

	public static class Columns implements BaseColumns {

		private static final String _ID = "_id";
        private static final String MIME_TYPE = "mime_type";
        private static final String URI = "uri";
        private static final String NAME = "name";
        private static final String SIZE = "size";
	}
}
