package io.eodc.planit.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.eodc.planit.R
import io.eodc.planit.adapter.License
import io.eodc.planit.adapter.LicenseAdapter
import kotlinx.android.synthetic.main.activity_licenses.*

/**
 * Activity for displaying licenses
 *
 * @author 2n
 */
class LicensesActivity : AppCompatActivity() {

    // If there's a better way to grab licenses from dependencies, pls push
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)

        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val licenses = arrayListOf(
                License("CircleImageView",
                        2018,
                        "Henning Dodenhof",
                        LICENCE_APACHE_2,
                        "https://github.com/hdodenhof/CircleImageView"),
                License("DragDropSwipeRecyclerview",
                        2018,
                        "ernestoyaquello",
                        LICENCE_APACHE_2,
                        "https://github.com/ernestoyaquello/DragDropSwipeRecyclerview"),
                License("Material Calendar View",
                        2018,
                        "Prolific Interactive",
                        LICENCE_MIT,
                        "https://github.com/prolificinteractive/material-calendarview"),
                License("MPAndroidChart",
                        2018,
                        "Phillip Jahoda",
                        LICENCE_APACHE_2,
                        "https://github.com/PhilJay/MPAndroidChart"),
                License("Spectrum",
                        2016,
                        "The Blue Alliance",
                        LICENCE_MIT,
                        "https://github.com/the-blue-alliance/spectrum"),
                License("Timber",
                        2013,
                        "Jake Wharton",
                        LICENCE_APACHE_2,
                        "https://github.com/JakeWharton/timber"),
                License("Joda-Time",
                        2011,
                        "Joda Stephen",
                        LICENCE_APACHE_2,
                        "https://github.com/JodaOrg/joda-time"))
        recycle_licence.adapter = LicenseAdapter(this, licenses)
        recycle_licence.layoutManager = LinearLayoutManager(this)
    }

    companion object {
        const val LICENCE_APACHE_2 = "https://www.apache.org/licenses/LICENSE-2.0"
        const val LICENCE_MIT = "https://opensource.org/licenses/MIT"
    }
}
