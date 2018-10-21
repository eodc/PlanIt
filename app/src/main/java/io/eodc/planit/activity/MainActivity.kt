package io.eodc.planit.activity

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import io.eodc.planit.BuildConfig
import io.eodc.planit.R
import io.eodc.planit.db.Subject
import io.eodc.planit.fragment.AddAssignmentFragment
import io.eodc.planit.fragment.CalendarFragment
import io.eodc.planit.fragment.HomeFragment
import io.eodc.planit.fragment.PlannerFragment
import io.eodc.planit.model.SubjectListViewModel
import timber.log.Timber

/**
 * Activity that the user mostly interacts with
 *
 * @author 2n
 */
class MainActivity : AppCompatActivity() {

    @BindView(R.id.nav_bottom)
    internal var mBottomNav: AHBottomNavigation? = null

    @BindView(R.id.fab_create_assign)
    internal var mFab: FloatingActionButton? = null

    @BindView(R.id.tb)
    internal var mToolbar: Toolbar? = null

    private var mFragmentManager: FragmentManager? = null

    var classes: List<Subject>? = null
        private set

    @OnClick(R.id.fab_create_assign)
    internal fun handleCreateFab() {
        if (fragmentManager != null) {
            AddAssignmentFragment().show(supportFragmentManager, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        Thread {
            classes = ViewModelProviders.of(this@MainActivity)
                    .get<SubjectListViewModel>(SubjectListViewModel::class.java!!)
                    .subjects
        }.start()

        mFragmentManager = supportFragmentManager

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        setSupportActionBar(mToolbar)
        mToolbar!!.inflateMenu(R.menu.main_menu)

        setupBottomNavigation()
        mFab!!.hide()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val initScreen = sharedPreferences.getString(getString(R.string.pref_init_page_key), getString(R.string.pref_init_page_home_value))

        mBottomNav!!.currentItem = -1 // Set nav to out of bounds so that callback for home is fired. Makes for consistent code in the if-block
        if (initScreen == getString(R.string.pref_init_page_home_value)) {
            mBottomNav!!.currentItem = 0
        } else if (initScreen == getString(R.string.pref_init_page_planner_value)) {
            mBottomNav!!.currentItem = 1
        } else if (initScreen == getString(R.string.pref_init_page_calendar_value)) {
            mBottomNav!!.currentItem = 2
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.mnu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return false
    }

    private fun setupBottomNavigation() {
        val bottomNavAdapter = AHBottomNavigationAdapter(this, R.menu.bottom_menu)
        mBottomNav!!.accentColor = ContextCompat.getColor(this, R.color.colorAccent)
        bottomNavAdapter.setupWithBottomNavigation(mBottomNav)
        mBottomNav!!.setOnTabSelectedListener { position, wasSelected ->
            if (!wasSelected) {
                val transaction = mFragmentManager!!.beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                when (position) {
                    0 -> {
                        mFab!!.hide()
                        transaction.replace(R.id.fragment_content, HomeFragment()).commit()
                    }
                    1 -> {
                        mFab!!.show()
                        transaction.replace(R.id.fragment_content, PlannerFragment()).commit()
                    }
                    2 -> {
                        mFab!!.hide()
                        transaction.replace(R.id.fragment_content, CalendarFragment()).commit()
                    }
                }
            }
            true
        }
    }
}
