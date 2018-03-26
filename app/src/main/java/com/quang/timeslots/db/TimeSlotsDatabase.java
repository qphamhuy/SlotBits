package com.quang.timeslots.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {Habit.class, Slot.class}, version = 1)
@TypeConverters({Converters.class})
public abstract  class TimeSlotsDatabase extends RoomDatabase {
    public abstract HabitDAO habitDAO();

    public abstract SlotDAO slotDAO();
}
