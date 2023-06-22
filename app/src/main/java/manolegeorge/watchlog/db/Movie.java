package manolegeorge.watchlog.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Movie {
	@PrimaryKey
	public int id;
	
	public int api_id;
	public String title;
	public String overview;
	public int runtime;
	public int budget;
	public int revenue;
	public double rating;
	public String backdrop;
	public String poster;
	
	public String release_date;
	public boolean watched;
	public boolean favourite;
}
