package net.ut11.ccmp.lib.net.api.response;

import android.net.Uri;

import net.ut11.ccmp.api.domain.Response;
import net.ut11.ccmp.lib.util.Logger;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import de.pribluda.android.jsonmarshaller.JSONUnmarshaller;

public class ApiResponse<T extends Response> {

	private int responseCode;
	private Header[] headers;
	private byte[] response;
	private JSONObject jsonResponse;

	public ApiResponse(int responseCode, Header[] headers, byte[] response) {
		this.responseCode = responseCode;
		this.headers = headers;
		this.response = response;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseText() {
		if (response != null) {
			return new String(response);
		}

		return null;
	}

	public Header[] getHeaders() {
		return headers;
	}

	public byte[] getResponse() throws ApiException {
		if (responseCode == HttpURLConnection.HTTP_OK) {
			return response;
		}

		throw new ApiException(this, null);
	}

	public JSONObject getJsonResponse() throws ApiException {
		if (responseCode == HttpURLConnection.HTTP_OK) {
			if (jsonResponse == null && response != null && response.length > 0) {
				try {
					jsonResponse = new JSONObject(new String(response));
				} catch (JSONException e) {
					try {
						JSONArray array = new JSONArray(new String(response));
						jsonResponse = new JSONObject();
						jsonResponse.put("array", array);
					} catch (JSONException e1) {
						Logger.warn("invalid json data retrieved");
					}
				}
			}

			return jsonResponse;
		}

		throw new ApiException(this, null);
	}

	public T getResponseObject(Class<T> responseClass) throws ApiException {
		if (responseCode == HttpURLConnection.HTTP_OK) {
			if (responseClass != null) {
				try {
					JSONObject data = getJsonResponse();
					return data == null ? null : JSONUnmarshaller.unmarshall(data.toString(), responseClass);
				} catch (Exception e) {
					Logger.error("request couldn't be unmarshalled: ", e);
				}
			}
		}

		throw new ApiException(this, null);
	}

	public List<T> getResponseArray(Class<T> responseClass) throws ApiException {
		if (responseCode == HttpURLConnection.HTTP_OK) {
			if (responseClass != null) {
				try {
					JSONObject data = getJsonResponse();
					if (data != null) {
						String key = (String) data.keys().next();
						List<T> ret = new ArrayList<T>();

						JSONArray array = data.optJSONArray(key);
						if (array != null) {
							for (int i = 0; i < array.length(); ++ i) {
								JSONObject entry = array.getJSONObject(i);
								ret.add(JSONUnmarshaller.unmarshall(entry, responseClass));
							}
						} else {
							ret.add(JSONUnmarshaller.unmarshall(data.optJSONObject(key), responseClass));
						}

						return ret;
					} else {
						return null;
					}
				} catch (JSONException e) {
					Logger.error("request couldn't be parsed: ", e);
				} catch (Exception e) {
					Logger.error("request couldn't be unmarshalled: ", e);
				}
			}
		}
		throw new ApiException(this, null);
	}

	public String getHeader(String name) {
		if (headers != null && name != null) {
			for (Header header : headers) {
				if (name.equalsIgnoreCase(header.getName())) {
					return header.getValue();
				}
			}
		}

		return null;
	}

	public String getReturnedId() throws ApiException {
		if (responseCode == HttpURLConnection.HTTP_CREATED) {
			String location = getHeader("Location");
			if (Logger.DEBUG) Logger.debug("Location: " + location);

			try {
				return Uri.parse(location).getLastPathSegment();
			} catch (NullPointerException e) {
				throw new ApiException(this, e);
			}
		}

		throw new ApiException(this, null);
	}
}
