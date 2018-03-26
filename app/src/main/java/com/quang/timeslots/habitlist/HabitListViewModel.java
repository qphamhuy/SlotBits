package com.quang.timeslots.habitlist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;

import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.db.Habit;
import com.quang.timeslots.db.TimeSlotsDatabase;

import java.util.List;

/**
 * View model for HabitListActivity
 */
public class HabitListViewModel extends ViewModel {
    private LiveData<List<Habit>> _habitList;
    private TimeSlotsDatabase _db;

    /**
     * Constructor
     */
    public HabitListViewModel() {
        _db = TimeSlotsApplication.getInstance().getDB();
        _habitList = _db.habitDAO().getAllHabits();
    }

    /**
     * Get list of Habit objects
     * @return Livedata with list of habits
     */
    public LiveData<List<Habit>> getHabitList() {
        return _habitList;
    }

    /**
     * Add habit to the list
     * @param habit - New Habit object
     */
    public void addHabit(Habit habit) {
        new AddAsyncTask(_db).execute(habit);
    }


    //////////


    /**
     * AsyncTask to add a new Habit to the list
     */
    private static class AddAsyncTask extends AsyncTask<Habit, Void, Void> {
        TimeSlotsDatabase _db;

        AddAsyncTask(TimeSlotsDatabase db) {
            _db = db;
        }
        @Override
        public Void doInBackground(final Habit... habits) {
            _db.habitDAO().createHabit(habits[0]);
            return null;
        }
    }
}
