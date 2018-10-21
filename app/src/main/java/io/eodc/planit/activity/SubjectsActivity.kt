package io.eodc.planit.activity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem

import butterknife.BindView
import butterknife.ButterKnife
import io.eodc.planit.R
import io.eodc.planit.adapter.SubjectAdapter
import io.eodc.planit.db.Subject
import io.eodc.planit.fragment.ModifyClassFragment
import io.eodc.planit.model.SubjectListViewModel

/**
 * Activity for adding or modifying classes
 *
 * @author 2n
 */
class SubjectsActivity : AppCompatActivity() {

    @BindView(R.id.tb)
    internal var mToolbar: Toolbar? = null
    @BindView(R.id.recycle_class)
    internal var mRvClasses: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classes)
        ButterKnife.bind(this)

        setSupportActionBar(mToolbar)
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        ViewModelProviders.of(this).get<SubjectListViewModel>(SubjectListViewModel::class.java!!)
                .subjectsObservable.observe(this, Observer<List<Subject>> { this.onClassListChanged(it) })

        mRvClasses!!.layoutManager = LinearLayoutManager(this)
    }

    private fun onClassListChanged(subjects: List<Subject>) {
        val subjectAdapter = mRvClasses!!.adapter as SubjectAdapter?
        if (subjectAdapter != null) {
            subjectAdapter.swapClassesList(subjects)
        } else {
            mRvClasses!!.adapter = SubjectAdapter(subjects, this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.classes_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.mnu_add_class) {
            ModifyClassFragment.newInstance(null)
                    .show(supportFragmentManager, null)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
