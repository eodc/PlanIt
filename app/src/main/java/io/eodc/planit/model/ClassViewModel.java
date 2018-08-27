package io.eodc.planit.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import io.eodc.planit.db.Class;
import io.eodc.planit.db.ClassDao;
import io.eodc.planit.db.PlannerDatabase;

public class ClassViewModel extends AndroidViewModel {
    private LiveData<Class> mClass;
    public ClassViewModel(@NonNull Application application, int id) {
        super(application);
        ClassDao classDao = PlannerDatabase.getInstance(application).classDao();
        mClass = classDao.getClassById(id);
    }

    public LiveData<Class> getAssignment() {
        return mClass;
    }
}
