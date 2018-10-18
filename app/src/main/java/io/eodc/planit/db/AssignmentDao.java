package io.eodc.planit.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.joda.time.DateTime;

import java.util.List;

@Dao
public interface AssignmentDao {
    @Query("SELECT * FROM assignments ORDER BY dueDate, type, classId ASC")
    LiveData<List<Assignment>> getAllAssignments();

    // Notification service does not require live updates, and does not follow conventional lifecycle,
    // so attaching an observer is too complicated. Keep It Simple, Stupid.
    @Query("SELECT * FROM assignments WHERE dueDate BETWEEN :from AND :to ORDER BY dueDate, type, classId ASC ")
    List<Assignment> getStaticAssignmentsDueBetweenDates(DateTime from, DateTime to);

    @Query("SELECT * FROM assignments WHERE dueDate BETWEEN :from AND :to ORDER BY dueDate, type, classId ASC ")
    LiveData<List<Assignment>> getAssignmentsDueBetweenDates(DateTime from, DateTime to);

    @Query("SELECT * FROM assignments WHERE dueDate < :date ORDER BY dueDate, type, classId ASC")
    LiveData<List<Assignment>> getOverdueAssignments(DateTime date);

    @Query("SELECT * FROM assignments WHERE classId = :id ORDER BY dueDate, type, classId ASC")
    LiveData<List<Assignment>> getAssignmentsByClassId(int id);

    @Query("SELECT * FROM assignments WHERE id = :id ORDER BY dueDate, type, classId ASC")
    LiveData<Assignment> getAssignmentById(int id);

    @Update
    void updateAssignment(Assignment assignment);

    @Insert
    void insertAssignments(Assignment... assignments);

    @Delete
    void removeAssignment(Assignment... assignments);
}
