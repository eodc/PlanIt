package io.eodc.planit.activity

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import io.eodc.planit.R
import io.eodc.planit.fragment.OnboardingAddClassesFragment
import io.eodc.planit.fragment.OnboardingFragment
import io.eodc.planit.model.SubjectListViewModel
import java.util.*

/**
 * Activity that is shown the first time the app is opened. Due to this, it is the default activity
 * but switches over to [MainActivity] if the first time flag has been tripped.
 *
 * @author 2n
 */
class OnboardingActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {

    @BindView(R.id.pager)
    internal var mViewPager: ViewPager? = null
    @BindView(R.id.layout_tab)
    internal var mTabLayout: TabLayout? = null
    @BindView(R.id.btn_back)
    internal var mBtnBack: Button? = null
    @BindView(R.id.btn_next)
    internal var mBtnNext: Button? = null

    private var mOnboardingFragments: MutableList<OnboardingFragment>? = null

    /**
     * Shows the next slide in the carousel
     */
    @OnClick(R.id.btn_next)
    internal fun nextSlide() {
        mViewPager!!.currentItem = mViewPager!!.currentItem + 1
    }

    /**
     * Goes back a slide in the carousel
     */
    @OnClick(R.id.btn_back)
    internal fun backSlide() {
        mViewPager!!.currentItem = mViewPager!!.currentItem - 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean(getString(R.string.pref_first_time_key), true)) {
            setContentView(R.layout.activity_onboarding)
            ButterKnife.bind(this)

            mOnboardingFragments = ArrayList()
            mOnboardingFragments!!.add(OnboardingFragment.newInstance(getString(R.string.app_name),
                    R.drawable.ic_logo_24dp,
                    getString(R.string.tagline)))
            mOnboardingFragments!!.add(OnboardingFragment.newInstance("Your Planner, Your Way",
                    R.drawable.ic_format_list_bulleted_blue_250dp,
                    "See your assignments in an overview, list, or calendar. It's your choice."))
            mOnboardingFragments!!.add(OnboardingAddClassesFragment())
            mViewPager!!.adapter = OnboardingPagerAdapter(supportFragmentManager, mOnboardingFragments)
            mTabLayout!!.setupWithViewPager(mViewPager)
            mViewPager!!.addOnPageChangeListener(this)

            preferences.edit()
                    .putString(getString(R.string.pref_show_notif_time_key), "19:00")
                    .apply()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        if (position == 0)
            mBtnBack!!.visibility = View.GONE
        else if (position == mTabLayout!!.tabCount - 1) {
            mBtnNext!!.setText(R.string.btn_finish_label)
            ViewModelProviders.of(this).get<SubjectListViewModel>(SubjectListViewModel::class.java!!)
                    .subjectsObservable.observe(this, { classes ->
                if (classes != null) {
                    if (mTabLayout!!.selectedTabPosition == mTabLayout!!.tabCount - 1) {
                        if (classes!!.size == 0) {
                            mBtnNext!!.setOnClickListener(null)
                            mBtnNext!!.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                        } else {
                            mBtnNext!!.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                            mBtnNext!!.setOnClickListener { v ->
                                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                                preferences.edit().putBoolean(getString(R.string.pref_first_time_key), false).apply()
                                val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }
            })

        } else {
            mBtnBack!!.visibility = View.VISIBLE
            mBtnNext!!.text = getString(R.string.btn_next_label)
            mBtnNext!!.setOnClickListener { v -> nextSlide() }
            mBtnNext!!.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
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
    internal constructor(fm: FragmentManager, private val fragments: List<OnboardingFragment>) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }
}
