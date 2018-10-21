package io.eodc.planit.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import io.eodc.planit.db.PlannerDatabase;
import io.eodc.planit.db.Subject;
import io.eodc.planit.db.SubjectDao;

public class SubjectViewModel extends AndroidViewModel {
    private Subject mSubject;

    public SubjectViewModel(@NonNull Application application, int id) {
        super(application);
        SubjectDao subjectDao = PlannerDatabase.getInstance(application).classDao();
        mSubject = subjectDao.getClassById(id);
    }

    public Subject getSubject() {
        return mSubject;
    }
}
