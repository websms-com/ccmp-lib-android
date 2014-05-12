package net.ut11.ccmp.lib.net.api.request;

import net.ut11.ccmp.api.domain.DeviceAttachmentRequest;
import net.ut11.ccmp.api.domain.Response;

public class DeviceUploadAttachmentCall extends ApiCall<DeviceAttachmentRequest, Response> {

	private String deviceToken = null;

	public DeviceUploadAttachmentCall(String deviceToken, DeviceAttachmentRequest request) {
		this.deviceToken = deviceToken;
		setRequestData(request);
	}

	@Override
	public String getEndpoint() {
		return "device/" + deviceToken + "/attachment";
	}
}
