package com.quang.slotbits.habitdetails;

import com.quang.slotbits.db.Slot;

import java.util.Date;

/**
 * Restructured array of slots to represent the history of a habit, for easier display in the UI
 */
public class HabitHistory {
    private int _numOfSlots;
    private Date _lastCompleted;

    /**
     * Constructor
     * @param slots - Array of slots for given habit
     */
    HabitHistory(Slot[] slots) {
        _numOfSlots = slots.length;
        _lastCompleted = slots[slots.length - 1].getDateCompleted();
    }

    public int getNumOfSlots() {
        return _numOfSlots;
    }

    public Date getLastCompleted() {
        return _lastCompleted;
    }
}