package net.ut11.ccmp.lib.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LibDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "ccmplib.db";
	private static final int DATABASE_VERSION = 5;

	private static LibDbHelper instance;

	public static LibDbHelper getInstance(Context context) {
		if (instance == null) {
			instance = new LibDbHelper(context);
		}
		return instance;
	}

	private LibDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createMessagesTable(db);
        createAccountsTable(db);
        createAttachmentsTable(db);
	}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 1 && newVersion > 1) {
            addAccountFeatures(db);
            createAccountsTable(db);
            createAttachmentsTable(db);
        }

		if (oldVersion <= 2 && newVersion > 2) {
			addExpired(db);
		}

		if (oldVersion <= 3 && newVersion > 3) {
			addPriority(db);
		}

		if (oldVersion <= 3 && newVersion > 3) {
			addPriority(db);
		}

		if (oldVersion <= 4 && newVersion > 4) {
			updateMessageProperties(db);
		}
    }

	private void createMessagesTable(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE messages (" +
				"  _id                 INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "  account_id          INTEGER DEFAULT (0)," +
                "  attachment_id       INTEGER DEFAULT (0)," +
				"  message_id          INTEGER UNIQUE," +
				"  address             TEXT NOT NULL," +
				"  message             TEXT NOT NULL," +
				"  message_checksum    TEXT NOT NULL," +
				"  date_sent           INTEGER NOT NULL," +
				"  incoming            INTEGER DEFAULT (0)," +
				"  is_sms              INTEGER DEFAULT (0)," +
				"  read                INTEGER DEFAULT (0)," +
				"  response_for_id     INTEGER," +
                "  push_parameter      TEXT," +
                "  expired             INTEGER DEFAULT (0)," +
				"  priority            INTEGER DEFAULT (-1)," +
				"  is_replyable        INTEGER DEFAULT (0)" +
				")"
		);
	}

    private void createAccountsTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE accounts (" +
                        "  _id                 INTEGER PRIMARY KEY NOT NULL," +
                        "  avatar_url          TEXT," +
                        "  display_name        TEXT," +
                        "  avatar              BLOB," +
                        "  time_stamp          INTEGER NOT NULL," +
                        "  is_replyable        INTEGER DEFAULT (0)" +
                        ")"
        );
    }

    private void createAttachmentsTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE attachments (" +
                        "  _id                 INTEGER PRIMARY KEY NOT NULL," +
                        "  uri                 TEXT," +
                        "  mime_type           TEXT," +
                        "  name                TEXT," +
                        "  size                INTEGER DEFAULT(0)" +
                        ")"
        );
    }

	private void addAccountFeatures(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE messages ADD " +
						"  attachment_id				INTEGER DEFAULT (0)"
		);
		db.execSQL("ALTER TABLE messages ADD " +
						"  push_parameter			    TEXT"
		);
		db.execSQL("ALTER TABLE messages ADD " +
						"  account_id					INTEGER"
		);
	}

	private void addExpired(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE messages ADD " +
						"  expired				INTEGER DEFAULT (0)"
		);
	}

	private void addPriority(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE messages ADD  priority INTEGER DEFAULT (-1)"
		);
	}

	private void updateMessageProperties(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE messages ADD  is_replyable INTEGER DEFAULT (0)"
		);

        db.execSQL("UPDATE messages SET account_id = (SELECT mp.account_id FROM messages mp WHERE" +
                        " messages.response_for_id = mp._id) WHERE account_id = 0 OR account_id IS NULL"
        );

		db.execSQL("UPDATE messages SET is_replyable = (SELECT a.is_replyable FROM accounts a WHERE" +
                        " messages.account_id = a._id) WHERE incoming = 1"
		);
	}
}
