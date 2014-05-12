package net.ut11.ccmp.lib.net.api.request;

import net.ut11.ccmp.api.domain.ClientConfigurationResponse;
import net.ut11.ccmp.api.domain.Request;

public class DeviceClientConfigurationCall extends ApiCall<Request, ClientConfigurationResponse> {

	@Override
	public String getEndpoint() {
		return "device/client/configuration/all";
	}
}
