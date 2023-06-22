package manolegeorge.watchlog.info;


public class WatchedMovieInfo {

    private final long timestamp;
    private final MovieInfo movieInfo;

    public WatchedMovieInfo(long mTimestamp, MovieInfo mMovieInfo) {
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
