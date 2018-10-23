package io.eodc.planit.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.joda.time.DateTime

@Dao
interface AssignmentDao {
    @get:Query("SELECT * FROM assignments ORDER BY dueDate, type, classId ASC")
    val allAssignments: LiveData<List<Assignment>>

    // Notification service does not require live updates, and does not follow conventional lifecycle,
    // so attaching an observer is too complicated. Keep It Simple, Stupid.
    @Query("SELECT * FROM assignments WHERE dueDate BETWEEN :from AND :to ORDER BY dueDate, type, classId ASC ")
    fun getStaticAssignmentsDueBetweenDates(from: DateTime, to: DateTime): List<Assignment>

    @Query("SELECT * FROM assignments WHERE dueDate BETWEEN :from AND :to ORDER BY dueDate, type, classId ASC ")
    fun getAssignmentsDueBetweenDates(from: DateTime, to: DateTime): LiveData<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE dueDate < :date ORDER BY dueDate, type, classId ASC")
    fun getOverdueAssignments(date: DateTime): LiveData<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE classId = :id ORDER BY dueDate, type, classId ASC")
    fun getAssignmentsByClassId(id: Int): LiveData<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE id = :id ORDER BY dueDate, type, classId ASC")
    fun getAssignmentById(id: Int): LiveData<Assignment>

    @Update
    fun updateAssignment(assignment: Assignment)

    @Insert
    fun insertAssignments(vararg assignments: Assignment)

    @Delete
    fun removeAssignment(vararg assignments: Assignment)
}
