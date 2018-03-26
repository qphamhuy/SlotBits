package com.quang.timeslots.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;

@Entity(tableName = "slots", primaryKeys = {"habit_id", "date_completed"})
public class Slot {
    @ColumnInfo(name = "habit_id")
    private int _habitId;
    @ColumnInfo(name = "date_completed")
    @NonNull
    private DateTime _dateCompleted;

    public Slot(int habitId, DateTime dateCompleted) {
        _habitId = habitId;
        _dateCompleted = dateCompleted;
    }

    public int getHabitId() {
        return _habitId;
    }

    public DateTime getDateCompleted() {
        return _dateCompleted;
    }
}
