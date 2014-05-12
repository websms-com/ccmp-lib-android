package net.ut11.ccmp.lib.net.api.request;

import net.ut11.ccmp.api.domain.DeviceRequest;
import net.ut11.ccmp.api.domain.DeviceResponse;

public class DeviceCall extends ApiCall<DeviceRequest, DeviceResponse> {

	public DeviceCall() {
	}

	public DeviceCall(DeviceRequest request) {
		setRequestData(request);
	}

	@Override
	public String getEndpoint() {
		return "device";
	}
}
