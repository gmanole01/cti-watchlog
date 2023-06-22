package manolegeorge.watchlog.db;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import org.jetbrains.annotations.NotNull;

@Database(entities = {Movie.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
	private static AppDatabase instance = null;
	
	public static AppDatabase getInstance(@NotNull Context context) {
		if (instance == null) {
			instance = Room.databaseBuilder(context, AppDatabase.class, "local-db").allowMainThreadQueries().build();
		}
		
		return instance;
	}
	
	public abstract MovieDao movieDao();
}
