package net.ut11.ccmp.lib.db;

public class Attachment {

    private String uri;
    private String mimeType;
    private String name;
    private long id;
    private long size;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
