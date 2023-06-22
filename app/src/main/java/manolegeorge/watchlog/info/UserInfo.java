package manolegeorge.watchlog.info;

@SuppressWarnings("ALL")

public class UserInfo {

	private int id;
	private String profilePicture = null;
	private String username = "undefined";

	public UserInfo(int mId) {
		this.id = mId;
	}

	public int getId() {
		return this.id;
	}

	public void setProfilePicture(String mProfilePicture) {
		this.profilePicture = mProfilePicture;
	}

	public void setUsername(String mUsername) {
		this.username = mUsername;
	}

	public String getProfilePicture() {
		return this.profilePicture;
	}

	public String getUsername() {
		return this.username;
	}

}
