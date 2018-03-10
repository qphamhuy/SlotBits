package com.quang.slotbits.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "slots", primaryKeys = {"habit_id", "date_completed"})
public class Slot {
    @ColumnInfo(name = "habit_id")
    private int _habitId;
    @ColumnInfo(name = "date_completed")
    @NonNull
    private Date _dateCompleted;

    public Slot(int habitId, Date dateCompleted) {
        _habitId = habitId;
        _dateCompleted = dateCompleted;
    }

    public int getHabitId() {
        return _habitId;
    }

    public Date getDateCompleted() {
        return _dateCompleted;
    }
}
