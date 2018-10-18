package io.eodc.planit.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ClassDao {
    @Query("SELECT * FROM classes")
    List<Class> getAllClassesStatically();

    @Query("SELECT * FROM classes")
    LiveData<List<Class>> getAllClasses();

    @Query("SELECT * FROM classes WHERE id = :id")
    LiveData<Class> getClassById(int id);

    @Update
    void updateClasses(Class... updatedClass);

    @Insert
    void insertClasses(Class... insertedClasses);

    @Delete
    void removeClasses(Class... removedClasses);
}
