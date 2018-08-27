package io.eodc.planit.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import io.eodc.planit.db.Class;
import io.eodc.planit.db.ClassDao;
import io.eodc.planit.db.PlannerDatabase;

public class ClassListViewModel extends AndroidViewModel {
    private ClassDao mClassDao;

    public ClassListViewModel(@NonNull Application application) {
        super(application);
        mClassDao = PlannerDatabase.getInstance(application).classDao();
    }

    public LiveData<List<Class>> getClasses() {
        return mClassDao.getAllClasses();
    }

    public void updateClasses(Class... classes) {
        mClassDao.updateClasses(classes);
    }

    public void insertClasses(Class... classes) {
        mClassDao.insertClasses(classes);
    }

    public void removeClasses(Class... classes) {
        mClassDao.removeClasses(classes);
    }
}
