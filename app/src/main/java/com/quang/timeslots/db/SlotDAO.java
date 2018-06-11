package com.quang.timeslots.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import org.joda.time.DateTime;

import java.util.List;

@Dao
public interface SlotDAO {
    @Insert
    void createSlot(Slot slot);

    @Insert
    void createSlots(List<Slot> slots);

    @Query("SELECT date_completed FROM slots WHERE habit_id = :id")
    DateTime[] getHabitHistory(int id);

    @Query("DELETE FROM slots WHERE habit_id = :id")
    void deleteHabitHistory(int id);

    @Query("DELETE FROM slots")
    void deleteAllSlots();

    @Query("SELECT * FROM slots")
    List<Slot> getAllSlots();
}
