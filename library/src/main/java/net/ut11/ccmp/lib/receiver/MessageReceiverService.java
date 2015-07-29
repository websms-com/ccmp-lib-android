package net.ut11.ccmp.lib.receiver;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsMessage;
import android.util.SparseArray;

import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.db.Message;
import net.ut11.ccmp.lib.util.Logger;
import net.ut11.ccmp.lib.util.MessageUtil;

import java.lang.reflect.Field;

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
						final SparseArray<SmsMessageWithConcatRef[]> messages = new SparseArray<SmsMessageWithConcatRef[]>();

                        for (Object pdu : pdus) {
                            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                            String address = sms.getOriginatingAddress();

                            if (address.matches(regex)) {
								SmsMessageWithConcatRef part = getSmsMessageWithConcatRef(sms);

								if (part.msgCount > 1) {
									try {
										SmsMessageWithConcatRef[] parts = messages.get(part.refNumber);
										if (parts == null) {
											parts = new SmsMessageWithConcatRef[part.msgCount];
											messages.put(part.refNumber, parts);
										}

										parts[part.seqNumber - 1] = part;
									} catch (Exception e) {
										Logger.warn("failed to handle multipart message");
										broadcastMessage(context, sms.getOriginatingAddress(), sms.getMessageBody(), sms.getTimestampMillis());
									}
								} else {
									broadcastMessage(context, sms.getOriginatingAddress(), sms.getMessageBody(), sms.getTimestampMillis());
								}

								if (!sms.getMessageClass().equals(SmsMessage.MessageClass.CLASS_0)) {
									++ handled;
								}
                            }
                        }

						// concat multipart messages
						for (int i = 0; i < messages.size(); ++ i) {
							SmsMessageWithConcatRef[] parts = messages.valueAt(i);

							String body = null;
							String address = null;
							long timestamp = 0;

							for (SmsMessageWithConcatRef part : parts) {
								if (part != null) {
									if (body == null) {
										body = part.smsMessage.getMessageBody();
										address = part.smsMessage.getOriginatingAddress();
										timestamp = part.smsMessage.getTimestampMillis();
									} else {
										body += part.smsMessage.getMessageBody();
									}
								}
							}

							broadcastMessage(context, address, body, timestamp);
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

		private void broadcastMessage(Context context, String address, String body, long timestamp) {
			Intent i = new Intent(context, MessageReceiverService.class);
			i.putExtra(INTENT_EXTRA_ADDRESS, address);
			i.putExtra(INTENT_EXTRA_MESSAGE, body);
			i.putExtra(INTENT_EXTRA_TIMESTAMP, timestamp);
			startWakefulService(context, i);
		}

		private SmsMessageWithConcatRef getSmsMessageWithConcatRef(SmsMessage smsMessage) {
			SmsMessageWithConcatRef smsWithHeader = new SmsMessageWithConcatRef();
			smsWithHeader.smsMessage = smsMessage;

			try {
				Field field = smsMessage.getClass().getField("mWrappedSmsMessage");
				Object mWrappedSmsMessage = field.get(smsMessage);

				field = null;
				for (Field field1 : mWrappedSmsMessage.getClass().getSuperclass().getDeclaredFields()) {
					if ("mUserDataHeader".equals(field1.getName())) {
						field = field1;
						break;
					}
				}

				if (field != null) {
					field.setAccessible(true);
					Object mUserDataHeader = field.get(mWrappedSmsMessage);

					field = null;
					for (Field field1 : mUserDataHeader.getClass().getDeclaredFields()) {
						if ("concatRef".equals(field1.getName())) {
							field = field1;
							break;
						}
					}

					if (field != null) {
						field.setAccessible(true);
						Object concatRef = field.get(mUserDataHeader);
						Class clazz = concatRef.getClass();

						smsWithHeader.msgCount = clazz.getField("msgCount").getInt(concatRef);
						smsWithHeader.seqNumber = clazz.getField("seqNumber").getInt(concatRef);
						smsWithHeader.refNumber = clazz.getField("refNumber").getInt(concatRef);
					}
				}
			} catch (Exception e) {
				Logger.warn("failed to get concatRef");
			}

			return smsWithHeader;
		}
	}

	private static class SmsMessageWithConcatRef {
		private SmsMessage smsMessage = null;

		private int refNumber = 1;
		private int seqNumber = 0;
		private int msgCount = 1;
	}
}
