package com.quang.timeslots.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.R;

import org.json.JSONObject;

/**
 * DB entity class that represents a user habit
 */
@Entity(tableName = "habits")
public class Habit implements Parcelable {
    private static final String LOG_TAG = Habit.class.getName();
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_ORDER_NUMBER = "order_number";
    private static final String COLUMN_NAME_NAME = "name";
    private static final String COLUMN_NAME_SLOT_LENGTH = "slot_length";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_NAME_ID)
    public int id;

    @ColumnInfo(name = COLUMN_NAME_ORDER_NUMBER)
    public int orderNumber;

    @ColumnInfo(name = COLUMN_NAME_NAME)
    private String _name;

    @ColumnInfo(name = COLUMN_NAME_SLOT_LENGTH)
    private int _slotLength;

    /**
     * Constructor
     * @param name - Habit name
     * @param slotLength - Slot length
     */
    public Habit(String name, int slotLength) {
        _name = name;
        _slotLength = slotLength;
    }

    /**
     * Constructor from a JSON object
     * @param jsonObject
     */
    public Habit(JSONObject jsonObject) {
        try {
            id = jsonObject.getInt(COLUMN_NAME_ID);
            orderNumber = jsonObject.getInt(COLUMN_NAME_ORDER_NUMBER);
            _name = jsonObject.getString(COLUMN_NAME_NAME);
            _slotLength = jsonObject.getInt(COLUMN_NAME_SLOT_LENGTH);
        }
        catch(Exception e) {
            e.printStackTrace();
            Log.e(
                LOG_TAG,
                TimeSlotsApplication.getInstance().getString(R.string.log_error_json_to_habit, e.getMessage()));
        }
    }

    /**
     * Get habit name
     * @return
     */
    public String getName() {
        return _name;
    }

    /**
     * Get habit's slot length
     * @return
     */
    public int getSlotLength() {
        return _slotLength;
    }

    /**
     * Construct a JSON object from this Habit
     * @return
     */
    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(COLUMN_NAME_ID, id);
            jsonObject.put(COLUMN_NAME_ORDER_NUMBER, orderNumber);
            jsonObject.put(COLUMN_NAME_NAME, _name);
            jsonObject.put(COLUMN_NAME_SLOT_LENGTH, _slotLength);
        }
        catch(Exception e) {
            e.printStackTrace();
            Log.e(
                LOG_TAG,
                TimeSlotsApplication.getInstance().getString(R.string.log_error_habit_to_json, e.getMessage()));
        }
        return jsonObject;
    }


    // Parcel overrides (to pass between activities)


    public Habit(Parcel in) {
        id = in.readInt();
        _name = in.readString();
        _slotLength = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString((String) _name);
        out.writeInt(_slotLength);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Habit> CREATOR = new Parcelable.Creator<Habit>() {
        public Habit createFromParcel(Parcel in) {
            return new Habit(in);
        }

        public Habit[] newArray(int size) {
            return new Habit[size];
        }
    };
}
