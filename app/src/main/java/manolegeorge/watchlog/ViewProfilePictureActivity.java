package manolegeorge.watchlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.jsibbold.zoomage.ZoomageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

public class ViewProfilePictureActivity extends AppCompatActivity {
	
	ImageLoader imageLoader;
	SharedPreferences userSP;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		imageLoader = WatchLog.ImageLoader(this);
		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);
		
		setContentView(R.layout.activity_view_profile_picture);
		
		ZoomageView profilePicture = findViewById(R.id.profile_picture);
		
		ImageAware profilePictureIA = new ImageViewAware(profilePicture, false);
		imageLoader.displayImage(WatchLog.Utils.getProfilePicture(userSP), profilePictureIA, WatchLog.getImageLoaderOptions());
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
