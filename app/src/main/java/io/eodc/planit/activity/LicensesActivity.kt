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
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val licenses = arrayListOf(
                License("AHBottomNavigation",
                        2017,
                        "Aurelien Hubert",
                        "https://www.apache.org/licenses/LICENSE-2.0.html",
                        "https://github.com/aurelhubert/ahbottomnavigation"),
                License("CircleImageView",
                        2018,
                        "Henning Dodenhof",
                        "https://www.apache.org/licenses/LICENSE-2.0.html",
                        "https://github.com/hdodenhof/CircleImageView"),
                License("DragDropSwipeRecyclerview",
                        2018,
                        "ernestoyaquello",
                        "https://www.apache.org/licenses/LICENSE-2.0.html",
                        "https://github.com/ernestoyaquello/DragDropSwipeRecyclerview"),
                License("Material Calendar View",
                        2018,
                        "Prolific Interactive",
                        "https://opensource.org/licenses/MIT",
                        "https://github.com/prolificinteractive/material-calendarview"),
                License("MPAndroidChart",
                        2018,
                        "Phillip Jahoda",
                        "https://www.apache.org/licenses/LICENSE-2.0.html",
                        "https://github.com/PhilJay/MPAndroidChart"),
                License("Spectrum",
                        2016,
                        "The Blue Alliance",
                        "https://opensource.org/licenses/MIT",
                        "https://github.com/the-blue-alliance/spectrum"),
                License("Timber",
                        2013,
                        "Jake Wharton",
                        "http://www.apache.org/licenses/LICENSE-2.0",
                        "https://github.com/JakeWharton/timber"),
                License("Joda-Time",
                        2011,
                        "Joda Stephen",
                        "http://www.apache.org/licenses/LICENSE-2.0",
                        "https://github.com/JodaOrg/joda-time"))
        recycle_licence.adapter = LicenseAdapter(this, licenses)
        recycle_licence.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
    }
}
