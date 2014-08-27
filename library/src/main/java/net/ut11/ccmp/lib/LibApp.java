package net.ut11.ccmp.lib;

import android.app.Application;
import android.content.Context;

import net.ut11.ccmp.lib.net.api.Api;
import net.ut11.ccmp.lib.net.api.client.ApiClient;
import net.ut11.ccmp.lib.receiver.DeviceUpdateReceiver;
import net.ut11.ccmp.lib.util.LibPreferences;
import net.ut11.ccmp.lib.util.Logger;
import net.ut11.ccmp.lib.util.MessageHandler;

public class LibApp extends Application {

	private static Context context;
	private static LibPreferences libPreferences;
    private static MessageHandler messageHandler;

	@Override
	public void onCreate() {
		super.onCreate();

		context = getApplicationContext();
		libPreferences = new LibPreferences(context);

		DeviceUpdateReceiver.checkConnected(context);

		initApiClient();
		initMessageHandler();
	}

	private void initApiClient() {
		try {
            if (getApplicationContext() != null) {
                Class<?> clazz = Class.forName(getApplicationContext().getString(R.string.apiClientClassName));
                ApiClient client = (ApiClient) clazz.newInstance();
                Api.setApiClient(client);
            }
		} catch (Exception e) {
			Logger.error("failed to init api client", e);
		}
	}

	private void initMessageHandler() {
		try {
            if (getApplicationContext() != null) {
                Class<?> clazz = Class.forName(getApplicationContext().getString(R.string.messageHandlerClassName));
                messageHandler = (MessageHandler) clazz.newInstance();
            }
		} catch (Exception e) {
			Logger.error("failed to init response id provider", e);
		}
	}

	public static Context getContext() {
		return context;
	}

	public static LibPreferences getLibPreferences() {
		return libPreferences;
	}

    public static MessageHandler getMessageHandler() {
        return messageHandler;
    }
}
