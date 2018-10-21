package io.eodc.planit.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface SubjectDao {
    @Query("SELECT * FROM subjects")
    List<Subject> getAllSubjects();

    @Query("SELECT * FROM subjects")
    LiveData<List<Subject>> getAllSubjectsObservable();

    @Query("SELECT * FROM subjects WHERE id = :id")
    Subject getClassById(int id);

    @Update
    void updateSubjects(Subject... updatedSubjects);

    @Insert
    void insertSubjects(Subject... insertedSubjects);

    @Delete
    void removeSubjects(Subject... removedSubjects);
}
