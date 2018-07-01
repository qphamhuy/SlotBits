package com.quang.timeslots.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.net.Uri;

import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.common.FileIO;

import java.util.List;

@Dao
public abstract class HabitDAO {
    public long createHabit(Habit habit) {
        long id = createHabitSql(habit);
        Uri dataFileUri = TimeSlotsApplication.getInstance().getDataFileUri();
        if (dataFileUri != null)
            FileIO.getInstance().writeToFile(dataFileUri, null);
        return id;
    }

    @Insert
    public abstract long createHabitSql(Habit habit);

    @Insert
    public abstract long[] createHabits(List<Habit> habits);

    @Update
    public abstract void updateHabit(Habit habit);

    @Delete
    public abstract void deleteHabit(Habit habit);

    @Query("DELETE FROM habits")
    public abstract void deleteAllHabits();

    @Query("SELECT MAX(order_number) FROM habits")
    public abstract int getMaxOrderNumber();

    @Query("SELECT * FROM habits ORDER BY order_number")
    public abstract LiveData<List<Habit>> getAllHabits();

    @Query("SELECT * FROM habits ORDER BY order_number")
    public abstract List<Habit> getAllHabitsSync();
}
