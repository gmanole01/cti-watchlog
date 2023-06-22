package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class MoviesActivity extends AppCompatActivity {
	
	SharedPreferences userSP = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);
		
		setContentView(R.layout.activity_movies);
		
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		
		setTitle(getResources().getString(R.string.movies));
		
		final TabLayout tabLayout = findViewById(R.id.tab_layout);
		final ViewPager viewPager = findViewById(R.id.view_pager);
		
		tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.movies_tab_1)));
		tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.movies_tab_2)));
		tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.movies_tab_3)));
		tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
		
		viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
		
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				viewPager.setCurrentItem(tab.getPosition());
			}
			
			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			}
			
			@Override
			public void onTabReselected(TabLayout.Tab tab) {
			}
			
		});
		
		viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_movies, menu);
		
		return true;
		
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.search:
				startActivity(new Intent(MoviesActivity.this, SearchMoviesActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private static class PagerAdapter extends FragmentPagerAdapter {
		
		PagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					try {
						return MoviesActivityFragmentLatest.class.newInstance();
					} catch (Exception ignored) {
					}
				case 1:
					try {
						return MoviesActivityFragmentTopRated.class.newInstance();
					} catch (Exception ignored) {
					}
				case 2:
					try {
						return MoviesActivityFragmentGenres.class.newInstance();
					} catch (Exception ignored) {
					}
			}
			return null;
		}
		
		@Override
		public int getCount() {
			return 3;
		}
		
	}
	
}
