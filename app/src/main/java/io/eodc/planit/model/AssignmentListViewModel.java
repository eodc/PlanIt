package io.eodc.planit.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.util.List;

import io.eodc.planit.db.Assignment;
import io.eodc.planit.db.AssignmentDao;
import io.eodc.planit.db.PlannerDatabase;

public class AssignmentListViewModel extends AndroidViewModel {
    private AssignmentDao mAssignmentDao;

    public AssignmentListViewModel(@NonNull Application application) {
        super(application);
        mAssignmentDao = PlannerDatabase.getInstance(application).assignmentDao();
    }

    public LiveData<List<Assignment>> getAllAssignments() {
        return mAssignmentDao.getAllAssignments();
    }

    public LiveData<List<Assignment>> getOverdueAssignments(DateTime date) {
        return mAssignmentDao.getOverdueAssignments(date);
    }

    public LiveData<List<Assignment>> getAssignmentsDueOnDay(DateTime date) {
        return mAssignmentDao.getAssignmentsDueBetweenDates(date.withTimeAtStartOfDay(), date.plusDays(1).minusMillis(1));
    }

    public LiveData<List<Assignment>> getAssignmentsBetweenDates(DateTime from, DateTime to) {
        return mAssignmentDao.getAssignmentsDueBetweenDates(from, to);
    }

    public LiveData<List<Assignment>> getAssignmentsByClassId(int id) {
        return mAssignmentDao.getAssignmentsByClassId(id);
    }

    public LiveData<Assignment> getAssignmentById(int id) {
        return mAssignmentDao.getAssignmentById(id);
    }

    public void insertAssignments(Assignment... assignments) {
        mAssignmentDao.insertAssignments(assignments);
    }

    public void removeAssignments(Assignment... assignments) {
        mAssignmentDao.removeAssignment(assignments);
    }
}
