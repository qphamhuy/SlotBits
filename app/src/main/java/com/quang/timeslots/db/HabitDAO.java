package com.quang.timeslots.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface HabitDAO {
    @Insert
    long createHabit(Habit habit);

    @Insert
    long[] createHabits(List<Habit> habits);

    @Update
    void updateHabit(Habit habit);

    @Delete
    void deleteHabit(Habit habit);

    @Query("DELETE FROM habits")
    void deleteAllHabits();

    @Query("SELECT MAX(order_number) FROM habits")
    int getMaxOrderNumber();

    @Query("SELECT * FROM habits ORDER BY order_number")
    LiveData<List<Habit>> getAllHabits();

    @Query("SELECT * FROM habits ORDER BY order_number")
    List<Habit> getAllHabitsSync();
}
