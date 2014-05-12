package net.ut11.ccmp.lib.net.api.client;

import net.ut11.ccmp.api.domain.Response;
import net.ut11.ccmp.lib.net.api.request.ApiCall;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.net.api.response.ApiResponse;

import org.apache.http.client.methods.HttpUriRequest;

public abstract class ApiClient {

	public abstract String getUrlFor(ApiCall call);
	public abstract <T extends Response> ApiResponse<T> request(HttpUriRequest request) throws ApiException;
}
