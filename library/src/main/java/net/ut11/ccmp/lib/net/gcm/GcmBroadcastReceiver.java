package net.ut11.ccmp.lib.net.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import net.ut11.ccmp.lib.LibApp;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (LibApp.getLibPreferences().isRegistered()) {
			startWakefulService(context, intent.setClass(context, GcmIntentService.class));
			setResultCode(Activity.RESULT_OK);
		}
	}
}
