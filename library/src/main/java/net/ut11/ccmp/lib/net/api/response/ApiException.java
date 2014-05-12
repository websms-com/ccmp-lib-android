package net.ut11.ccmp.lib.net.api.response;

import net.ut11.ccmp.api.domain.Response;

public class ApiException extends Exception {

	private int responseCode;
	private String responseText;

	public <T extends Response> ApiException(ApiResponse<T> response, Throwable cause) {
		super(response.getResponseCode() + " - " + response.getResponseText(), cause);
		this.responseCode = response.getResponseCode();
		this.responseText = response.getResponseText();
	}

	public ApiException(int responseCode, Throwable cause) {
		super(String.valueOf(responseCode), cause);
		this.responseCode = responseCode;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseText() {
		return responseText;
	}
}
