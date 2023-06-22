package manolegeorge.watchlog.info;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")

public class MovieInfo {

	private int id;
	private String title;

	private String releaseDate = "00-00-0000";

	private String poster = "undefined";

	private List<GenreInfo> genres = null;

	private double rating = 0;

	public MovieInfo(int id, String title) {
		this.id = id;
		this.title = title;
	}

	public int getId() {
		return this.id;
	}

	public String getTitle() {
		return this.title;
	}

	public void setReleaseDate(String mReleaseDate) {
		this.releaseDate = mReleaseDate;
	}

	public String getReleaseDate() {
		return this.releaseDate;
	}

	public void setPoster(String poster) {
		this.poster = poster;
	}

	public String getPoster() {
		return this.poster;
	}

	public void setGenres(List<GenreInfo> mGenres) {
		this.genres = mGenres;
	}

	public String getGenresText() {
		if(genres != null && this.genres.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < this.genres.size(); i++) {
				sb.append(this.genres.get(i).getName());
				if(i < (this.genres.size() - 1)) {
					sb.append(", ");
				}
			}
			return sb.toString();
		}
		return "N/A";
	}

	public void setRating(double mRating) {
		this.rating = mRating;
	}

	public double getRating() {
		return this.rating;
	}

}
