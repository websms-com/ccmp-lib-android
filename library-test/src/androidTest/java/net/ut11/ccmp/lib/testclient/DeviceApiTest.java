package net.ut11.ccmp.lib.testclient;

import android.test.AndroidTestCase;

import net.ut11.ccmp.api.domain.DeviceInboxResponse;
import net.ut11.ccmp.api.domain.DeviceResponse;
import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.LocationHeader;
import net.ut11.ccmp.lib.TestApiClient;
import net.ut11.ccmp.lib.net.api.Api;
import net.ut11.ccmp.lib.net.api.endpoint.DeviceEndpoint;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.util.LibPreferences;

import org.apache.http.Header;
import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.util.List;

import de.pribluda.android.jsonmarshaller.JSONMarshaller;

public class DeviceApiTest extends AndroidTestCase {

	private TestApiClient client = null;
	private LibPreferences prefs = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		client = (TestApiClient) Api.getClient();

		prefs = LibApp.getLibPreferences();
		prefs.setMsisdn(0);
		prefs.setDeviceToken(null);
		prefs.setDeviceVerified(false);
	}

	public void testMsisdnTooShort() throws ApiException {
		boolean failed = false;

		try {
			DeviceEndpoint.registerDevice(99999);
		} catch (IllegalArgumentException e) {
			failed = true;
		}

		assertTrue(failed);
	}

	public void testPinTooShort() throws ApiException {
		boolean failed = false;
		assertFalse(prefs.isDeviceVerified());

		try {
			DeviceEndpoint.verifyPin("123");
		} catch (IllegalArgumentException e) {
			failed = true;
		}

		assertTrue(failed);
		assertFalse(prefs.isDeviceVerified());
	}

	public void testPinVerified() throws ApiException {
		prefs.setDeviceToken("47110815");
		assertFalse(prefs.isDeviceVerified());

		client.addResponse(HttpURLConnection.HTTP_OK, null, null);
		DeviceEndpoint.verifyPin("1234");

		assertTrue(prefs.isDeviceVerified());
	}

	public void testGetDeviceToken() throws ApiException {
		assertNull(prefs.getDeviceToken());

		client.addResponse(HttpURLConnection.HTTP_OK, null, "[]");
		client.addResponse(HttpURLConnection.HTTP_CREATED, new Header[]{ new LocationHeader("47110815") }, null);

		boolean success = DeviceEndpoint.registerDevice(436760000000L);

		assertTrue(success);
		assertEquals("47110815", prefs.getDeviceToken());
	}

	public void testIgnoreDeviceToken() throws ApiException {
		int statusCode = -1;

		client.addResponse(HttpURLConnection.HTTP_BAD_REQUEST, new Header[]{ new LocationHeader("47110815") }, null);
		try {
			DeviceEndpoint.registerDevice(436760000000L);
		} catch (ApiException e) {
			statusCode = e.getResponseCode();
		}

		assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, statusCode);
	}

	public void testGetDevice() throws ApiException, NoSuchMethodException, IllegalAccessException, JSONException, InvocationTargetException {
		DeviceResponse resp = new DeviceResponse();
		resp.setMsisdn(436760000000L);

		String json = JSONMarshaller.marshall(resp).toString();
		client.addResponse(HttpURLConnection.HTTP_OK, null, json);

		boolean failed = false;
		try {
			DeviceEndpoint.getDevice();
		} catch (IllegalArgumentException e) {
			failed = true;
		}
		assertTrue(failed);

		prefs.setDeviceToken("47110815");
		resp = DeviceEndpoint.getDevice();
        Long msisdn = 436760000000L;
		assertEquals(msisdn, resp.getMsisdn());
	}

	public void testGetMessage() throws ApiException, NoSuchMethodException, IllegalAccessException, JSONException, InvocationTargetException {
		prefs.setDeviceToken("47110815");

		DeviceInboxResponse resp = new DeviceInboxResponse();
		resp.setId(4711);
		resp.setContent("0815");

		client.addResponse(HttpURLConnection.HTTP_NOT_FOUND, null, null);
		int statusCode = -1;
		try {
			DeviceEndpoint.getMessage(4711);
		} catch (ApiException e) {
			statusCode = e.getResponseCode();
		}
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, statusCode);

		String json = JSONMarshaller.marshall(resp).toString();
		client.addResponse(HttpURLConnection.HTTP_OK, null, json);

		resp = DeviceEndpoint.getMessage(4711);
		assertEquals(4711, resp.getId());
		assertEquals("0815", resp.getContent());
	}

	public void testGetMessages() throws ApiException, NoSuchMethodException, IllegalAccessException, JSONException, InvocationTargetException {
		prefs.setDeviceToken("47110815");

		DeviceInboxResponse[] array  = new DeviceInboxResponse[2];
		array[0] = new DeviceInboxResponse();
		array[0].setId(4711);
		array[0].setContent("0815");

		array[1] = new DeviceInboxResponse();
		array[1].setId(4712);
		array[1].setContent("0816");

		client.addResponse(HttpURLConnection.HTTP_NOT_FOUND, null, null);
		int statusCode = -1;
		try {
			DeviceEndpoint.getMessages();
		} catch (ApiException e) {
			statusCode = e.getResponseCode();
		}
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, statusCode);

		String json = JSONMarshaller.marshallArray(array).toString();
		client.addResponse(HttpURLConnection.HTTP_OK, null, json);

		List<DeviceInboxResponse> respList = DeviceEndpoint.getMessages();

		assertEquals(4711, respList.get(0).getId());
		assertEquals("0815", respList.get(0).getContent());

		assertEquals(4712, respList.get(1).getId());
		assertEquals("0816", respList.get(1).getContent());
	}
}
