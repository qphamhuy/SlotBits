package com.quang.timeslots.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.R;

import org.joda.time.DateTime;
import org.json.JSONObject;

/**
 * DB entity class representing a completed slot
 */
@Entity(tableName = "slots", primaryKeys = {"habit_id", "date_completed"})
public class Slot {
    private final static String LOG_TAG = Slot.class.getName();
    private final static String COLUMN_NAME_HABIT_ID = "habit_id";
    private final static String COLUMN_NAME_DATE_COMPLETED = "date_completed";

    @ColumnInfo(name = COLUMN_NAME_HABIT_ID)
    private int _habitId;

    @ColumnInfo(name = COLUMN_NAME_DATE_COMPLETED)
    @NonNull
    private DateTime _dateCompleted;

    /**
     * Constructor
     * @param habitId - ID of habit for which this slot has been completed
     * @param dateCompleted - Date and time of completion
     */
    public Slot(int habitId, DateTime dateCompleted) {
        _habitId = habitId;
        _dateCompleted = dateCompleted;
    }

    /**
     * Constructor from a JSON object
     * @param jsonObject
     */
    public Slot(JSONObject jsonObject) {
        try {
            _habitId = jsonObject.getInt(COLUMN_NAME_HABIT_ID);
            _dateCompleted = new DateTime(jsonObject.getString(COLUMN_NAME_DATE_COMPLETED));
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, TimeSlotsApplication.getInstance().getString(R.string.log_error_json_to_slot, e.getMessage()));
        }
    }

    /**
     * Get ID of habit
     * @return
     */
    public int getHabitId() {
        return _habitId;
    }

    /**
     * Get date & time of completion
     * @return
     */
    public DateTime getDateCompleted() {
        return _dateCompleted;
    }

    /**
     * Construct a JSON object from this Slot
     * @return
     */
    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(COLUMN_NAME_HABIT_ID, _habitId);
            jsonObject.put(COLUMN_NAME_DATE_COMPLETED, _dateCompleted);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, TimeSlotsApplication.getInstance().getString(R.string.log_error_slot_to_json, e.getMessage()));
        }
        return jsonObject;
    }

    /**
     * Override of method to compare two slots
     * @param rhs - Slot to compare with
     * @return
     */
    @Override
    public boolean equals(Object rhs) {
        if (this == rhs)
            return true;
        if (rhs instanceof Slot) {
            Slot rhsSlot = (Slot) rhs;
            return (_habitId == rhsSlot._habitId && _dateCompleted.equals(rhsSlot._dateCompleted));
        }
        return false;
    }
}
