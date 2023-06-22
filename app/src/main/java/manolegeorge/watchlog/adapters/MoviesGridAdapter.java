package manolegeorge.watchlog.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import manolegeorge.watchlog.R;
import manolegeorge.watchlog.info.MovieInfo;

public class MoviesGridAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<MovieInfo> movies;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public MoviesGridAdapter(LayoutInflater inflater, List<MovieInfo> movies, ImageLoader imageLoader) {
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
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.grid_view_movies, parent, false);
            holder = new ViewHolder();
            holder.poster = convertView.findViewById(R.id.poster);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        imageLoader.displayImage(movies.get(position).getPoster(), holder.poster, this.options);
        return convertView;
    }

    public class ViewHolder {
        public ImageView poster;
    }

}
