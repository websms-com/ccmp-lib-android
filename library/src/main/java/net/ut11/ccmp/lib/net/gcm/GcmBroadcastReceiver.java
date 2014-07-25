package net.ut11.ccmp.lib.net.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.receiver.VerificationPinReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String INTENT_EXTRA_TYPE = "type";
    private static final String INTENT_EXTRA_CONTENT = "content";
    private static final String CONTENT_TYPE_PIN = "PIN";

    @Override
	public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra(INTENT_EXTRA_TYPE);
        if (type != null && type.equals(CONTENT_TYPE_PIN) && LibApp.getLibPreferences().getDeviceToken() != null) {
            String pin = intent.getStringExtra(INTENT_EXTRA_CONTENT);
            VerificationPinReceiver.handlePin(context, pin);
        } else if (LibApp.getLibPreferences().isRegistered()) {
			startWakefulService(context, intent.setClass(context, GcmIntentService.class));
			setResultCode(Activity.RESULT_OK);
		}
	}
}
