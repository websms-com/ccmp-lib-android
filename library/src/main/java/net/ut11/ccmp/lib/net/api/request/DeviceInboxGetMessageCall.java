package net.ut11.ccmp.lib.net.api.request;

import net.ut11.ccmp.api.domain.DeviceInboxResponse;
import net.ut11.ccmp.api.domain.Request;

/**
 * Created by johannes on 04.04.18.
 */

public class DeviceInboxGetMessageCall extends ApiCall<Request, DeviceInboxResponse> {

    private String deviceToken = null;
    private long messageId = 0;

    public DeviceInboxGetMessageCall(String deviceToken, long messageId) {
        this.deviceToken = deviceToken;
        this.messageId = messageId;
    }


    @Override
    public String getEndpoint() {
        return "device/" + deviceToken + "/inbox/" + messageId;
    }

}
