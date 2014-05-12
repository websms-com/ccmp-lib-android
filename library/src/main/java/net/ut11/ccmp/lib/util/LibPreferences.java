package net.ut11.ccmp.lib.util;

import android.content.Context;
import android.content.SharedPreferences;

public class LibPreferences {

	private SharedPreferences preferences;
	private Context context;

	private static final String PREFERENCE_MSISDN = "msisdn";
	private static final String PREFERENCE_DEVICE_TOKEN = "device_token";
	private static final String PREFERENCE_DEVICE_VERIFIED = "device_verified";
	private static final String PREFERENCE_GCM_APP_VERSION = "gcm_app_version";
	private static final String PREFERENCE_GCM_REGISTRATION_ID = "gcm_registration_id";
	private static final String PREFERENCE_GCM_NEEDS_DEVICE_UPDATE = "gcm_needs_device_update_push";
	private static final String PREFERENCE_LAST_DEVICE_UPDATE_TIME = "last_device_update_time";
	private static final String PREFERENCE_LAST_INBOX_UPDATE_TIME = "last_inbox_update_time";
	private static final String PREFERENCE_RECIPIENT_NATIONAL = "recipient_national";
	private static final String PREFERENCE_RECIPIENT_INTERNATIONAL = "recipient_international";
	private static final String PREFERENCE_MESSAGE_SENDER = "message_sender";
	private static final String PREFERENCE_PIN_REGEX = "pin_regex";
	private static final String PREFERENCE_ATTACHMENT_BASE_URL = "attachment_base_url";

	public LibPreferences(Context context) {
		this.preferences = context.getSharedPreferences("ccmp_lib_preferences", Context.MODE_PRIVATE);
		this.context = context;
	}

	public long getMsisdn() {
		return preferences.getLong(PREFERENCE_MSISDN, 0);
	}

	public void setMsisdn(long msisdn) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(PREFERENCE_MSISDN, msisdn);
		editor.commit();
	}

	public String getDeviceToken() {
		return preferences.getString(PREFERENCE_DEVICE_TOKEN, null);
	}

	public void setDeviceToken(String token) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_DEVICE_TOKEN, token);
		editor.commit();
	}

	public boolean isDeviceVerified() {
		return preferences.getBoolean(PREFERENCE_DEVICE_VERIFIED, false);
	}

	public void setDeviceVerified(boolean verified) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(PREFERENCE_DEVICE_VERIFIED, verified);
		editor.commit();
	}

	public boolean isRegistered() {
		return getDeviceToken() != null && isDeviceVerified();
	}

	public int getGcmAppVersion() {
		return preferences.getInt(PREFERENCE_GCM_APP_VERSION, 0);
	}

	public void setGcmAppVersion(int version) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(PREFERENCE_GCM_APP_VERSION, version);
		editor.commit();
	}

	public String getGcmRegistrationId() {
		return preferences.getString(PREFERENCE_GCM_REGISTRATION_ID, null);
	}

	public void setGcmRegistrationId(String registrationId) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_GCM_REGISTRATION_ID, registrationId);
		editor.commit();

		setGcmNeedsDeviceUpdate(true);
	}

	public boolean gcmNeedsDeviceUpdate() {
		return preferences.getBoolean(PREFERENCE_GCM_NEEDS_DEVICE_UPDATE, false);
	}

	public void setGcmNeedsDeviceUpdate(boolean needsUpdate) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(PREFERENCE_GCM_NEEDS_DEVICE_UPDATE, needsUpdate);
		editor.commit();
	}

	public long getLastDeviceUpdateTime() {
		long time = preferences.getLong(PREFERENCE_LAST_DEVICE_UPDATE_TIME, -1);
		if (time == -1) {
			time = System.currentTimeMillis();
			setLastDeviceUpdateTime(time);
		}
		return time;
	}

	public void setLastDeviceUpdateTime(long time) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(PREFERENCE_LAST_DEVICE_UPDATE_TIME, time);
		editor.commit();
	}

	public long getLastInboxUpdateTime() {
		return preferences.getLong(PREFERENCE_LAST_INBOX_UPDATE_TIME, -1);
	}

	public void setLastInboxUpdateTime(long time) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(PREFERENCE_LAST_INBOX_UPDATE_TIME, time);
		editor.commit();
	}

	public String getRecipientNational() {
		return preferences.getString(PREFERENCE_RECIPIENT_NATIONAL, null);
	}

	public void setRecipientNational(String recipient) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_RECIPIENT_NATIONAL, recipient);
		editor.commit();
	}

	public String getRecipientInternational() {
		return preferences.getString(PREFERENCE_RECIPIENT_INTERNATIONAL, null);
	}

	public void setRecipientInternational(String recipient) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_RECIPIENT_INTERNATIONAL, recipient);
		editor.commit();
	}

	public String getMessageSender() {
		return preferences.getString(PREFERENCE_MESSAGE_SENDER, null);
	}

	public void setMessageSender(String sender) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_MESSAGE_SENDER, sender);
		editor.commit();
	}

	public String getPinRegex() {
		return preferences.getString(PREFERENCE_PIN_REGEX, null);
	}

	public void setPinRegex(String regex) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_PIN_REGEX, regex);
		editor.commit();
	}

	public String getAttachmentBaseUrl() {
		return preferences.getString(PREFERENCE_ATTACHMENT_BASE_URL, null);
	}

	public void setAttachmentBaseUrl(String url) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_ATTACHMENT_BASE_URL, url);
		editor.commit();
	}
}
