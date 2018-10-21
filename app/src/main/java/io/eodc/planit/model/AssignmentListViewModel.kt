package io.eodc.planit.model

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import io.eodc.planit.db.Assignment
import io.eodc.planit.db.AssignmentDao
import io.eodc.planit.db.PlannerDatabase
import org.joda.time.DateTime

class AssignmentListViewModel(application: Application) : AndroidViewModel(application) {
    private val mAssignmentDao: AssignmentDao

    val allAssignments: LiveData<List<Assignment>>
        get() = mAssignmentDao.allAssignments

    init {
        mAssignmentDao = PlannerDatabase.getInstance(application)!!.assignmentDao()
    }

    fun getOverdueAssignments(date: DateTime): LiveData<List<Assignment>> {
        return mAssignmentDao.getOverdueAssignments(date)
    }

    fun getAssignmentsDueOnDay(date: DateTime): LiveData<List<Assignment>> {
        return mAssignmentDao.getAssignmentsDueBetweenDates(date.withTimeAtStartOfDay(), date.plusDays(1).minusMillis(1))
    }

    fun getAssignmentsBetweenDates(from: DateTime, to: DateTime): LiveData<List<Assignment>> {
        return mAssignmentDao.getAssignmentsDueBetweenDates(from, to)
    }

    fun getAssignmentsByClassId(id: Int): LiveData<List<Assignment>> {
        return mAssignmentDao.getAssignmentsByClassId(id)
    }

    fun getAssignmentById(id: Int): LiveData<Assignment> {
        return mAssignmentDao.getAssignmentById(id)
    }

    fun insertAssignments(vararg assignments: Assignment) {
        mAssignmentDao.insertAssignments(*assignments)
    }

    fun removeAssignments(vararg assignments: Assignment) {
        mAssignmentDao.removeAssignment(*assignments)
    }
}
