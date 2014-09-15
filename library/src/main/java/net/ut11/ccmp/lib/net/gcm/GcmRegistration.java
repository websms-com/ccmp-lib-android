package net.ut11.ccmp.lib.net.gcm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.R;
import net.ut11.ccmp.lib.net.api.endpoint.DeviceEndpoint;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.util.LibPreferences;
import net.ut11.ccmp.lib.util.Logger;

import java.io.IOException;

public class GcmRegistration {

    public static final String INTENT_GCM_REGISTRATION_ERROR = "net.ut11.ccmp.lib.GCM_REGISTRATION_ERROR";
    public static final String INTENT_GCM_REGISTRATION_SUCCESSFUL = "net.ut11.ccmp.lib.GCM_REGISTRATION_SUCCESSFUL";
    public static final String INTENT_EXTRA_RESULT_CODE = "resultCode";

	public static void checkRegistration() {
		final Context context = LibApp.getContext();
		final String senderId = context.getString(R.string.gcmSenderId);

		if (TextUtils.isEmpty(senderId)) {
			throw new IllegalArgumentException("gcmSenderId not set");
		}

		final LibPreferences prefs = LibApp.getLibPreferences();
		final int appVersion = getAppVersion();

		if (TextUtils.isEmpty(prefs.getGcmRegistrationId()) || appVersion > prefs.getGcmAppVersion()) {
			if (checkPlayServices(context)) {
				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

				try {
					String registrationId = gcm.register(senderId);
					prefs.setGcmRegistrationId(registrationId);
					prefs.setGcmAppVersion(appVersion);
					prefs.setGcmNeedsDeviceUpdate(true);

					if (prefs.isRegistered()) {
						DeviceEndpoint.updateDevice();
					}

                    Intent i = new Intent(INTENT_GCM_REGISTRATION_SUCCESSFUL);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);
				} catch (IOException e) {
					if (Logger.DEBUG) Logger.debug("gcm registration failed");
				} catch (ApiException e) {
					if (Logger.DEBUG) Logger.debug("device update failed");
				}
			}
		} else if (prefs.gcmNeedsDeviceUpdate()) {
			try {
				DeviceEndpoint.updateDevice();
			} catch (ApiException e) {
				if (Logger.DEBUG) Logger.debug("device update failed");
			}
		}
	}

	private static boolean checkPlayServices(Context context) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(LibApp.getContext());
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				Intent i = new Intent(INTENT_GCM_REGISTRATION_ERROR);
				i.putExtra(INTENT_EXTRA_RESULT_CODE, resultCode);

				LocalBroadcastManager.getInstance(context).sendBroadcast(i);
			}

			return false;
		}
		return true;
	}

	public static int getAppVersion() {
		try {
			Context context = LibApp.getContext();
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
}
