package net.ut11.ccmp.lib;

import net.ut11.ccmp.api.domain.Response;
import net.ut11.ccmp.lib.net.api.client.CcmpClient;
import net.ut11.ccmp.lib.net.api.request.ApiCall;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.net.api.response.ApiResponse;
import net.ut11.ccmp.lib.util.Logger;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.LinkedList;

public class TestApiClient extends CcmpClient {

	private LinkedList<TestResponse> responses = new LinkedList<TestResponse>();

	@Override
	public String getUrlFor(ApiCall call) {
		if (responses.isEmpty()) {
			return super.getUrlFor(call);
		}

		return "http://localhost/" + call.getEndpoint();
	}

	@Override
	public <T extends Response> ApiResponse<T> request(HttpUriRequest request) throws ApiException {
		if (responses.isEmpty()) {
			return super.request(request);
		}

		boolean json = false;
		Header accept = request.getFirstHeader("Accept");
		if (accept != null && "application/json".equalsIgnoreCase(accept.getValue())) {
			json = true;
		}

		if (Logger.DEBUG) Logger.debug("executing " + request.getMethod() + " on " + request.getURI());
		TestResponse resp = responses.pop();
		if (Logger.DEBUG) Logger.debug("response code: " + resp.statusCode);

		if (json) {
			if (Logger.DEBUG && resp.response != null) Logger.debug("response text: " + new String(resp.response));
		} else {
			if (Logger.DEBUG) Logger.debug("binary response");
		}

		return new ApiResponse<T>(resp.statusCode, resp.headers, resp.response);
	}

	public void addResponse(int statusCode, Header[] headers, String response) {
		responses.add(new TestResponse(statusCode, headers, response));
	}

	private class TestResponse {
		private int statusCode = 0;
		private Header[] headers = null;
		private byte[] response = null;

		public TestResponse(int statusCode, Header[] headers, String response) {
			this.statusCode = statusCode;
			this.response = response == null ? null : response.getBytes();
			this.headers = headers;
		}
	}
}
