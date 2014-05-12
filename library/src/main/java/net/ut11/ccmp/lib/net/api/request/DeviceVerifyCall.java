package net.ut11.ccmp.lib.net.api.request;

import net.ut11.ccmp.api.domain.DeviceRegistrationRequest;
import net.ut11.ccmp.api.domain.Response;

public class DeviceVerifyCall extends ApiCall<DeviceRegistrationRequest, Response> {

	private String deviceToken = null;

	public DeviceVerifyCall(String deviceToken, String pin) {
		this.deviceToken = deviceToken;
		setRequestData(pin);
	}

	@Override
	public String getEndpoint() {
		return "device/registration/" + deviceToken + "/verify_pin";
	}
}
