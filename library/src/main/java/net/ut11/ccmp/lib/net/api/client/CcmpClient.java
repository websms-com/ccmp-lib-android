package net.ut11.ccmp.lib.net.api.client;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import net.ut11.ccmp.api.domain.Response;
import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.R;
import net.ut11.ccmp.lib.net.api.request.ApiCall;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.net.api.response.ApiResponse;
import net.ut11.ccmp.lib.util.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class CcmpClient extends ApiClient {

	private static final int CONNECTION_TIMEOUT = 15000;
	private static final int REQUEST_TIMEOUT = 60000;

	private String apiUrl = null;
	private String uaBranding = null;
	private String versionName = null;

	public CcmpClient() {
		Context context = LibApp.getContext();

		Resources res = context.getResources();
		uaBranding = res.getString(R.string.appName_ua);
		apiUrl = res.getString(R.string.apiUrl);

		if (!apiUrl.endsWith("/")) {
			apiUrl += "/";
		}

		try {
            if (context != null && context.getPackageManager() != null) {
                versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            }
		} catch (Exception e) {
			versionName = "unknown";
		}
	}

	public String getUrlFor(ApiCall call) {
		return apiUrl + call.getEndpoint();
	}

	public <T extends Response> ApiResponse<T> request(HttpUriRequest request) throws ApiException {
		return execRequest(request);
	}

	private <T extends Response> ApiResponse<T> execRequest(HttpUriRequest request) throws ApiException {
		if (request == null) {
			if (Logger.DEBUG) Logger.debug("request is null");
			return null;
		}

		HttpResponse response;
		Header[] headers = null;
		byte[] responseData = null;
		int statusCode = -1;

		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpClient.getParams(), REQUEST_TIMEOUT);

			boolean json = false;
			Header accept = request.getFirstHeader("Accept");
			if (accept != null && "application/json".equalsIgnoreCase(accept.getValue())) {
				json = true;
			}

			request.setHeader("Accept-Encoding", "deflate,gzip");
			request.setHeader("User-Agent", getUserAgent());
			request.setHeader("X-Api-Key", getApiKey());

			if (Logger.DEBUG) Logger.debug("executing " + request.getMethod() + " on " + request.getURI());
			long start = System.currentTimeMillis();
			response = httpClient.execute(request);
			long time = System.currentTimeMillis() - start;

			if (response != null) {
				statusCode = response.getStatusLine().getStatusCode();
				headers = response.getAllHeaders();
				if (Logger.DEBUG) Logger.debug("response code: " + statusCode + ", took: " + time + "ms");

				responseData = getResponseData(response);
				if (json && responseData != null) {
					if (Logger.DEBUG) {
                        Logger.debug("response text: " + new String(responseData));
                    }
				} else if (Logger.DEBUG) {
                    Logger.debug("binary response");
				}
			}
		} catch (IOException e) {
			throw new ApiException(statusCode, e);
		}

		return new ApiResponse<T>(statusCode, headers, responseData);
	}

	private static byte[] getResponseData(HttpResponse response) {
		if (response != null) {
			InputStream is = null;

			try {
				HttpEntity entity = response.getEntity();
				if (entity == null) {
					return null;
				}
				Header enc = entity.getContentEncoding();
				is = entity.getContent();

				if (enc != null) {
					String contentEncoding = enc.getValue();
					if (Logger.DEBUG) Logger.debug("response encoding: " + contentEncoding);
					if ("deflate".equalsIgnoreCase(contentEncoding)) {
						is = new InflaterInputStream(is);
					} else if ("gzip".equalsIgnoreCase(contentEncoding)) {
						is = new GZIPInputStream(is);
					}
				}
			} catch (Exception e) {
				Logger.error("failed to read from stream: ", e);
			}

			if (is != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int len;

				try {
					while ((len = is.read(buf)) > 0) {
						baos.write(buf, 0, len);
					}
				} catch (IOException e) {
					Logger.error("read response failed: ", e);
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						Logger.error("input stream couldn't be closed: ", e);
					}
				}

				return baos.toByteArray();
			}
		}

		return null;
	}

	private static String getApiKey() {
		String apiKey = LibApp.getContext().getString(R.string.apiKey);
		if (TextUtils.isEmpty(apiKey)) {
			throw new IllegalArgumentException("apiKey not set");
		}

		return apiKey;
	}

	private String getUserAgent() {
		String userAgent = "ccmp-" + uaBranding + "/" + versionName + ", Android ";

		String version = Build.VERSION.RELEASE;
		if (version.length() == 0) {
			version = "1.0";
		}
		userAgent += version;

		String model = Build.MODEL;
		String id = Build.ID;
		if (model.length() > 0) {
			userAgent += ", "+ model;

			if (id.length() > 0) {
				userAgent += " Build/" + id;
			}
		}

		Context context = LibApp.getContext();
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		userAgent += ", " + tm.getNetworkOperatorName();

		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			String typeName = info.getTypeName();
			String subTypeName = info.getSubtypeName();

			if (typeName != null && typeName.length() > 0) {
				userAgent += ", " + typeName;

				if (info.getType() == ConnectivityManager.TYPE_MOBILE && subTypeName != null && subTypeName.length() > 0) {
					userAgent += "/" + subTypeName;
				}
			}
		}

		return userAgent;
	}
}
