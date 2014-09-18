package net.ut11.ccmp.lib.db;

public class Message {

	private String address;
    private String message;
    private String pushParameter;
    private long id;
    private long messageId;
    private long dateSent;
    private long accountId;
    private long responseForId;
    private long attachmentId;
    private boolean incoming;
    private boolean read;
    private boolean isSms;
    private boolean expired = false;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getDateSent() {
		return dateSent;
	}

	public void setDateSent(long dateSent) {
		this.dateSent = dateSent;
	}

	public boolean isIncoming() {
		return incoming;
	}

	public void setIncoming(boolean incoming) {
		this.incoming = incoming;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isSms() {
		return isSms;
	}

	public void setIsSms(boolean isSms) {
		this.isSms = isSms;
	}

	public long getResponseForId() {
		return responseForId;
	}

	public void setResponseForId(long responseForId) {
		this.responseForId = responseForId;
	}

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getPushParameter() {
        return pushParameter;
    }

    public void setPushParameter(String pushParameter) {
        this.pushParameter = pushParameter;
    }

    public long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}
