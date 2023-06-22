package manolegeorge.watchlog.info;

public class FriendRequestInfo {

    private int id;
    private UserInfo userInfo;
    private long timestamp;

    public FriendRequestInfo(int mId, long mTimestamp, UserInfo mUserInfo) {
        this.id = mId;
        this.timestamp = mTimestamp;
        this.userInfo = mUserInfo;
    }

    public int getId() {
        return this.id;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public UserInfo getUserInfo() {
        return this.userInfo;
    }

}
