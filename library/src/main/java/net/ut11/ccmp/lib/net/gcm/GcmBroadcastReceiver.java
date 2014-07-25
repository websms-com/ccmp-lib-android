package net.ut11.ccmp.lib.net.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.receiver.VerificationPinReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("type");
        if (type != null && type.equals("PIN") && LibApp.getLibPreferences().getDeviceToken() != null) {
            String pin = intent.getStringExtra("content");
            VerificationPinReceiver.handlePin(context, pin);
        } else if (LibApp.getLibPreferences().isRegistered()) {
			startWakefulService(context, intent.setClass(context, GcmIntentService.class));
			setResultCode(Activity.RESULT_OK);
		}
	}
}
