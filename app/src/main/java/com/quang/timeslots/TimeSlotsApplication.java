package com.quang.timeslots;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.quang.timeslots.common.HabitTimer;
import com.quang.timeslots.db.TimeSlotsDatabase;

import net.danlew.android.joda.JodaTimeAndroid;

public class TimeSlotsApplication extends Application {
    private static TimeSlotsApplication _instance;
    private TimeSlotsDatabase _db;

    @Override
    public void onCreate() {
        super.onCreate();

        _instance = this;
        _db = Room.databaseBuilder(getApplicationContext(), TimeSlotsDatabase.class, "TimeSlots")
                .build();
        HabitTimer.getInstance();

        JodaTimeAndroid.init(this);
    }

    public static TimeSlotsApplication getInstance() {
        return _instance;
    }

    public TimeSlotsDatabase getDB() {
        return _db;
    }
}
