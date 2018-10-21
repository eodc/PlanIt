package io.eodc.planit.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

import butterknife.BindView
import butterknife.ButterKnife
import io.eodc.planit.R

/**
 * Activity for the settings page
 *
 * @see io.eodc.planit.fragment.SettingsFragment
 */
class SettingsActivity : AppCompatActivity() {
    @BindView(R.id.tb)
    internal var mToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ButterKnife.bind(this)

        setSupportActionBar(mToolbar)
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}
