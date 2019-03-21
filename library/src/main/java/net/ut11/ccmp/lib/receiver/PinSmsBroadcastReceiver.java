package net.ut11.ccmp.lib.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.util.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by johannes on 08.02.19.
 */

public class PinSmsBroadcastReceiver extends BroadcastReceiver {

    public static final String INTENT_VERIFICATION_PIN_RECEIVED = "net.ut11.ccmp.lib.VERIFICATION_PIN_RECEIVED";
    public static final String INTENT_EXTRA_PIN = "verifyPin";

    private static final Pattern PIN_REGEX = Pattern.compile(LibApp.getLibPreferences().getPinRegex(), Pattern.CASE_INSENSITIVE);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            switch(status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    Logger.debug("PINSMS RECEIVED");
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    if (handlePin(context, message)) {
                        abortBroadcast();
                    }
                    break;
                case CommonStatusCodes.TIMEOUT:
                    Logger.debug("No pin received");
                    break;
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
        i.setPackage(context.getPackageName());
        i.putExtra(INTENT_EXTRA_PIN, pin);

        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    public static void setEnabled(boolean enabled) {
        Context context = LibApp.getContext();

        PackageManager pm = context.getPackageManager();
        ComponentName cn = new ComponentName(context, PinSmsBroadcastReceiver.class);
        int state = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        if (pm != null) {
            pm.setComponentEnabledSetting(cn, state, PackageManager.DONT_KILL_APP);
        }
    }
}
