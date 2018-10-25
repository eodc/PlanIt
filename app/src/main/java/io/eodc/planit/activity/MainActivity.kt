package io.eodc.planit.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import io.eodc.planit.BuildConfig
import io.eodc.planit.R
import io.eodc.planit.db.Subject
import io.eodc.planit.fragment.AddAssignmentFragment
import io.eodc.planit.fragment.CalendarFragment
import io.eodc.planit.fragment.HomeFragment
import io.eodc.planit.fragment.PlannerFragment
import io.eodc.planit.model.SubjectListViewModel
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

/**
 * Activity that the user mostly interacts with
 *
 * @author 2n
 */
class MainActivity : AppCompatActivity() {
    var subjects: List<Subject>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread {
            subjects = ViewModelProviders.of(this@MainActivity)
                    .get<SubjectListViewModel>(SubjectListViewModel::class.java)
                    .subjects
        }.start()

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        setSupportActionBar(tb)
        tb.inflateMenu(R.menu.main_menu)

        setupBottomNavigation()
        fab_create_assign.hide()
        fab_create_assign.setOnClickListener {
            if (supportFragmentManager != null) {
                AddAssignmentFragment().show(supportFragmentManager, null)
            }
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val initScreen = sharedPreferences.getString(getString(R.string.pref_init_page_key), getString(R.string.pref_init_page_home_value))

        nav_bottom.currentItem = -1 // Set nav to out of bounds so that callback for home is fired. Makes for consistent code in the if-block
        when (initScreen) {
            getString(R.string.pref_init_page_home_value) -> nav_bottom.currentItem = 0
            getString(R.string.pref_init_page_planner_value) -> nav_bottom.currentItem = 1
            getString(R.string.pref_init_page_calendar_value) -> nav_bottom.currentItem = 2
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
        nav_bottom.accentColor = ContextCompat.getColor(this, R.color.colorAccent)
        bottomNavAdapter.setupWithBottomNavigation(nav_bottom)
        nav_bottom.setOnTabSelectedListener { position, wasSelected ->
            if (!wasSelected) {
                val transaction = supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                when (position) {
                    0 -> {
                        fab_create_assign.hide()
                        transaction.replace(R.id.fragment_content, HomeFragment()).commit()
                    }
                    1 -> {
                        fab_create_assign.show()
                        transaction.replace(R.id.fragment_content, PlannerFragment()).commit()
                    }
                    2 -> {
                        fab_create_assign.hide()
                        transaction.replace(R.id.fragment_content, CalendarFragment()).commit()
                    }
                }
            }
            true
        }
    }
}
