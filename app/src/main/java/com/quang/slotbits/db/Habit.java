package com.quang.slotbits.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "habits")
public class Habit implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    private String _name;

    @ColumnInfo(name = "slot_length")
    private int _slotLength;

    public Habit(String name, int slotLength) {
        _name = name;
        _slotLength = slotLength;
    }

    public String getName() {
        return _name;
    }

    public int getSlotLength() {
        return _slotLength;
    }

    public void setName(String name) { _name = name; }

    public void setSlotLength(int length) { _slotLength = length; }


    // Parcel overrides


    @Ignore
    public Habit(Parcel in) {
        id = in.readInt();
        _name = in.readString();
        _slotLength = in.readInt();
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString((String) _name);
        out.writeInt(_slotLength);
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    public static final Parcelable.Creator<Habit> CREATOR = new Parcelable.Creator<Habit>() {
        public Habit createFromParcel(Parcel in) {
            return new Habit(in);
        }

        public Habit[] newArray(int size) {
            return new Habit[size];
        }
    };
}
