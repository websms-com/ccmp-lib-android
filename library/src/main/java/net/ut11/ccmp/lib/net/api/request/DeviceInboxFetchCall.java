package net.ut11.ccmp.lib.net.api.request;

import net.ut11.ccmp.api.domain.DeviceInboxResponse;
import net.ut11.ccmp.api.domain.Request;

public class DeviceInboxFetchCall extends ApiCall<Request, DeviceInboxResponse> {

	private String deviceToken = null;
	private long messageId = 0;

	public DeviceInboxFetchCall(String deviceToken) {
		this(deviceToken, 0);
	}

	public DeviceInboxFetchCall(String deviceToken, long messageId) {
		this.deviceToken = deviceToken;
		this.messageId = messageId;
	}

	@Override
	public String getEndpoint() {
		String endpoint = "device/" + deviceToken + "/inbox";
		if (messageId > 0) {
			endpoint += "/" + messageId;
		}
		endpoint += "/fetch";

		return endpoint;
	}
}
