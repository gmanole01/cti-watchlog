package manolegeorge.watchlog.info;

@SuppressWarnings("ALL")

public class WatchedMovieInfo {

    private int id;
    private long timestamp;
    private MovieInfo movieInfo;

    public WatchedMovieInfo(int mId, long mTimestamp, MovieInfo mMovieInfo) {
        this.id = mId;
        this.timestamp = mTimestamp;
        this.movieInfo = mMovieInfo;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public MovieInfo getMovieInfo() {
        return this.movieInfo;
    }

}
