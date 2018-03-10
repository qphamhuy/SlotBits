package com.quang.slotbits.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

@Dao
public interface SlotDAO {
    @Insert
    public void createSlot(Slot slot);

    @Query("SELECT * FROM slots WHERE habit_id = :id")
    public Slot[] getHabitHistory(int id);
}
