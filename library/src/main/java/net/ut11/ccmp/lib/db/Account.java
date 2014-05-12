package net.ut11.ccmp.lib.db;

import android.graphics.Bitmap;

public class Account {

    private long id;
    private long timeStamp;
    private String avatarUrl;
    private String displayName;
    private Bitmap avatar;
    private boolean isReplyable;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    public boolean isReplyable() {
        return isReplyable;
    }

    public void setReplyable(boolean isReplyable) {
        this.isReplyable = isReplyable;
    }
}
