package io.eodc.planit.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import io.eodc.planit.R
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * Activity for the settings page
 *
 * @see io.eodc.planit.fragment.SettingsFragment
 */
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(tb)
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}
