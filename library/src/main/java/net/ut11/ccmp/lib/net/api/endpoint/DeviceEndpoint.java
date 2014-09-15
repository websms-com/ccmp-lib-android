package net.ut11.ccmp.lib.net.api.endpoint;

import android.text.TextUtils;
import android.util.Base64;

import net.ut11.ccmp.api.domain.ClientConfigurationResponse;
import net.ut11.ccmp.api.domain.DeviceAttachmentRequest;
import net.ut11.ccmp.api.domain.DeviceAttachmentResponse;
import net.ut11.ccmp.api.domain.DeviceInboxResponse;
import net.ut11.ccmp.api.domain.DeviceOutboxRequest;
import net.ut11.ccmp.api.domain.DeviceRegistrationRequest;
import net.ut11.ccmp.api.domain.DeviceRequest;
import net.ut11.ccmp.api.domain.DeviceResponse;
import net.ut11.ccmp.api.domain.Response;
import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.net.api.request.DeviceCall;
import net.ut11.ccmp.lib.net.api.request.DeviceClientConfigurationCall;
import net.ut11.ccmp.lib.net.api.request.DeviceGetAttachmentCall;
import net.ut11.ccmp.lib.net.api.request.DeviceInboxFetchCall;
import net.ut11.ccmp.lib.net.api.request.DeviceOutboxCall;
import net.ut11.ccmp.lib.net.api.request.DeviceRegistrationCall;
import net.ut11.ccmp.lib.net.api.request.DeviceUploadAttachmentCall;
import net.ut11.ccmp.lib.net.api.request.DeviceVerifyCall;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.net.api.response.ApiResponse;
import net.ut11.ccmp.lib.net.gcm.GcmRegistration;
import net.ut11.ccmp.lib.receiver.DeviceUpdateReceiver;
import net.ut11.ccmp.lib.receiver.MessageReceiverService;
import net.ut11.ccmp.lib.util.LibPreferences;
import net.ut11.ccmp.lib.util.Logger;

import java.net.HttpURLConnection;
import java.util.List;

public class DeviceEndpoint {

	private static final String KEY_RECIPIENT_NATIONAL = "RECIPIENT_NATIONAL";
	private static final String KEY_RECIPIENT_INTERNATIONAL = "RECIPIENT_INTERNATIONAL";
	private static final String KEY_SENDER = "SENDER";
	private static final String KEY_PIN_SMS_TEMPLATE = "PIN_SMS_TEMPLATE";
	private static final String KEY_ATTACHMENT_BASE_URL = "ATTACHMENT_BASE_URL";

	public static boolean registerDevice(long msisdn) throws ApiException {
		return registerDevice(msisdn, true);
	}

	public static boolean registerDevice(long msisdn, boolean sendSms) throws ApiException {
		if (msisdn <= 99999) {
			throw new IllegalArgumentException("msisdn is too short");
		}

		updateClientConfiguration();

		MessageReceiverService.setEnabled(false);

		DeviceRegistrationRequest req = new DeviceRegistrationRequest();
		req.setMsisdn(msisdn);

		DeviceRegistrationCall call = new DeviceRegistrationCall(req, sendSms);
		ApiResponse<Response> response = call.post();

		if (response.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
			LibPreferences prefs = LibApp.getLibPreferences();
			prefs.setDeviceToken(response.getReturnedId());
			prefs.setMsisdn(msisdn);

			return true;
		}

		throw new ApiException(response, null);
	}

    public static boolean registerDevice(String pushId) throws ApiException {
        updateClientConfiguration();

        MessageReceiverService.setEnabled(false);

        DeviceRegistrationRequest req = new DeviceRegistrationRequest();
        req.setPushId(pushId);

        DeviceRegistrationCall call = new DeviceRegistrationCall(req, true);
        ApiResponse<Response> response = call.post();

        if (response.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
            LibPreferences prefs = LibApp.getLibPreferences();
            prefs.setDeviceToken(response.getReturnedId());

            return true;
        }

        throw new ApiException(response, null);
    }

    public static boolean verifyPin(String pin) throws ApiException {
		if (pin.length() < 4) {
			if (Logger.DEBUG) Logger.debug("pin " + pin + " is too short");
			throw new IllegalArgumentException("pin is too short");
		}

		DeviceVerifyCall call = new DeviceVerifyCall(getToken(), pin);
		ApiResponse<Response> response = call.post();

		if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
			LibPreferences prefs = LibApp.getLibPreferences();
			prefs.setDeviceVerified(true);
			DeviceUpdateReceiver.checkConnected(LibApp.getContext());
			MessageReceiverService.setEnabled(true);
			GcmRegistration.checkRegistration();

			return true;
		}

		throw new ApiException(response, null);
	}

	public static DeviceResponse getDevice() throws ApiException {
		DeviceCall call = new DeviceCall();
		call.setToken(getToken());
		ApiResponse<DeviceResponse> response = call.get();

		if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return response.getResponseObject(DeviceResponse.class);
		}

		throw new ApiException(response, null);
	}

	public static DeviceResponse updateDevice() throws ApiException {
		LibPreferences prefs = LibApp.getLibPreferences();

		DeviceRequest req = new DeviceRequest();
		req.setMsisdn(prefs.getMsisdn());
		req.setPushId(prefs.getGcmRegistrationId());
		req.setEnabled(true);

		DeviceCall call = new DeviceCall(req);
		call.setToken(getToken());
		ApiResponse<DeviceResponse> response = call.put();

		if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
			prefs.setGcmNeedsDeviceUpdate(false);
			prefs.setLastDeviceUpdateTime(System.currentTimeMillis());
			return response.getResponseObject(DeviceResponse.class);
		}

		throw new ApiException(response, null);
	}

	public static DeviceInboxResponse getMessage(long messageId) throws ApiException {
		DeviceInboxFetchCall call = new DeviceInboxFetchCall(getToken(), messageId);
		ApiResponse<DeviceInboxResponse> response = call.post();

		if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return response.getResponseObject(DeviceInboxResponse.class);
		}

		throw new ApiException(response, null);
	}

	public static List<DeviceInboxResponse> getMessages() throws ApiException {
		DeviceInboxFetchCall call = new DeviceInboxFetchCall(getToken());
		ApiResponse<DeviceInboxResponse> response = call.post();

		if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return response.getResponseArray(DeviceInboxResponse.class);
		}

		throw new ApiException(response, null);
	}

	private static String getToken() {
		LibPreferences prefs = LibApp.getLibPreferences();
		String token = prefs.getDeviceToken();

		if (TextUtils.isEmpty(token)) {
			throw new IllegalArgumentException("device token is empty");
		}

		return token;
	}

	public static boolean updateClientConfiguration() throws ApiException {
		DeviceClientConfigurationCall call = new DeviceClientConfigurationCall();
		ApiResponse<ClientConfigurationResponse> response = call.get();

		if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
			LibPreferences prefs = LibApp.getLibPreferences();
			for (ClientConfigurationResponse config : response.getResponseArray(ClientConfigurationResponse.class)) {
				String key = config.getKey();
				String value = config.getValue();

				if (KEY_RECIPIENT_NATIONAL.equals(key)) {
					prefs.setRecipientNational(value);
				} else if (KEY_RECIPIENT_INTERNATIONAL.equals(key)) {
					prefs.setRecipientInternational(value);
				} else if (KEY_SENDER.equals(key)) {
					prefs.setMessageSender(value);
				} else if (KEY_PIN_SMS_TEMPLATE.equals(key)) {
					prefs.setPinRegex(value);
				} else if (KEY_ATTACHMENT_BASE_URL.equals(key)) {
					prefs.setAttachmentBaseUrl(value);
				}
			}

			return true;
		}

		throw new ApiException(response, null);
	}

	public static boolean sendMessage(String recipient, String content, Integer attachmentId) throws ApiException {
		DeviceOutboxRequest req = new DeviceOutboxRequest();
		req.setRecipient(recipient);
		req.setContent(content);

		if (attachmentId != null) {
			req.setAttachmentId(attachmentId);
		}

		DeviceOutboxCall call = new DeviceOutboxCall(getToken(), req);
		ApiResponse<Response> response = call.post();

		if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return true;
		}

		throw new ApiException(response, null);
	}

	public static int uploadAttachment(String mimeType, byte[] data) throws ApiException {
		DeviceAttachmentRequest req = new DeviceAttachmentRequest();
		req.setMimeType(mimeType);
		req.setData(Base64.encodeToString(data, Base64.NO_WRAP));

		DeviceUploadAttachmentCall call = new DeviceUploadAttachmentCall(getToken(), req);
		ApiResponse<Response> response = call.post();

		if (response.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
			return Integer.parseInt(response.getReturnedId());
		}

		throw new ApiException(response, null);
	}

	public static DeviceAttachmentResponse getAttachment(int attachmentId) throws ApiException {
		DeviceGetAttachmentCall call = new DeviceGetAttachmentCall(getToken(), attachmentId);
		ApiResponse<DeviceAttachmentResponse> response = call.get();

		if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return response.getResponseObject(DeviceAttachmentResponse.class);
		}

		throw new ApiException(response, null);
	}
}
