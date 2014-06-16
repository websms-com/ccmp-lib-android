package net.ut11.ccmp.lib.receiver;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsMessage;

import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.db.Message;
import net.ut11.ccmp.lib.util.Logger;
import net.ut11.ccmp.lib.util.MessageUtil;

public class MessageReceiverService extends IntentService {

	private static final String INTENT_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

	private static final String INTENT_EXTRA_ADDRESS = "address";
	private static final String INTENT_EXTRA_MESSAGE = "message";
	private static final String INTENT_EXTRA_TIMESTAMP = "timestamp";

	public MessageReceiverService() {
		super("MessageReceiverService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String address = intent.getStringExtra(INTENT_EXTRA_ADDRESS);
		String message = intent.getStringExtra(INTENT_EXTRA_MESSAGE);
		long date = intent.getLongExtra(INTENT_EXTRA_TIMESTAMP, System.currentTimeMillis());

		Message msg = new Message();
		msg.setDateSent(date);
		msg.setIncoming(true);
		msg.setAddress(address);
		msg.setMessage(message);
		msg.setIsSms(true);
		msg.setRead(false);

		MessageUtil.insertMessage(msg);

		MessageReceiver.completeWakefulIntent(intent);
	}

	public static void setEnabled(boolean enabled) {
		Context context = LibApp.getContext();

		PackageManager pm = context.getPackageManager();
		ComponentName cn = new ComponentName(context, MessageReceiver.class);
		int state = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        if (pm != null) {
            pm.setComponentEnabledSetting(cn, state, PackageManager.DONT_KILL_APP);
        }
	}

	public static class MessageReceiver extends WakefulBroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String regex = LibApp.getLibPreferences().getMessageSender();

			if (INTENT_SMS_RECEIVED.equals(action) && regex != null) {
				try {
					Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");
                    int handled = 0;
                    if (pdus != null) {
                        for (Object pdu : pdus) {
                            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                            String address = sms.getOriginatingAddress();

                            if (address.matches(regex)) {
                                Intent i = new Intent(context, MessageReceiverService.class);
                                i.putExtra(INTENT_EXTRA_ADDRESS, address);
                                i.putExtra(INTENT_EXTRA_MESSAGE, sms.getMessageBody());
                                i.putExtra(INTENT_EXTRA_TIMESTAMP, sms.getTimestampMillis());
                                startWakefulService(context, i);

								if (!sms.getMessageClass().equals(SmsMessage.MessageClass.CLASS_0)) {
									++ handled;
								}
                            }
                        }

                        // only abort broadcast if we've handled all messages within this intent
                        if (handled == pdus.length) {
                            abortBroadcast();
                        }
                    }

				} catch(Exception e) {
					Logger.error("Failed to handle message", e);
				}
			}
		}
	}
}
