package manolegeorge.watchlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

@SuppressWarnings({"ConstantConditions", "deprecation"})

public class TVShowsActivity extends AppCompatActivity {

	SharedPreferences userSP;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);

		setContentView(R.layout.activity_movies);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(getResources().getString(R.string.tv_shows));

		final TabLayout tabLayout = findViewById(R.id.tab_layout);
		final ViewPager viewPager = findViewById(R.id.view_pager);

		tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tv_shows_tab_1)));
		tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tv_shows_tab_2)));
		tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tv_shows_tab_3)));
		tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);

		viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {}

		});

		viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
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
			switch(position) {
				case 0:
					try {
						return TVShowsActivityFragmentLatest.class.newInstance();
					} catch(Exception ignored) {}
				case 1:
					try {
						return TVShowsActivityFragmentTopRated.class.newInstance();
					} catch(Exception ignored) {}
				case 2:
					try {
						return TVShowsActivityFragmentGenres.class.newInstance();
					} catch(Exception ignored) {}
			}
			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}

	}

}
