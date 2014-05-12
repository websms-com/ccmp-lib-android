package net.ut11.ccmp.lib.ccmpclient;

import android.test.AndroidTestCase;

import net.ut11.ccmp.api.domain.DeviceAttachmentResponse;
import net.ut11.ccmp.api.domain.DeviceResponse;
import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.net.api.endpoint.DeviceEndpoint;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.test.R;
import net.ut11.ccmp.lib.util.LibPreferences;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

public class DeviceApiTest extends AndroidTestCase {

	private LibPreferences prefs = null;

	private static long msisdn = 0;
	private static String deviceToken = null;
	private static String pushId = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		prefs = LibApp.getLibPreferences();
		prefs.setMsisdn(0);
		prefs.setDeviceToken(null);
		prefs.setDeviceVerified(false);
	}

	public void testDeviceNotExists() {
		prefs.setDeviceToken("47110815");
		int statusCode = -1;

		try {
			DeviceEndpoint.getDevice();
		} catch (ApiException e) {
			statusCode = e.getResponseCode();
		}

		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, statusCode);
	}

	public void testRegister() throws ApiException {
		ensureRegistered();
	}

	public void testUpdateDevice() throws ApiException {
		ensureRegistered();
		DeviceEndpoint.updateDevice();
	}

	public void testSendMessage() throws ApiException {
		ensureRegistered();
		DeviceEndpoint.sendMessage("436760000000", "Test0815", null);
	}

	public void testRegisterPinInvalid() throws ApiException {
		assertNull(prefs.getDeviceToken());
		assertFalse(prefs.isDeviceVerified());

		boolean result = DeviceEndpoint.registerDevice(436760000000L);
		assertTrue(result);
		assertFalse(prefs.isDeviceVerified());

		String token = prefs.getDeviceToken();
		assertNotNull(token);

		int statusCode = -1;
		try {
			DeviceEndpoint.verifyPin("4321");
		} catch (ApiException e) {
			statusCode = e.getResponseCode();
		}

		assertEquals(HttpURLConnection.HTTP_FORBIDDEN, statusCode);
		assertFalse(prefs.isDeviceVerified());
	}

	public void testGetConfiguration() throws ApiException {
		prefs.setRecipientNational(null);
		prefs.setRecipientInternational(null);
		prefs.setMessageSender(null);
		prefs.setPinRegex(null);

		DeviceEndpoint.updateClientConfiguration();
		assertNotNull(prefs.getRecipientNational());
		assertNotNull(prefs.getRecipientInternational());
		assertNotNull(prefs.getMessageSender());
		assertNotNull(prefs.getPinRegex());

		Pattern.compile(prefs.getPinRegex(), Pattern.CASE_INSENSITIVE);
	}

	public void testAttachment() throws ApiException, IOException {
		ensureRegistered();

		InputStream is =  LibApp.getContext().getResources().openRawResource(R.raw.ic_launcher);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int len;

		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}

		is.close();
		baos.close();

		final byte[] image = baos.toByteArray();

		int attachmentId = DeviceEndpoint.uploadAttachment("image/png", image);
		assertTrue(attachmentId > 0);

		DeviceAttachmentResponse resp = DeviceEndpoint.getAttachment(attachmentId);
		assertEquals("image/png", resp.getMimeType());

		String uri = resp.getUri();
		assertTrue(uri.length() > 4);

		baos = new ByteArrayOutputStream();
		is = new URL(uri).openStream();

		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}

		is.close();
		baos.close();

		assertTrue(Arrays.equals(image, baos.toByteArray()));
	}

	private void ensureRegistered() throws ApiException {
		if (msisdn > 0) {
			prefs.setMsisdn(msisdn);
			prefs.setDeviceToken(deviceToken);
			prefs.setDeviceVerified(true);
			prefs.setGcmRegistrationId(pushId);
		} else {
			registerDevice();

			msisdn = prefs.getMsisdn();
			deviceToken = prefs.getDeviceToken();
			pushId = prefs.getGcmRegistrationId();
		}
	}

	private void registerDevice() throws ApiException {
		assertNull(prefs.getDeviceToken());
		assertFalse(prefs.isDeviceVerified());
		assertEquals(prefs.getMsisdn(), 0);

		boolean result = DeviceEndpoint.registerDevice(436760000000L);
		assertTrue(result);
		assertFalse(prefs.isDeviceVerified());
		assertEquals(prefs.getMsisdn(), 436760000000L);

		String token = prefs.getDeviceToken();
		assertNotNull(token);

		result = DeviceEndpoint.verifyPin("1234");
		assertTrue(result);
		assertTrue(prefs.isDeviceVerified());

		DeviceResponse resp = DeviceEndpoint.getDevice();
		assertEquals(436760000000L, resp.getMsisdn());
		assertNotNull(resp.getPushId());
	}
}
