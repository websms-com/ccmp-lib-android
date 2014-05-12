package net.ut11.ccmp.lib.net.api.request;

import net.ut11.ccmp.api.domain.DeviceAttachmentResponse;
import net.ut11.ccmp.api.domain.Request;

public class DeviceGetAttachmentCall extends ApiCall<Request, DeviceAttachmentResponse> {

	private String deviceToken = null;
	private int attachmentId;

	public DeviceGetAttachmentCall(String deviceToken, int attachmentId) {
		this.deviceToken = deviceToken;
		this.attachmentId = attachmentId;
	}

	@Override
	public String getEndpoint() {
		return "device/" + deviceToken + "/attachment/" + attachmentId + "?absolute_uri=true";
	}
}
