package net.ut11.ccmp.lib.net;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import net.ut11.ccmp.api.domain.DeviceInboxResponse;
import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.db.AccountsDb;
import net.ut11.ccmp.lib.db.Message;
import net.ut11.ccmp.lib.net.api.endpoint.DeviceEndpoint;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.net.gcm.GcmRegistration;
import net.ut11.ccmp.lib.util.LibPreferences;
import net.ut11.ccmp.lib.util.Logger;
import net.ut11.ccmp.lib.util.MessageUtil;

public class InboxUpdateService extends IntentService {

	public InboxUpdateService() {
		super("InboxUpdateService");
	}

	public static void startUpdate() {
		LibPreferences prefs = LibApp.getLibPreferences();
		long now = System.currentTimeMillis();

		if (now > prefs.getLastInboxUpdateTime() + 60000) {
			Context context = LibApp.getContext();
			context.startService(new Intent(context, InboxUpdateService.class));

			prefs.setLastInboxUpdateTime(now);
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			for (DeviceInboxResponse resp : DeviceEndpoint.getMessages()) {
                AccountsDb.insert(resp.getAccountId(), resp.getReplyable(), resp.getSenderDisplayImage(), resp.getSenderDisplayName(), resp.getAccountTimestamp());
				Message msg = MessageUtil.getMessageFrom(resp);
				MessageUtil.insertMessage(msg);
			}
		} catch (ApiException e) {
			Logger.warn("failed to update messages");
		}

		GcmRegistration.checkRegistration();
	}
}
