package com.andromedalabs.ytdl.ui.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.andromedalabs.ytdl.R;
import com.andromedalabs.ytdl.ui.fragments.BrowserFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	@InjectView(R.id.toolbar) Toolbar mToolbar;
	@InjectView(R.id.adView) AdView mAdView;
	@InjectView(R.id.tabs) TabLayout mTabLayout;
	@InjectView(R.id.pager) ViewPager mPager;

	private PagerAdapter mPagerAdapter;
	private int[] mTabTitles = {R.string.browser,R.string.downloads};

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.inject(this);

		setSupportActionBar(mToolbar);

		mAdView.loadAd(new AdRequest.Builder().build());

		mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
		mPagerAdapter.addFragment(new BrowserFragment(),mTabTitles[0]);
		mPagerAdapter.addFragment(new BrowserFragment(),mTabTitles[1]);

		mPager.setAdapter(mPagerAdapter);
		mTabLayout.setupWithViewPager(mPager);


	}

	public class PagerAdapter extends FragmentPagerAdapter {

		private List<Fragment> fragments = new ArrayList<>();
		private List<Integer> fragmentTitles = new ArrayList<>();

		public PagerAdapter(FragmentManager fm){
			super(fm);
		}

		public void addFragment(Fragment fragment,int title){
			fragments.add(fragment);
			fragmentTitles.add(title);
		}

		@Override public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override public int getCount() {
			return fragments.size();
		}

		@Override public CharSequence getPageTitle(int position) {
			return getString(fragmentTitles.get(position));
		}
	}

}
