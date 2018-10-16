package io.eodc.planit.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.joda.time.DateTime;

@Entity(tableName = "assignments")
public class Assignment {
    public static final int TYPE_TEST        = 1 << 1;
    public static final int TYPE_PROJECT     = 1 << 2;
    public static final int TYPE_HOMEWORK    = 1 << 3;

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo
    private String title;
    @ColumnInfo
    private int classId;
    @ColumnInfo
    private int type;
    @ColumnInfo
    private DateTime dueDate;
    @ColumnInfo
    private String notes;

    public Assignment(String title, int classId, DateTime dueDate, String notes) {
        this.title = title;
        this.classId = classId;
        this.dueDate = dueDate;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public DateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(DateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return title;
    }
}
