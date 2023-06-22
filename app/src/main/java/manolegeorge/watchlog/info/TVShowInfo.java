package manolegeorge.watchlog.info;

@SuppressWarnings("unused")

public class TVShowInfo {

	private final int id;
	private final String name;

	private String poster = "";

	private double rating = 0;

	private int episodesCount = 0;
	private int watchedEpisodesCount = 0;

	public TVShowInfo(int mId, String mTitle) {
		this.id = mId;
		this.name = mTitle;
	}

	public void setEpisodesCount(int mEpisodesCount) {
		this.episodesCount = mEpisodesCount;
	}

	public void setWatchedEpisodesCount(int mWatchedEpisodesCount) {
		this.watchedEpisodesCount = mWatchedEpisodesCount;
	}

	public int getEpisodesCount() {
		return this.episodesCount;
	}

	public int getWatchedEpisodesCount() {
		return this.watchedEpisodesCount;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setPoster(String mPoster) {
		this.poster = mPoster;
	}

	public String getPoster() {
		return this.poster;
	}

	public void setRating(double mRating) {
		this.rating = mRating;
	}

	public double getRating() {
		return this.rating;
	}

}
