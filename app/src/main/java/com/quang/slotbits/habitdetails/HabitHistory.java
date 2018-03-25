package com.quang.slotbits.habitdetails;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Restructured array of slots to represent the history of a habit, for easier display in the UI
 */
public class HabitHistory {
    /**
     * Pair of date - number of slots completed on that date
     */
    public class DailyCount {
        public LocalDate date;
        public int count;

        DailyCount(LocalDate date, int count) {
            this.date = date;
            this.count = count;
        }
    }

    private int _habitID;
    private List<DailyCount> _dailyCounts;
    private int _totalCount;

    /**
     * Constructor
     * @param slots - Array of slots for given habit
     */
    HabitHistory(int habitID, DateTime[] slots) {
        _habitID = habitID;
        _dailyCounts = new ArrayList<>();
        _totalCount = slots.length;

        //Populate history with DailyCounts generated from slots. If there's insufficient
        //data, add zero counts to have an array of seven days.
        LocalDate today = new LocalDate();
        LocalDate todayMinusSeven = today.minusDays(7);
        LocalDate firstSlotDate = (slots.length > 0 ? slots[0].toLocalDate() : today);
        LocalDate firstDate = firstSlotDate.isBefore(todayMinusSeven) ? firstSlotDate : todayMinusSeven;

        LocalDate tomorrow = today.plusDays(1);
        int slotIdx = 0;
        for (LocalDate d = firstDate; d.isBefore(tomorrow); d = d.plusDays(1)) {
            DailyCount dailyCount = new DailyCount(d, 0);
            while (slotIdx < slots.length && slots[slotIdx].toLocalDate().isEqual(d)) {
                ++dailyCount.count;
                ++slotIdx;
            }
            _dailyCounts.add(dailyCount);
        }
    }

    public List<DailyCount> getDailyCounts() {
        return _dailyCounts;
    }

    public int getTotalCount() {
        return _totalCount;
    }

    public LocalDate getLastCompleted() {
        return _dailyCounts.get(_dailyCounts.size() - 1).date;
    }
}