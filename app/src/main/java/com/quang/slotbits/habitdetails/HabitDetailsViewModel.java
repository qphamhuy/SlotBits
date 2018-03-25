package com.quang.slotbits.habitdetails;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;

import com.quang.slotbits.SlotBitsApplication;
import com.quang.slotbits.db.Habit;
import com.quang.slotbits.db.SlotBitsDatabase;

/**
 * View model for HabitDetailsActivity
 */
public class HabitDetailsViewModel extends ViewModel {
    private MutableLiveData<Habit> _habit = new MutableLiveData<>();
    private MutableLiveData<HabitHistory> _habitHistory = new MutableLiveData<>();
    private SlotBitsDatabase _db;

    /**
     * Constructor
     * @param habit - Habit in focus
     */
    public HabitDetailsViewModel(Habit habit) {
        _db = SlotBitsApplication.getInstance().getDB();
        _habit.setValue(habit);
        new UpdateHistoryAsyncTask(_db, _habitHistory).execute(habit.id);
    }

    /**
     * Get live data with selected habit
     * @return Habit live data
     */
    public MutableLiveData<Habit> getHabit() {
        return _habit;
    }

    /**
     * Get live data with the selected habit's history
     * @return Live data with list of Slots
     */
    public MutableLiveData<HabitHistory> getHabitHistory() {
        return _habitHistory;
    }

    /**
     * Delete selected habit
     */
    public void deleteHabit() {
        new DeleteHabitAsyncTask(_db, _habit).execute(_habit.getValue());
    }

    /**
     * Update selected habit
     * @param habit - Habit object with updated data
     */
    public void updateHabit(Habit habit) {
        new UpdateHabitAsyncTask(_db, _habit).execute(habit);
    }

    public void updateHabitHistory() {
        new UpdateHistoryAsyncTask(_db, _habitHistory).execute(_habit.getValue().id);
    }


    //////////


    /**
     * Async task to delete selected habit
     */
    private static class DeleteHabitAsyncTask extends AsyncTask<Habit, Void, Void> {
        private SlotBitsDatabase _db;
        private MutableLiveData<Habit> _habit;

        DeleteHabitAsyncTask(SlotBitsDatabase db, MutableLiveData<Habit> habit) {
            _db = db;
            _habit = habit;
        }

        @Override
        public Void doInBackground(final Habit... habits) {
            _db.habitDAO().deleteHabit(habits[0]);
            _db.slotDAO().deleteHabitHistory(habits[0].id);
            _habit.postValue(null);
            return null;
        }
    }

    /**
     * Async task to update selected habit
     */
    private class UpdateHabitAsyncTask extends AsyncTask<Habit, Void, Void> {
        private SlotBitsDatabase _db;
        private MutableLiveData<Habit> _habit;

        UpdateHabitAsyncTask(SlotBitsDatabase db, MutableLiveData<Habit> habit) {
            _db = db;
            _habit = habit;
        }

        @Override
        public Void doInBackground(final Habit... habits) {
            _db.habitDAO().updateHabit(habits[0]);
            _habit.postValue(habits[0]);
            return null;
        }
    }

    /**
     * Async task to update the history of the selected habit
     */
    private class UpdateHistoryAsyncTask extends AsyncTask<Integer, Void, Void> {
        private SlotBitsDatabase _db;
        private MutableLiveData<HabitHistory> _habitHistory;

        UpdateHistoryAsyncTask(SlotBitsDatabase db, MutableLiveData<HabitHistory> habitHistory) {
            _db = db;
            _habitHistory = habitHistory;
        }

        @Override
        public Void doInBackground(final Integer... habitIds) {
            _habitHistory.postValue(
                    new HabitHistory(habitIds[0], _db.slotDAO().getHabitHistory(habitIds[0])));
            return null;
        }
    }
}
