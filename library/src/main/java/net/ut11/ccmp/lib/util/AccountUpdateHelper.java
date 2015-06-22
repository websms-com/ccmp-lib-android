package net.ut11.ccmp.lib.util;

import net.ut11.ccmp.api.domain.DeviceConfigurationResponse;
import net.ut11.ccmp.lib.db.Account;
import net.ut11.ccmp.lib.db.AccountsDb;
import net.ut11.ccmp.lib.net.api.request.DeviceAccountConfigurationCall;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.net.api.response.ApiResponse;

import java.net.HttpURLConnection;
import java.util.List;


public class AccountUpdateHelper {

    private static boolean isAccountRefreshNecessary(long accountId, long timestamp) {
        Account account = AccountCache.getAccount(accountId);
        return !(account != null && timestamp == account.getTimeStamp());
    }

    public static void updateAccountData(long accountId, long lastRefreshTimestamp) {
        if (!isAccountRefreshNecessary(accountId, lastRefreshTimestamp)) {
            return;
        }

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

            AccountsDb.insert(accountId, replyable, senderDisplayImage, senderDisplayName, lastRefreshTimestamp);
        } catch (ApiException e) {
            Logger.warn("failed to refresh account information");
        }
    }
}
