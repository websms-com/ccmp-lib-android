package net.ut11.ccmp.lib.util;

import android.util.Log;

import net.ut11.ccmp.lib.BuildConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	public static final boolean DEBUG = BuildConfig.VERSION_NAME.endsWith("-SNAPSHOT");
	public static final boolean DEBUG_TIMESTAMPS = true;

	private static final String LOG_TAG = "ccmp-lib";

	private static SimpleDateFormat format;

	public static void debug(String message) {
		if (DEBUG_TIMESTAMPS) {
			if (format == null) {
				format = new SimpleDateFormat("dd/kkmm: ");
			}
			message = format.format(new Date()) + message;
		}

		Log.d(LOG_TAG, message);
	}

	public static void info(String message) {
		Log.i(LOG_TAG, message);
	}

	public static void warn(String message) {
		Log.w(LOG_TAG, message);
	}

	public static void error(String message, Exception e) {
		Log.e(LOG_TAG, message, e);
	}
}
