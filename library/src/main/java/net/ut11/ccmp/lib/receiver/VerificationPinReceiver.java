package net.ut11.ccmp.lib.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;

import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.util.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerificationPinReceiver extends BroadcastReceiver {

	public static final String INTENT_VERIFICATION_PIN_RECEIVED = "net.ut11.ccmp.lib.VERIFICATION_PIN_RECEIVED";
	public static final String INTENT_EXTRA_PIN = "verifyPin";

	private static final String INTENT_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final Pattern PIN_REGEX = Pattern.compile(LibApp.getLibPreferences().getPinRegex(), Pattern.CASE_INSENSITIVE);

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (INTENT_SMS_RECEIVED.equals(action)) {
			try {
				Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");

                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu);

                        if (handlePin(context, msg.getMessageBody())) {
                            if (pdus.length == 1) {
                                // only abort if no more messages stored in intent
                                abortBroadcast();
                            }
                        }
                    }
                }
			} catch(Exception e) {
				Logger.error("Failed to parse pin message", e);
			}
		}
	}

    public static boolean handlePin(Context context, String msg) {
        if (msg != null) {
            String pin = checkMessage(msg);

            if (pin != null) {
                sendPinReceivedBroadcast(context, pin);

                return true;
            }
        }

        return false;
    }

	private static String checkMessage(String body) {
		String pin = null;
		Matcher m = PIN_REGEX.matcher(body);

		if (m.find()) {
			pin = m.group(1);
		}

		return pin;
	}

	private static void sendPinReceivedBroadcast(Context context, String pin) {
		Intent i = new Intent(INTENT_VERIFICATION_PIN_RECEIVED);
		i.putExtra(INTENT_EXTRA_PIN, pin);

		LocalBroadcastManager.getInstance(context).sendBroadcast(i);
	}

	public static void setEnabled(boolean enabled) {
		Context context = LibApp.getContext();

		PackageManager pm = context.getPackageManager();
		ComponentName cn = new ComponentName(context, VerificationPinReceiver.class);
		int state = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        if (pm != null) {
            pm.setComponentEnabledSetting(cn, state, PackageManager.DONT_KILL_APP);
        }
	}
}
