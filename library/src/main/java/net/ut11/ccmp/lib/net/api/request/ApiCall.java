package net.ut11.ccmp.lib.net.api.request;

import net.ut11.ccmp.api.domain.Request;
import net.ut11.ccmp.api.domain.Response;
import net.ut11.ccmp.lib.net.api.Api;
import net.ut11.ccmp.lib.net.api.response.ApiException;
import net.ut11.ccmp.lib.net.api.response.ApiResponse;
import net.ut11.ccmp.lib.util.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import de.pribluda.android.jsonmarshaller.JSONMarshaller;

public abstract class ApiCall<REQ extends Request, RES extends Response> {

	private String mimeType = "application/json";
	private String token;
	private Parameter parameter;
	private REQ requestData;
	private String stringData;

	public abstract String getEndpoint();

	private String getUrl() {
		String query = "";
		if (token != null) {
			query += "/" + token;
		}

		if (parameter != null) {
			query += "?" + URLEncodedUtils.format(parameter.getAll(), HTTP.UTF_8);
		}

		String url = Api.getUrlFor(this);
		if (query.length() > 0) {
			url += query;
		}

		return url;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public void setRequestData(REQ requestData) {
		this.requestData = requestData;
	}

	public void setRequestData(String data) {
		this.stringData = data;
	}

	public ApiResponse<RES> get() throws ApiException {
		return request(new HttpGet(getUrl()));
	}

	public ApiResponse<RES> post() throws ApiException {
		return request(new HttpPost(getUrl()));
	}

	public ApiResponse<RES> put() throws ApiException {
		return request(new HttpPut(getUrl()));
	}

	public ApiResponse<RES> delete() throws ApiException {
		return request(new HttpDelete(getUrl()));
	}

	private ApiResponse<RES> request(HttpUriRequest request) throws ApiException {
		request.setHeader("Accept", mimeType);
		if ((requestData != null || stringData != null) && request instanceof HttpEntityEnclosingRequestBase) {
			attachData((HttpEntityEnclosingRequestBase) request, stringData == null ? requestData : stringData);
		}

		return Api.request(request);
	}

	private static void attachData(HttpEntityEnclosingRequestBase request, Object data) {
		if (request != null) {
			try {
				String output;
				String type = "application/json";

				if (data instanceof String) {
					type = "text/plain";
					output = (String) data;
				} else if (!data.getClass().isArray()) {
					output = JSONMarshaller.marshall(data).toString();
				} else {
					output = JSONMarshaller.marshallArray(data).toString();
				}

				request.setHeader("Content-Type", type);
				if (Logger.DEBUG) Logger.debug("appending request data [" + type + "] (" + request.getURI() + "): " + output);

				HttpEntity entity = new StringEntity(output, HTTP.UTF_8);
				request.setEntity(entity);
			} catch (Exception e) {
				Logger.error("failed to append request data to endpoint " + request.getURI(), e);
			}
		}
	}
}
