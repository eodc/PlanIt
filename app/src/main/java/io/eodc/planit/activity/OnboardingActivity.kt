package io.eodc.planit.activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import io.eodc.planit.R
import io.eodc.planit.fragment.OnboardingAddClassesFragment
import io.eodc.planit.fragment.OnboardingFragment
import io.eodc.planit.model.SubjectListViewModel
import kotlinx.android.synthetic.main.activity_onboarding.*
import kotlinx.android.synthetic.main.layout_onboarding_controls.*

/**
 * Activity that is shown the first time the app is opened. Due to this, it is the default activity
 * but switches over to [MainActivity] if the first time flag has been tripped.
 *
 * @author 2n
 */
class OnboardingActivity : AppCompatActivity(), androidx.viewpager.widget.ViewPager.OnPageChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean(getString(R.string.pref_first_time_key), true)) {
            setContentView(R.layout.activity_onboarding)

            val mOnboardingFragments = arrayListOf(
                    OnboardingFragment.newInstance(getString(R.string.app_name),
                            R.drawable.ic_logo_24dp,
                            getString(R.string.tagline)),
                    OnboardingFragment.newInstance("Your Planner, Your Way",
                            R.drawable.ic_format_list_bulleted_blue_250dp,
                            "See your assignments in an overview, list, or calendar. It's your choice."),
                    OnboardingAddClassesFragment())
            pager.adapter = OnboardingPagerAdapter(supportFragmentManager, mOnboardingFragments)
            layout_tab.setupWithViewPager(pager)
            pager.addOnPageChangeListener(this)

            btn_next.setOnClickListener { nextSlide() }
            btn_back.setOnClickListener { prevSlide() }

            preferences.edit()
                    .putString(getString(R.string.pref_show_notif_time_key), "19:00")
                    .apply()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun nextSlide() {
        pager.currentItem = pager.currentItem + 1
    }

    private fun prevSlide() {
        pager.currentItem = pager.currentItem - 1
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        when (position) {
            0 -> btn_back.visibility = View.GONE
            layout_tab.tabCount - 1 -> {
                btn_next.setText(R.string.btn_finish_label)
                ViewModelProviders.of(this).get<SubjectListViewModel>(SubjectListViewModel::class.java)
                        .subjectsObservable.observe(this, Observer { subjects ->
                    if (subjects != null) {
                        if (layout_tab.selectedTabPosition == layout_tab.tabCount - 1) {
                            if (subjects.isEmpty()) {
                                btn_next.setOnClickListener(null)
                                btn_next.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                            } else {
                                btn_next.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                                btn_next.setOnClickListener { _ ->
                                    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                                    preferences.edit().putBoolean(getString(R.string.pref_first_time_key), false).apply()
                                    val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                })

            }
            else -> {
                btn_back.visibility = View.VISIBLE
                btn_next.text = getString(R.string.btn_next_label)
                btn_next.setOnClickListener { v -> nextSlide() }
                btn_next.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}

    /**
     * Adapter for interfacing [OnboardingFragment] with the [ViewPager].
     *
     * @author 2n
     */
    private inner class OnboardingPagerAdapter
    /**
     * Creates a new instance of OnboardingPagerAdapter
     *
     * @param fm        Instance of [FragmentManager]
     * @param fragments List of [OnboardingFragment] to insert into the adapter
     */
    internal constructor(fm: androidx.fragment.app.FragmentManager, private val fragments: List<OnboardingFragment>) : androidx.fragment.app.FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }
}
