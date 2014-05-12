package net.ut11.ccmp.lib.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.telephony.SmsManager;

import net.ut11.ccmp.lib.LibApp;

import java.util.ArrayList;

public class SmsUtil {

	public static boolean sendSms(String recipient, String message) {
		Context context = LibApp.getContext();
		SmsManager sm = SmsManager.getDefault();
		ArrayList<String> bodyParts = sm.divideMessage(message);

		final Object lockObj = new Object();
		final SentReceiverThread t = new SentReceiverThread(lockObj, bodyParts.size());
		t.start();

		String scheme = SentReceiverThread.INTENT_SCHEME + "-" + System.currentTimeMillis();

		IntentFilter filter = new IntentFilter(SentReceiverThread.INTENT_ACTION_SMS_SENT);
		filter.addDataScheme(scheme);

		BroadcastReceiver r = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				t.onReceive(intent, getResultCode());
			}
		};
		context.registerReceiver(r, filter);

		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();

		for (int i = 0; i < bodyParts.size(); ++ i) {
			Intent sentIntent = new Intent(SentReceiverThread.INTENT_ACTION_SMS_SENT, Uri.parse(scheme + "://" + i));
			sentIntent.putExtra(SentReceiverThread.INTENT_EXTRA_LAST_PART, i == bodyParts.size() - 1);

			sentIntents.add(PendingIntent.getBroadcast(context, 0, sentIntent, 0));
		}

		sm.sendMultipartTextMessage(recipient, null, bodyParts, sentIntents, null);

		synchronized (lockObj) {
			// just wait for lock
		}

		context.unregisterReceiver(r);
		return t.isSent();
	}

	private static class SentReceiverThread extends Thread {

		private static final String INTENT_ACTION_SMS_SENT = "net.ut11.ccmp.lib.util.SMS_SENT";
		private static final String INTENT_SCHEME = "ccmp-sms-sent";
		private static final String INTENT_EXTRA_LAST_PART = "lastPart";

		private Object lockObj;
		private int parts;
		private int sentParts = 0;

		public SentReceiverThread(Object lockObj, int parts) {
			this.parts = parts;
			this.lockObj = lockObj;
		}

		public void onReceive(Intent intent, int resultCode) {
			if (resultCode == Activity.RESULT_OK) {
				++ sentParts;
			}

			if (intent.getBooleanExtra(INTENT_EXTRA_LAST_PART, false)) {
				interrupt();
			}
		}

		public boolean isSent() {
			return sentParts == parts;
		}

		@Override
		public void run() {
			synchronized (lockObj) {
				try {
					sleep(60000);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
