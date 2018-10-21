package io.eodc.planit.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface SubjectDao {
    @get:Query("SELECT * FROM subjects")
    val allSubjects: List<Subject>

    @get:Query("SELECT * FROM subjects")
    val allSubjectsObservable: LiveData<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getClassById(id: Int): Subject

    @Update
    fun updateSubjects(vararg updatedSubjects: Subject)

    @Insert
    fun insertSubjects(vararg insertedSubjects: Subject)

    @Delete
    fun removeSubjects(vararg removedSubjects: Subject)
}
