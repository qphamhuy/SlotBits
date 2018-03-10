package com.quang.slotbits.db;

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
    public long createHabit(Habit habit);

    @Update
    public void updateHabit(Habit habit);

    @Delete
    public void deleteHabit(Habit habit);

    @Query("SELECT * FROM habits")
    public LiveData<List<Habit>> getAllHabits();
}
