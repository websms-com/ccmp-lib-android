package net.ut11.ccmp.lib.net;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.ut11.ccmp.api.domain.DeviceConfigurationResponse;
import net.ut11.ccmp.api.domain.DeviceInboxResponse;
import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.db.Account;
import net.ut11.ccmp.lib.db.AccountsDb;
import net.ut11.ccmp.lib.db.Message;
import net.ut11.ccmp.lib.net.api.endpoint.DeviceEndpoint;
import net.ut11.ccmp.lib.net.api.request.DeviceAccountConfigurationCall;
import net.ut11.ccmp.lib.net.api.request.DeviceInboxFetchCall;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.net.api.response.ApiResponse;
import net.ut11.ccmp.lib.net.gcm.GcmRegistration;
import net.ut11.ccmp.lib.util.AccountCache;
import net.ut11.ccmp.lib.util.LibPreferences;
import net.ut11.ccmp.lib.util.Logger;
import net.ut11.ccmp.lib.util.MessageUtil;

import java.net.HttpURLConnection;
import java.util.List;

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
                if (accountRefreshNeeded(resp.getAccountId(), resp.getAccountTimestamp())) {
                    refreshAccount(resp.getAccountId(), resp.getAccountTimestamp());
                }

				Message msg = MessageUtil.getMessageFrom(resp);
				MessageUtil.insertMessage(msg);
			}
		} catch (ApiException e) {
			Logger.warn("failed to update messages");
		}

		GcmRegistration.checkRegistration();
	}

    private void refreshAccount(long accountId, long refreshTimestamp) {
        try {
            DeviceAccountConfigurationCall call = new DeviceAccountConfigurationCall(accountId);
            ApiResponse<DeviceConfigurationResponse> response = call.get();

            if (response.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Logger.warn(String.format("account information refresh failed with non-ok http status %d", response.getResponseCode()));
                return;
            }

            boolean replyable = false;
            String senderDisplayName = null;
            String senderDisplayImage = null;

            List<DeviceConfigurationResponse> configuration = response.getResponseArray(DeviceConfigurationResponse.class);
            for (DeviceConfigurationResponse config : configuration) {
                if (DeviceAccountConfigurationCall.KEY_SENDER_DISPLAY_NAME.equals(config.getKey())) {
                    senderDisplayName = config.getValue();
                } else if (DeviceAccountConfigurationCall.KEY_SENDER_DISPLAY_IMAGE.equals(config.getKey())) {
                    senderDisplayImage = config.getValue();
                } else if (DeviceAccountConfigurationCall.KEY_DEFAULT_REPLYABLE.equals(config.getKey())) {
                    replyable = Boolean.parseBoolean(config.getValue());
                }
            }

            AccountsDb.insert(accountId, replyable, senderDisplayImage, senderDisplayName, refreshTimestamp);
        } catch (ApiException e) {
            Logger.warn("failed to refresh account information");
        }
    }

    private boolean accountRefreshNeeded(long accountId, long timestamp) {
        Account account = AccountCache.getAccount(accountId);
        return !(account != null && timestamp == account.getTimeStamp());
    }
}
