package manolegeorge.watchlog.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import manolegeorge.watchlog.R;
import manolegeorge.watchlog.info.MovieInfo;

public class MoviesListAdapter extends BaseAdapter {
	
	private final LayoutInflater inflater;
	private final List<MovieInfo> movies;
	private final ImageLoader imageLoader;
	private final DisplayImageOptions options;
	
	public MoviesListAdapter(LayoutInflater inflater, List<MovieInfo> movies, ImageLoader imageLoader) {
		this.inflater = inflater;
		this.movies = movies;
		this.imageLoader = imageLoader;
		this.options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.no_poster)
				.showImageForEmptyUri(R.drawable.no_poster)
				.showImageOnFail(R.drawable.no_poster)
				.cacheInMemory(false)
				.cacheOnDisk(true)
				.build();
	}
	
	@Override
	public int getCount() {
		return movies.size();
	}
	
	@Override
	public MovieInfo getItem(int position) {
		return movies.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_view_movies, parent, false);
			holder = new ViewHolder();
			holder.poster = convertView.findViewById(R.id.poster);
			holder.title = convertView.findViewById(R.id.title);
			holder.genres = convertView.findViewById(R.id.genres);
			holder.releaseDate = convertView.findViewById(R.id.release_date);
			holder.rating = convertView.findViewById(R.id.rating);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		imageLoader.displayImage(movies.get(position).getPoster(), holder.poster, this.options);
		holder.title.setText(movies.get(position).getTitle());
		holder.genres.setText(movies.get(position).getGenresText());
		holder.releaseDate.setText(movies.get(position).getReleaseDate());
		holder.rating.setText(String.valueOf(movies.get(position).getRating()));
		return convertView;
	}
	
	public static class ViewHolder {
		public ImageView poster;
		public TextView title;
		public TextView genres;
		public TextView releaseDate;
		public TextView rating;
	}
	
}
