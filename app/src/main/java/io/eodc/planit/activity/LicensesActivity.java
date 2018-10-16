package io.eodc.planit.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.adapter.License;
import io.eodc.planit.adapter.LicenseAdapter;

/**
 * Activity for displaying licenses
 *
 * @author 2n
 */
public class LicensesActivity extends AppCompatActivity {
    @BindView(R.id.tb)     Toolbar         mToolbar;
    @BindView(R.id.recycle_licence) RecyclerView    mRvLicenses;

    // If there's a better way to grab licenses from dependencies, pls push
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        List<License> licenses = new ArrayList<>();
        licenses.add(new License("AHBottomNavigation",
                2017,
                "Aurelien Hubert",
                "https://www.apache.org/licenses/LICENSE-2.0.html",
                "https://github.com/aurelhubert/ahbottomnavigation"));
        licenses.add(new License("ButterKnife",
                2013,
                "Jake Wharton",
                "https://www.apache.org/licenses/LICENSE-2.0.html",
                "https://github.com/JakeWharton/butterknife"));
        licenses.add(new License("CircleImageView",
                2018,
                "Henning Dodenhof",
                "https://www.apache.org/licenses/LICENSE-2.0.html",
                "https://github.com/hdodenhof/CircleImageView"));
        licenses.add(new License("Material Calendar View",
                2018,
                "Prolific Interactive",
                "https://opensource.org/licenses/MIT",
                "https://github.com/prolificinteractive/material-calendarview"));
        licenses.add(new License("MPAndroidChart",
                2018,
                "Phillip Jahoda",
                "https://www.apache.org/licenses/LICENSE-2.0.html",
                "https://github.com/PhilJay/MPAndroidChart"));
        licenses.add(new License("Spectrum",
                2016,
                "The Blue Alliance",
                "https://opensource.org/licenses/MIT",
                "https://github.com/the-blue-alliance/spectrum"));
        licenses.add(new License("Timber",
                2013,
                "Jake Wharton",
                "http://www.apache.org/licenses/LICENSE-2.0",
                "https://github.com/JakeWharton/timber"));
        licenses.add(new License("Joda-Time",
                2011,
                "Joda Stephen",
                "http://www.apache.org/licenses/LICENSE-2.0",
                "https://github.com/JodaOrg/joda-time"));
        mRvLicenses.setAdapter(new LicenseAdapter(this, licenses));
        mRvLicenses.setLayoutManager(new LinearLayoutManager(this));
    }
}
