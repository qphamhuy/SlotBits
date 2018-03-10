package com.quang.slotbits;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.os.CountDownTimer;

import com.quang.slotbits.common.HabitTimerListener;
import com.quang.slotbits.common.HabitTimer;
import com.quang.slotbits.db.SlotBitsDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SlotBitsApplication extends Application {
    private static SlotBitsApplication _instance;
    private SlotBitsDatabase _db;

    @Override
    public void onCreate() {
        super.onCreate();

        _instance = this;
        _db = Room.databaseBuilder(getApplicationContext(), SlotBitsDatabase.class, "SlotBits")
                .build();
        HabitTimer.getInstance();
    }

    public static SlotBitsApplication getInstance() {
        return _instance;
    }

    public SlotBitsDatabase getDB() {
        return _db;
    }
}
