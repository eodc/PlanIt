package io.eodc.planit.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem

import io.eodc.planit.R
import io.eodc.planit.adapter.SubjectAdapter
import io.eodc.planit.db.Subject
import io.eodc.planit.fragment.ModifyClassFragment
import io.eodc.planit.model.SubjectListViewModel
import kotlinx.android.synthetic.main.activity_subjects.*

/**
 * Activity for adding or modifying classes
 *
 * @author 2n
 */
class SubjectsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subjects)

        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewModelProviders.of(this).get<SubjectListViewModel>(SubjectListViewModel::class.java)
                .subjectsObservable.observe(this, Observer<List<Subject>> {this.onClassListChanged(it!!); })

        recycle_subject.layoutManager = LinearLayoutManager(this)
    }

    private fun onClassListChanged(subjects: List<Subject>) {
        val subjectAdapter = recycle_subject.adapter as SubjectAdapter?
        if (subjectAdapter != null) {
            subjectAdapter.swapClassesList(subjects)
        } else {
            recycle_subject.adapter = SubjectAdapter(subjects, this)
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
