package manolegeorge.watchlog.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MovieDao {
	@Query("SELECT * FROM movie")
	List<Movie> getAll();
	
	@Query("SELECT * FROM movie WHERE api_id = :api_id")
	Movie getByApiId(int api_id);
	
	@Query("SELECT * FROM movie WHERE watched = 1")
	List<Movie> getAllWatched();
	
	@Query("SELECT * FROM movie WHERE favourite = 1")
	List<Movie> getAllFavourite();
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Movie... movies);
}
