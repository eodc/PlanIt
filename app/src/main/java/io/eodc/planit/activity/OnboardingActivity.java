package io.eodc.planit.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eodc.planit.R;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.fragment.OnboardingAddClassesFragment;
import io.eodc.planit.fragment.OnboardingFragment;
import io.eodc.planit.listener.OnClassListChangeListener;

/**
 * Activity that is shown the first time the app is opened. Due to this, it is the default activity
 * but switches over to {@link MainActivity} if the first time flag has been tripped.
 *
 * @author 2n
 */
public class OnboardingActivity extends AppCompatActivity implements
        ViewPager.OnPageChangeListener,
        OnClassListChangeListener {

    @BindView(R.id.viewPager)   ViewPager   viewPager;
    @BindView(R.id.tabLayout)   TabLayout   tabLayout;
    @BindView(R.id.btn_back)    Button      btnBack;
    @BindView(R.id.btn_next)    Button      btnNext;

    /**
     * Shows the next slide in the carousel
     */
    @OnClick(R.id.btn_next) void nextSlide() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    /**
     * Goes back a slide in the carousel
     */
    @OnClick(R.id.btn_back) void backSlide() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

    private List<OnboardingFragment> fragments;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean(getString(R.string.pref_first_time_key), true)) {
            setContentView(R.layout.activity_onboarding);
            ButterKnife.bind(this);

            fragments = new ArrayList<>();
            fragments.add(OnboardingFragment.newInstance(getString(R.string.app_name),
                    R.drawable.ic_book_blue_250dp,
                    getString(R.string.tagline)));
            fragments.add(OnboardingFragment.newInstance("Your Planner, Your Way",
                    R.drawable.ic_format_list_bulleted_blue_250dp,
                    "See your assignments in an overview, list, or calendar. It's your choice."));
            fragments.add(OnboardingAddClassesFragment.newInstance(this));
            viewPager.setAdapter(new OnboardingPagerAdapter(getSupportFragmentManager(), fragments));
            tabLayout.setupWithViewPager(viewPager);
            viewPager.addOnPageChangeListener(this);

            preferences.edit()
                    .putString(getString(R.string.pref_show_notif_time_key), "19:00")
                    .apply();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    @Override
    public void onPageSelected(final int position) {
        if (position == 0) {
            btnBack.setVisibility(View.GONE);
        } else if (position == fragments.size() - 1) {
            btnNext.setText(R.string.btn_finish_label);
            Cursor c = getContentResolver().query(PlannerContract.ClassColumns.CONTENT_URI,
                    null, null, null, null);
            if (c != null && c.getCount() == 0)
                btnNext.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            if (c != null) c.close();
        } else {
            btnBack.setVisibility(View.VISIBLE);
            btnNext.setText(getString(R.string.btn_next_label));
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextSlide();
                }
            });
            btnNext.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) { }

    @Override
    public void onClassListChange(int count) {
        if (count == 0) {
            btnNext.setOnClickListener(null);
            btnNext.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        } else {
            btnNext.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(OnboardingActivity.this);
                    preferences.edit().putBoolean(getString(R.string.pref_first_time_key), false).apply();
                    Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Adapter for interfacing {@link OnboardingFragment} with the {@link ViewPager}.
     *
     * @author 2n
     */
    private class OnboardingPagerAdapter extends FragmentStatePagerAdapter {
        private List<OnboardingFragment> fragments;

        /**
         * Creates a new instance of OnboardingPagerAdapter
         *
         * @param fm Instance of {@link FragmentManager}
         * @param fragments List of {@link OnboardingFragment} to insert into the adapter
         */
        OnboardingPagerAdapter(FragmentManager fm, List<OnboardingFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
