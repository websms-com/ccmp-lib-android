package net.ut11.ccmp.lib.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import net.ut11.ccmp.lib.util.AttachmentCache;
import net.ut11.ccmp.lib.util.MD5Util;

import java.util.ArrayList;
import java.util.List;

public class MessagesDb extends BaseDb {

	private static final String TABLE_NAME = "messages";

	public static List<Message> getMessages() {
		List<Message> msgs = new ArrayList<Message>();

		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, Columns.DATE_SENT + " DESC, " + Columns._ID + " DESC");

		if (c != null) {
			while (c.moveToNext()) {
				msgs.add(getMessageFromCursor(c));
			}
			c.close();
		}

		return msgs;
	}

	public static List<Message> getUnreadMessages() {
		List<Message> msgs = new ArrayList<Message>();

		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, null, Columns.READ + "=0", null, null, null, Columns.DATE_SENT + " DESC, " + Columns._ID + " DESC");

		if (c != null) {
			while (c.moveToNext()) {
				msgs.add(getMessageFromCursor(c));
			}
			c.close();
		}

		return msgs;
	}

	public static List<Message> getResponseMessages(long id) {
		List<Message> msgs = new ArrayList<Message>();

		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, null, Columns.RESPONSE_FOR_ID + "=?", new String[]{ String.valueOf(id) }, null, null, Columns.DATE_SENT + " DESC, " + Columns._ID + " DESC");

		if (c != null) {
			while (c.moveToNext()) {
				msgs.add(getMessageFromCursor(c));
			}
			c.close();
		}

		return msgs;
	}

	public static Message getMessage(long id) {
		Message msg = null;

		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, null, Columns._ID + "=?", new String[]{ String.valueOf(id) }, null, null, null);

		if (c != null && c.moveToNext()) {
			msg = getMessageFromCursor(c);

            if (msg != null && msg.getAttachmentId() > 0) {
                long attachmentId = msg.getAttachmentId();
                Attachment attachment = AttachmentsDb.getAttachment(attachmentId);
                if (attachment == null) {
                    new Thread(new AttachmentCache.GetAttachmentRunnable(attachmentId)).start();
                }
            }

			c.close();
		}

		return msg;
	}

	public static List<Message> getOutgoingMessages() {
		List<Message> ret = new ArrayList<Message>();

		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, null, Columns.INCOMING + "=0", null, null, null, Columns.DATE_SENT + " DESC");

		if (c != null) {
			while (c.moveToNext()) {
				ret.add(getMessageFromCursor(c));
			}
			c.close();
		}

		return ret;
	}

	public static Message getMessage(long date, String sender, String message) {
		Message msg = null;

		String sel = Columns.INCOMING + "=1 AND " + Columns.DATE_SENT + ">? AND " + Columns.DATE_SENT + "<? AND " + Columns.ADDRESS + "=? AND " + Columns.MESSAGE_CHECKSUM + "=?";
		String[] selArgs = { String.valueOf(date - 30000), String.valueOf(date + 30000), sender, MD5Util.getMD5Sum(message) };

		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, null, sel, selArgs, null, null, null);

		if (c != null && c.moveToNext()) {
			msg = getMessageFromCursor(c);
			c.close();
		}

		return msg;
	}

	public static void saveMessage(Message msg) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = getValues(msg);

		if (msg.getId() > 0) {
			db.update(TABLE_NAME, values, Columns._ID + "=?", new String[]{ String.valueOf(msg.getId()) });
		} else {
			putNull(values, Columns.MESSAGE_CHECKSUM, MD5Util.getMD5Sum(msg.getMessage()));

			long id = db.insert(TABLE_NAME, null, values);
			if (id > 0) {
				msg.setId(id);
			}
		}
	}

	public static void deleteMessage(Message msg) {
		SQLiteDatabase db = getWritableDatabase();

		if (msg.getId() > 0) {
			db.delete(TABLE_NAME, Columns._ID + "=?", new String[]{ String.valueOf(msg.getId()) });
		}
	}

	public static void clear() {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_NAME, null, null);
	}

	private static ContentValues getValues(Message msg) {
		ContentValues values = new ContentValues();
		putNull(values, Columns._ID, msg.getId() > 0 ? msg.getId() : null);
        putNull(values, Columns.ACCOUNT_ID, msg.getAccountId());
        putNull(values, Columns.ATTACHMENT_ID, msg.getAttachmentId());
		putNull(values, Columns.MESSAGE_ID, msg.getMessageId() > 0 ? msg.getMessageId() : null);
		putNull(values, Columns.ADDRESS, msg.getAddress());
		putNull(values, Columns.MESSAGE, msg.getMessage());
		putNull(values, Columns.DATE_SENT, msg.getDateSent());
		putNull(values, Columns.INCOMING, msg.isIncoming() ? 1 : 0);
		putNull(values, Columns.IS_SMS, msg.isSms() ? 1 : 0);
        putNull(values, Columns.PUSH_PARAMETER, msg.getPushParameter());
        putNull(values, Columns.READ, msg.isRead() ? 1 : 0);
        putNull(values, Columns.RESPONSE_FOR_ID, msg.getResponseForId() >0 ? msg.getResponseForId() : null);

        return values;
	}

	private static Message getMessageFromCursor(Cursor c) {
		Message ret = new Message();
		ret.setId(c.getLong(c.getColumnIndexOrThrow(Columns._ID)));
        ret.setAttachmentId(c.getLong(c.getColumnIndexOrThrow(Columns.ATTACHMENT_ID)));
        ret.setAccountId(c.getLong(c.getColumnIndexOrThrow(Columns.ACCOUNT_ID)));
        ret.setMessageId(c.getLong(c.getColumnIndexOrThrow(Columns.MESSAGE_ID)));
        ret.setAddress(c.getString(c.getColumnIndexOrThrow(Columns.ADDRESS)));
        ret.setMessage(c.getString(c.getColumnIndexOrThrow(Columns.MESSAGE)));
        ret.setDateSent(c.getLong(c.getColumnIndexOrThrow(Columns.DATE_SENT)));
        ret.setIncoming(c.getInt(c.getColumnIndexOrThrow(Columns.INCOMING)) == 1);
        ret.setIsSms(c.getInt(c.getColumnIndexOrThrow(Columns.IS_SMS)) == 1);
        ret.setRead(c.getInt(c.getColumnIndexOrThrow(Columns.READ)) == 1);
        ret.setResponseForId(c.getLong(c.getColumnIndexOrThrow(Columns.RESPONSE_FOR_ID)));
        ret.setPushParameter(c.getString(c.getColumnIndexOrThrow(Columns.PUSH_PARAMETER)));
        ret.setExpired(c.getInt(c.getColumnIndexOrThrow(Columns.EXPIRED)) == 1);

		return ret;
	}

	private static class Columns implements BaseColumns {

        private static final String ACCOUNT_ID = "account_id";
        private static final String ATTACHMENT_ID = "attachment_id";
		private static final String ADDRESS = "address";
        private static final String MESSAGE_ID = "message_id";
        private static final String MESSAGE = "message";
		private static final String MESSAGE_CHECKSUM = "message_checksum";
		private static final String DATE_SENT = "date_sent";
		private static final String INCOMING = "incoming";
		private static final String IS_SMS = "is_sms";
        private static final String PUSH_PARAMETER = "push_parameter";
		private static final String READ = "read";
        private static final String RESPONSE_FOR_ID = "response_for_id";
        private static final String EXPIRED = "expired";
	}
}
