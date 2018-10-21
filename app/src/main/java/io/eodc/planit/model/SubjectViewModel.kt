package io.eodc.planit.model

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import io.eodc.planit.db.PlannerDatabase
import io.eodc.planit.db.Subject

class SubjectViewModel(application: Application, id: Int) : AndroidViewModel(application) {
    val subject: Subject

    init {
        val subjectDao = PlannerDatabase.getInstance(application)!!.classDao()
        subject = subjectDao.getClassById(id)
    }
}
