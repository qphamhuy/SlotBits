package com.quang.timeslots.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.net.Uri;

import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.common.FileIO;

import org.joda.time.DateTime;

import java.util.List;

@Dao
public abstract class SlotDAO {
    public void createSlot(Slot slot) {
        createSlotSQL(slot);
        Uri dataFileUri = TimeSlotsApplication.getInstance().getDataFileUri();
        if (dataFileUri != null)
            FileIO.getInstance().writeToFile(dataFileUri, null);
    }

    @Insert
    protected abstract void createSlotSQL(Slot slot);

    @Insert
    public abstract void createSlots(List<Slot> slots);

    @Query("SELECT date_completed FROM slots WHERE habit_id = :id")
    public abstract DateTime[] getHabitHistory(int id);

    @Query("DELETE FROM slots WHERE habit_id = :id")
    public abstract void deleteHabitHistory(int id);

    @Query("DELETE FROM slots")
    public abstract void deleteAllSlots();

    @Query("SELECT * FROM slots")
    public abstract List<Slot> getAllSlots();
}
