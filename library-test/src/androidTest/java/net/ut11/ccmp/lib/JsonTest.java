package net.ut11.ccmp.lib;

import junit.framework.TestCase;

import net.ut11.ccmp.api.domain.DeviceRequest;
import net.ut11.ccmp.api.domain.DeviceResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

import de.pribluda.android.jsonmarshaller.JSONMarshaller;
import de.pribluda.android.jsonmarshaller.JSONUnmarshaller;

public class JsonTest extends TestCase {

	private static String jsonString = "{\"enabled\":false,\"msisdn\":436761234567,\"apiKey\":\"123456abcdef#?-\\/\\\"\"}";
	private static String jsonString2 = "{\"enabled\":true,\"msisdn\":436760123456,\"apiKey\":\"012345abcdef#?-\\/\\\"\"}";
	private static String jsonArrayString = "[" + jsonString + "," + jsonString2 + "]";

	public void testMarshall() throws NoSuchMethodException, IllegalAccessException, JSONException, InvocationTargetException {
		DeviceRequest req = new DeviceRequest();
		req.setApiKey("123456abcdef#?-/\"");
		req.setMsisdn(436761234567L);
		req.setEnabled(false);

		JSONObject obj = JSONMarshaller.marshall(req);
		assertEquals(obj.getString("apiKey"), "123456abcdef#?-/\"");
		assertEquals(obj.getLong("msisdn"), 436761234567L);
		assertFalse(obj.getBoolean("enabled"));

		assertEquals(obj.toString(), jsonString);
	}

	public void testUnmarshall() throws NoSuchMethodException, InstantiationException, IllegalAccessException, JSONException, InvocationTargetException {
		DeviceResponse resp = JSONUnmarshaller.unmarshall(jsonString, DeviceResponse.class);

		assertEquals(resp.getApiKey(), "123456abcdef#?-/\"");
        Long msisdn = 436761234567L;
		assertEquals(resp.getMsisdn(), msisdn);
		assertFalse(resp.getEnabled());
	}

	public void testMarshallArray() throws NoSuchMethodException, IllegalAccessException, JSONException, InvocationTargetException {
		DeviceRequest[] reqs = new DeviceRequest[2];
		reqs[0] = new DeviceRequest();
		reqs[0].setApiKey("123456abcdef#?-/\"");
		reqs[0].setMsisdn(436761234567L);
		reqs[0].setEnabled(false);

		reqs[1] = new DeviceRequest();
		reqs[1].setApiKey("012345abcdef#?-/\"");
		reqs[1].setMsisdn(436760123456L);
		reqs[1].setEnabled(true);

		JSONArray array = JSONMarshaller.marshallArray(reqs);
		JSONObject jo = array.getJSONObject(0);
		assertEquals(jo.getString("apiKey"), "123456abcdef#?-/\"");
		assertEquals(jo.getLong("msisdn"), 436761234567L);
		assertFalse(jo.getBoolean("enabled"));

		jo = array.getJSONObject(1);
		assertEquals(jo.getString("apiKey"), "012345abcdef#?-/\"");
		assertEquals(jo.getLong("msisdn"), 436760123456L);
		assertTrue(jo.getBoolean("enabled"));

		assertEquals(array.toString(), jsonArrayString);
	}
}
