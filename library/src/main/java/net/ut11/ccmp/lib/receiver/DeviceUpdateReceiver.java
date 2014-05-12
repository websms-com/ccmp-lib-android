package net.ut11.ccmp.lib.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.net.api.endpoint.DeviceEndpoint;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.net.gcm.GcmRegistration;
import net.ut11.ccmp.lib.util.Logger;

public class DeviceUpdateReceiver extends BroadcastReceiver {

	private static final long WAKE_INTERVAL_UPDATE = AlarmManager.INTERVAL_DAY;

	private static final String WAKE_DEVICE_UPDATE = "net.ut11.ccmp.lib.WAKE_DEVICE_UPDATE";
	private static final Intent updateIntent = new Intent(WAKE_DEVICE_UPDATE);

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			checkConnected(context);
		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			boolean connected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
			handleConnected(context, connected);
		} else if (WAKE_DEVICE_UPDATE.equals(action)) {
			updateDevice();
		}
	}

	public static void checkConnected(Context context) {
		handleConnected(context, isConnected(context));
	}

	private static void handleConnected(Context context, boolean connected) {
		if (!LibApp.getLibPreferences().isRegistered()) {
			return;
		}

		if (connected) {
			startUpdateAlarm(context);
		} else {
			stopUpdateAlarm(context);
		}
	}

	private static void startUpdateAlarm(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		long time = LibApp.getLibPreferences().getLastDeviceUpdateTime() + WAKE_INTERVAL_UPDATE;

		am.setRepeating(AlarmManager.RTC_WAKEUP, time, WAKE_INTERVAL_UPDATE, getUpdatePendingIntent(context));
	}

	private static void stopUpdateAlarm(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(getUpdatePendingIntent(context));
	}

	private static PendingIntent getUpdatePendingIntent(Context context) {
		return PendingIntent.getBroadcast(context, 0, updateIntent, 0);
	}

	public static boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();

		return info != null && info.isConnected();
	}

	private static void updateDevice() {
		new Thread() {
			@Override
			public void run() {
				GcmRegistration.checkRegistration();

				try {
					DeviceEndpoint.updateClientConfiguration();
					DeviceEndpoint.updateDevice();
				} catch (ApiException e) {
					Logger.warn("failed to update device");
				}
			}
		}.start();
	}
}
