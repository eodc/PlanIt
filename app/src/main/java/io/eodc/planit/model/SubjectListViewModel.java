package io.eodc.planit.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import io.eodc.planit.db.PlannerDatabase;
import io.eodc.planit.db.Subject;
import io.eodc.planit.db.SubjectDao;

public class SubjectListViewModel extends AndroidViewModel {
    private SubjectDao mSubjectDao;

    public SubjectListViewModel(@NonNull Application application) {
        super(application);
        mSubjectDao = PlannerDatabase.getInstance(application).classDao();
    }

    public List<Subject> getSubjects() {
        return mSubjectDao.getAllSubjects();
    }

    public LiveData<List<Subject>> getSubjectsObservable() {
        return mSubjectDao.getAllSubjectsObservable();
    }

    public void updateSubjects(Subject... subjects) {
        mSubjectDao.updateSubjects(subjects);
    }

    public void insertSubjects(Subject... subjects) {
        mSubjectDao.insertSubjects(subjects);
    }

    public void removeSubjects(Subject... subjects) {
        mSubjectDao.removeSubjects(subjects);
    }
}
