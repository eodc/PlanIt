package io.eodc.planit.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
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
class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
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

        fabCreateAssignment.hide()
        fabCreateAssignment.setOnClickListener {
            if (supportFragmentManager != null) {
                AddAssignmentFragment().show(supportFragmentManager, null)
            }
        }
        bottomNavigation.setOnNavigationItemSelectedListener(this)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val initScreen = sharedPreferences.getString(getString(R.string.pref_init_page_key), getString(R.string.pref_init_page_home_value))

        val transaction = supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)

        when (initScreen) {
            getString(R.string.pref_init_page_home_value) -> {
                bottomNavigation.selectedItemId = R.id.bnv_home
                transaction.replace(R.id.fragment_content, HomeFragment()).commit()
            }
            getString(R.string.pref_init_page_planner_value) -> {
                bottomNavigation.selectedItemId = R.id.bnv_planner
                fabCreateAssignment.show()
                transaction.replace(R.id.fragment_content, PlannerFragment()).commit()
            }
            getString(R.string.pref_init_page_calendar_value) -> {
                bottomNavigation.selectedItemId = R.id.bnv_calendar
                transaction.replace(R.id.fragment_content, CalendarFragment()).commit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.mnu_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return false
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id != bottomNavigation.selectedItemId) {
            val transaction = supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            when (id) {
                R.id.bnv_home -> {
                    fabCreateAssignment.hide()
                    transaction.replace(R.id.fragment_content, HomeFragment()).commit()
                }
                R.id.bnv_planner -> {
                    fabCreateAssignment.show()
                    transaction.replace(R.id.fragment_content, PlannerFragment()).commit()
                }
                R.id.bnv_calendar -> {
                    fabCreateAssignment.hide()
                    transaction.replace(R.id.fragment_content, CalendarFragment()).commit()
                }
            }
            return true
        }
        return false
    }
}
