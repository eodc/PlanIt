package io.eodc.planit.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
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
import io.eodc.planit.fragment.OnboardingAddClassesFragment;
import io.eodc.planit.fragment.OnboardingFragment;
import io.eodc.planit.model.ClassListViewModel;

/**
 * Activity that is shown the first time the app is opened. Due to this, it is the default activity
 * but switches over to {@link MainActivity} if the first time flag has been tripped.
 *
 * @author 2n
 */
public class OnboardingActivity extends AppCompatActivity implements
        ViewPager.OnPageChangeListener {

    @BindView(R.id.pager)   ViewPager   mViewPager;
    @BindView(R.id.layout_tab)   TabLayout   mTabLayout;
    @BindView(R.id.btn_back)    Button      mBtnBack;
    @BindView(R.id.btn_next)    Button      mBtnNext;

    private List<OnboardingFragment> mOnboardingFragments;

    /**
     * Shows the next slide in the carousel
     */
    @OnClick(R.id.btn_next)
    void nextSlide() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
    }

    /**
     * Goes back a slide in the carousel
     */
    @OnClick(R.id.btn_back)
    void backSlide() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean(getString(R.string.pref_first_time_key), true)) {
            setContentView(R.layout.activity_onboarding);
            ButterKnife.bind(this);

            mOnboardingFragments = new ArrayList<>();
            mOnboardingFragments.add(OnboardingFragment.newInstance(getString(R.string.app_name),
                    R.drawable.ic_logo_24dp,
                    getString(R.string.tagline)));
            mOnboardingFragments.add(OnboardingFragment.newInstance("Your Planner, Your Way",
                    R.drawable.ic_format_list_bulleted_blue_250dp,
                    "See your assignments in an overview, list, or calendar. It's your choice."));
            mOnboardingFragments.add(new OnboardingAddClassesFragment());
            mViewPager.setAdapter(new OnboardingPagerAdapter(getSupportFragmentManager(), mOnboardingFragments));
            mTabLayout.setupWithViewPager(mViewPager);
            mViewPager.addOnPageChangeListener(this);

            preferences.edit()
                    .putString(getString(R.string.pref_show_notif_time_key), "19:00")
                    .apply();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(final int position) {
        if (position == 0) mBtnBack.setVisibility(View.GONE);
        else if (position == mTabLayout.getTabCount() - 1) {
            mBtnNext.setText(R.string.btn_finish_label);
            ViewModelProviders.of(this).get(ClassListViewModel.class)
                    .getClasses().observe(this, classes -> {
                        if (classes != null) {
                            if (mTabLayout.getSelectedTabPosition() == mTabLayout.getTabCount() - 1) {
                                if (classes.size() == 0) {
                                    mBtnNext.setOnClickListener(null);
                                    mBtnNext.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                                } else {
                                    mBtnNext.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                                    mBtnNext.setOnClickListener(v -> {
                                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                                        preferences.edit().putBoolean(getString(R.string.pref_first_time_key), false).apply();
                                        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    });
                                }
                            }
                        }
                    });

        } else {
            mBtnBack.setVisibility(View.VISIBLE);
            mBtnNext.setText(getString(R.string.btn_next_label));
            mBtnNext.setOnClickListener(v -> nextSlide());
            mBtnNext.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
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
         * @param fm        Instance of {@link FragmentManager}
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
