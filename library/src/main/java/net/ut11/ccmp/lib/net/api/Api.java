package net.ut11.ccmp.lib.net.api;

import net.ut11.ccmp.api.domain.Response;
import net.ut11.ccmp.lib.net.api.client.ApiClient;
import net.ut11.ccmp.lib.net.api.request.ApiCall;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.net.api.response.ApiResponse;

import org.apache.http.client.methods.HttpUriRequest;

public class Api {

	private static ApiClient client = null;

	public static void setApiClient(ApiClient client) {
		Api.client = client;
	}

	public static ApiClient getClient() {
		return client;
	}

	public static String getUrlFor(ApiCall call) {
		return client.getUrlFor(call);
	}

	public static <T extends Response> ApiResponse<T> request(HttpUriRequest request) throws ApiException {
		return client.request(request);
	}
}
