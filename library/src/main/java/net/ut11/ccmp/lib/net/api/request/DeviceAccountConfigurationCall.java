package net.ut11.ccmp.lib.net.api.request;

import net.ut11.ccmp.api.domain.DeviceConfigurationRequest;
import net.ut11.ccmp.api.domain.DeviceConfigurationResponse;

public class DeviceAccountConfigurationCall extends ApiCall<DeviceConfigurationRequest, DeviceConfigurationResponse> {

	private long accountId = 0;

    public static final String KEY_SENDER_DISPLAY_NAME = "SENDER_DISPLAY_NAME";
    public static final String KEY_SENDER_DISPLAY_IMAGE = "SENDER_DISPLAY_IMAGE";
    public static final String KEY_DEFAULT_REPLYABLE = "DEFAULT_REPLYABLE";

    public DeviceAccountConfigurationCall(long accountId) {
        this.accountId = accountId;
    }

    @Override
	public String getEndpoint() {
		return "device/account/" + accountId + "/configuration/all";
	}
}
