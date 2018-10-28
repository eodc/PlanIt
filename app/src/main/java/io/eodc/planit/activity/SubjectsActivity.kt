package io.eodc.planit.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import io.eodc.planit.R
import io.eodc.planit.adapter.SubjectAdapter
import io.eodc.planit.db.Subject
import io.eodc.planit.fragment.ModifySubjectFragment
import io.eodc.planit.model.SubjectListViewModel
import kotlinx.android.synthetic.main.activity_subjects.*

/**
 * Activity for adding or modifying subjects
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

        recycleSubject.layoutManager = LinearLayoutManager(this)
    }

    private fun onClassListChanged(subjects: List<Subject>) {
        val subjectAdapter = recycleSubject.adapter as SubjectAdapter?
        if (subjectAdapter != null) {
            subjectAdapter.swapSubjectsList(subjects)
        } else {
            recycleSubject.adapter = SubjectAdapter(subjects, this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.subjects_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.mnu_add_subject) {
            ModifySubjectFragment.newInstance(null)
                    .show(supportFragmentManager, null)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
