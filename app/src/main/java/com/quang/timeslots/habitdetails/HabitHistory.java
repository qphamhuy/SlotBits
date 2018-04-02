package com.quang.timeslots.habitdetails;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

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
    private LocalDate _lastCompleted;

    /**
     * Constructor
     * @param slots - Array of slots for given habit
     */
    HabitHistory(int habitID, DateTime[] slots) {
        _habitID = habitID;
        _dailyCounts = new ArrayList<>();
        _lastCompleted = (slots.length > 0 ? slots[slots.length - 1].toLocalDate() : null);

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

    /**
     * Get array of daily slot counts
     * @return Array of DailyCount objects
     */
    public List<DailyCount> getDailyCounts() {
        return _dailyCounts;
    }

    /**
     * Get slot count over the last N days
     * @param numDays - Last N days to get count for
     * @return Int count
     */
    public int getCountInLastNDays(int numDays) {
        int count = 0;
        numDays = Math.min(numDays, _dailyCounts.size());
        for (int i = 0; i < numDays; ++i) {
            count += _dailyCounts.get(_dailyCounts.size() - i - 1).count;
        }
        return count;
    }

    /**
     * Get date of last completed slot
     * @return Nullable LocalDate object
     */
    public LocalDate getLastCompleted() {
        return _lastCompleted;
    }
}