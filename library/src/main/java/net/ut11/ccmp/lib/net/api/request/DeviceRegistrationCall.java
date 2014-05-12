package net.ut11.ccmp.lib.net.api.request;

import net.ut11.ccmp.api.domain.DeviceRegistrationRequest;
import net.ut11.ccmp.api.domain.Response;

public class DeviceRegistrationCall extends ApiCall<DeviceRegistrationRequest, Response> {

	boolean sendSms;

	public DeviceRegistrationCall(DeviceRegistrationRequest request, boolean sendSms) {
		this.sendSms = sendSms;
		setRequestData(request);
	}

	@Override
	public String getEndpoint() {
		return "device/registration/register?send_sms=" + sendSms;
	}
}
