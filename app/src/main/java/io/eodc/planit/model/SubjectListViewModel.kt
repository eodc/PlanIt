package io.eodc.planit.model

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

import io.eodc.planit.db.PlannerDatabase
import io.eodc.planit.db.Subject
import io.eodc.planit.db.SubjectDao

class SubjectListViewModel(application: Application) : AndroidViewModel(application) {
    private val mSubjectDao: SubjectDao

    val subjects: List<Subject>
        get() = mSubjectDao.allSubjects

    val subjectsObservable: LiveData<List<Subject>>
        get() = mSubjectDao.allSubjectsObservable

    init {
        mSubjectDao = PlannerDatabase.getInstance(application)!!.classDao()
    }

    fun updateSubjects(vararg subjects: Subject) {
        mSubjectDao.updateSubjects(*subjects)
    }

    fun insertSubjects(vararg subjects: Subject) {
        mSubjectDao.insertSubjects(*subjects)
    }

    fun removeSubjects(vararg subjects: Subject) {
        mSubjectDao.removeSubjects(*subjects)
    }
}
