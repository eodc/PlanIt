package io.eodc.planit.activity;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eodc.planit.BuildConfig;
import io.eodc.planit.R;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.fragment.AddAssignmentFragment;
import io.eodc.planit.fragment.CalendarFragment;
import io.eodc.planit.fragment.DatePickerFragment;
import io.eodc.planit.fragment.HomeFragment;
import io.eodc.planit.fragment.PlannerFragment;
import io.eodc.planit.listener.AssignmentTypeLoadChangeListener;
import timber.log.Timber;

/**
 * Activity that the user mostly interacts with
 *
 * @author 2n
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.bottom_navigation)   AHBottomNavigation      mBottomNav;
    @BindView(R.id.create_fab)          FloatingActionButton    mFab;
    @BindView(R.id.overlay)             ImageView               mOverlay;
    @BindView(R.id.toolbar)             Toolbar                 mToolbar;

    private boolean mBottomSheetShown       = false;
    private int     mShownAssignmentFlag    = PlannerContract.FLAG_SHOW_INCOMPLETE;

    private FragmentManager         mFragmentManager;
    private AddAssignmentFragment   mBottomSheet;

    @OnClick(R.id.create_fab)
    void handleBottomSheet() {
        if (!mBottomSheetShown) showBottomSheet();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFragmentManager = getSupportFragmentManager();

        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

        setSupportActionBar(mToolbar);
        mToolbar.inflateMenu(R.menu.main_menu);

        setupBottomNavigation();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String initScreen = sharedPreferences.getString(getString(R.string.pref_init_page_key), getString(R.string.pref_init_page_home_value));

        if (initScreen.equals(getString(R.string.pref_init_page_home_value))) {
            mFragmentManager.beginTransaction().add(R.id.content_fragment, new HomeFragment()).commit();
        } else if (initScreen.equals(getString(R.string.pref_init_page_planner_value))) mBottomNav.setCurrentItem(1);
        else if (initScreen.equals(getString(R.string.pref_init_page_calendar_value))) mBottomNav.setCurrentItem(2);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.mnu_show_completed:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    mShownAssignmentFlag = PlannerContract.FLAG_SHOW_COMPLETE;
                } else {
                    item.setChecked(false);
                    mShownAssignmentFlag = PlannerContract.FLAG_SHOW_INCOMPLETE;
                }
                Fragment currentFragment = mFragmentManager.findFragmentById(R.id.content_fragment);
                if (currentFragment instanceof AssignmentTypeLoadChangeListener) {
                    ((AssignmentTypeLoadChangeListener) currentFragment).onTypeChanged(mShownAssignmentFlag);
                }
                return true;
            case R.id.mnu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    private void setupBottomNavigation() {
        AHBottomNavigationAdapter bottomNavAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_menu);
        mBottomNav.setAccentColor(ContextCompat.getColor(this, R.color.colorAccent));
        bottomNavAdapter.setupWithBottomNavigation(mBottomNav);
        mBottomNav.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (!wasSelected) {
                    FragmentTransaction transaction = mFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    switch (position) {
                        case 0:
                            hideBottomSheet();
                            transaction.replace(R.id.content_fragment, new HomeFragment()).commit();
                            break;
                        case 1:
                            mFab.show();
                            Fragment fragment = PlannerFragment.newInstance(mShownAssignmentFlag);
                            transaction.replace(R.id.content_fragment, fragment).commit();
                            break;
                        case 2:
                            hideBottomSheet();
                            transaction.replace(R.id.content_fragment, new CalendarFragment()).commit();
                            break;
                    }
                } else {
                    if (position == 1) hideBottomSheet();
                }
                return true;
            }
        });

    }

    private void showBottomSheet() {
        mOverlay.setVisibility(View.VISIBLE);
        mOverlay.animate()
                .alpha(0.5f)
                .setDuration(225)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        PlannerFragment fragment = (PlannerFragment) mFragmentManager.findFragmentById(R.id.content_fragment);

                        mBottomSheet = AddAssignmentFragment.newInstance(fragment);

                        mFragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.slide_up, 0)
                                .add(R.id.bottom_sheet, mBottomSheet)
                                .commit();

                        BottomSheetBehavior sheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
                        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                            @Override
                            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                                    hideBottomSheet();
                                    mFab.show();
                                }
                            }

                            @Override
                            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                            }
                        });
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mOverlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hideBottomSheet();
                                mFab.show();
                            }
                        });

                        FrameLayout bottomSheetContainer = findViewById(R.id.bottom_sheet);
                        bottomSheetContainer.setOnClickListener(null);
                        mFab.hide();

                        mBottomSheetShown = true;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });

    }

    public void hideBottomSheet() {
        mOverlay.animate()
                .alpha(0.0f)
                .setDuration(195)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (mBottomSheet != null) {
                            mFragmentManager.beginTransaction()
                                    .remove(mBottomSheet)
                                    .commit();
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mOverlay.setVisibility(View.GONE);
                        mBottomSheetShown = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (mBottomSheetShown) {
            hideBottomSheet();
            mFab.show();
        } else super.onBackPressed();
    }

    public void showFab() {
        mFab.show();
    }

    /**
     * Gets the instance of the bottom navigation for use in fragments
     *
     * @return instance of mBottomNav
     */
    @NotNull
    public AHBottomNavigation getBottomNav() {
        return mBottomNav;
    }

    /**
     * Shows the date picker in the bottom sheet
     *
     * @param view view that called this method
     */
    public void showDatePicker(View view) {
        DialogFragment datePicker = DatePickerFragment.newInstance(mBottomSheet);
        datePicker.show(getSupportFragmentManager(), "datePicker");
    }
}
