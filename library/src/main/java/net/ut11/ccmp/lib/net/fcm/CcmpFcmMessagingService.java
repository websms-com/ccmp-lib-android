package net.ut11.ccmp.lib.net.fcm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.ut11.ccmp.api.domain.DeviceInboxResponse;
import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.db.Message;
import net.ut11.ccmp.lib.net.api.endpoint.DeviceEndpoint;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.util.AccountUpdateHelper;
import net.ut11.ccmp.lib.util.LibPreferences;
import net.ut11.ccmp.lib.util.Logger;
import net.ut11.ccmp.lib.util.MessageUtil;

public class CcmpFcmMessagingService extends FirebaseMessagingService {

    public static final String INTENT_GCM_REGISTRATION_SUCCESSFUL = "net.ut11.ccmp.lib.GCM_REGISTRATION_SUCCESSFUL";
    private static final String EXTRA_MESSAGE_ID = "message_id";

    @Override
    public void onNewToken(String token) {

        if (Logger.DEBUG) Logger.debug("Token: " + token);

        final LibPreferences prefs = LibApp.getLibPreferences();
        final Context context = LibApp.getContext();

        prefs.setGcmRegistrationId(token);
        prefs.setGcmNeedsDeviceUpdate(true);

        try {
            if (prefs.isRegistered()) {
                DeviceEndpoint.updateDevice();
            }

            Intent i = new Intent(INTENT_GCM_REGISTRATION_SUCCESSFUL);
            LocalBroadcastManager.getInstance(context).sendBroadcast(i);

        } catch (ApiException e) {
            if (Logger.DEBUG) Logger.debug("device update failed");
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() == 0) {
            return;
        }

        if (Logger.DEBUG) Logger.debug("Received: " + remoteMessage.toString());

        String messageIdStr = remoteMessage.getData().get(EXTRA_MESSAGE_ID);

        if (messageIdStr != null) {
            long messageId = Long.parseLong(messageIdStr);

            if (messageId > 0) {
                try {
                    DeviceInboxResponse resp = DeviceEndpoint.getMessage(messageId, false);
                    AccountUpdateHelper.updateAccountData(resp.getAccountId(), resp.getAccountTimestamp());

                    Message msg = MessageUtil.getMessageFrom(resp);
                    if(MessageUtil.insertMessage(msg)) {
                        DeviceEndpoint.getMessage(messageId, true);
                    }
                } catch (ApiException e) {
                    Logger.error("failed to get/fetch message", e);
                }
            }
        }

    }
}