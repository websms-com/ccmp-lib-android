package net.ut11.ccmp.lib.net.api.request;

import net.ut11.ccmp.api.domain.DeviceOutboxRequest;
import net.ut11.ccmp.api.domain.Response;

public class DeviceOutboxCall extends ApiCall<DeviceOutboxRequest, Response> {

	private String deviceToken = null;

	public DeviceOutboxCall(String deviceToken, DeviceOutboxRequest request) {
		this.deviceToken = deviceToken;
		setRequestData(request);
	}

	@Override
	public String getEndpoint() {
		return "device/" + deviceToken + "/outbox";
	}
}
