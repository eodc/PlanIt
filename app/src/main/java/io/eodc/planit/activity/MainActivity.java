package io.eodc.planit.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.BuildConfig;
import io.eodc.planit.R;
import io.eodc.planit.fragment.BaseFragment;
import io.eodc.planit.fragment.CalendarFragment;
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
    @BindView(R.id.toolbar)             Toolbar                 mToolbar;

    private FragmentManager         mFragmentManager;

    private int     mShownAssignmentFlag;

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
        }  else if (initScreen.equals(getString(R.string.pref_init_page_planner_value))) mBottomNav.setCurrentItem(1);
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
                    mShownAssignmentFlag = BaseFragment.FLAG_SHOW_COMPLETE;
                } else {
                    item.setChecked(false);
                    mShownAssignmentFlag = BaseFragment.FLAG_SHOW_INCOMPLETE;
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
        mBottomNav.setBehaviorTranslationEnabled(false);
        bottomNavAdapter.setupWithBottomNavigation(mBottomNav);
        mBottomNav.setOnTabSelectedListener((position, wasSelected) -> {
            if (!wasSelected) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                switch (position) {
                    case 0:
                        transaction.replace(R.id.content_fragment, new HomeFragment()).commit();
                        break;
                    case 1:
                        Fragment fragment = PlannerFragment.newInstance(mShownAssignmentFlag);
                        transaction.replace(R.id.content_fragment, fragment).commit();
                        break;
                    case 2:
                        transaction.replace(R.id.content_fragment, new CalendarFragment()).commit();
                        break;
                }
            }
            return true;
        });

    }
}
