package net.ut11.ccmp.lib.net.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.ut11.ccmp.api.domain.DeviceInboxResponse;
import net.ut11.ccmp.lib.db.AccountsDb;
import net.ut11.ccmp.lib.db.Message;
import net.ut11.ccmp.lib.net.api.endpoint.DeviceEndpoint;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.util.Logger;
import net.ut11.ccmp.lib.util.MessageUtil;

public class GcmIntentService extends IntentService {

	private static final String EXTRA_MESSAGE_ID = "message_id";

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				if (Logger.DEBUG) Logger.debug("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				if (Logger.DEBUG) Logger.debug("Deleted messages on server: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				if (Logger.DEBUG) Logger.debug("Received: " + extras.toString());

				String messageIdStr = extras.getString(EXTRA_MESSAGE_ID);

				if (messageIdStr != null) {
					long messageId = Long.parseLong(messageIdStr);

					if (messageId > 0) {
						try {
							DeviceInboxResponse resp = DeviceEndpoint.getMessage(messageId);
                            AccountsDb.insert(resp.getAccountId(), resp.getReplyable(), resp.getSenderDisplayImage(), resp.getSenderDisplayName(), resp.getAccountTimestamp());
							Message msg = MessageUtil.getMessageFrom(resp);
							MessageUtil.insertMessage(msg);
						} catch (ApiException e) {
							Logger.error("failed to fetch message", e);
						}
					}
				}
			}
		}

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
}
