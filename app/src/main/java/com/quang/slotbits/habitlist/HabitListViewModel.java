package com.quang.slotbits.habitlist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;

import com.quang.slotbits.SlotBitsApplication;
import com.quang.slotbits.db.Habit;
import com.quang.slotbits.db.SlotBitsDatabase;

import java.util.List;

/**
 * View model for HabitListActivity
 */
public class HabitListViewModel extends ViewModel {
    private LiveData<List<Habit>> _habitList;
    private SlotBitsDatabase _db;

    /**
     * Constructor
     */
    public HabitListViewModel() {
        _db = SlotBitsApplication.getInstance().getDB();
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
        SlotBitsDatabase _db;

        AddAsyncTask(SlotBitsDatabase db) {
            _db = db;
        }
        @Override
        public Void doInBackground(final Habit... habits) {
            _db.habitDAO().createHabit(habits[0]);
            return null;
        }
    }
}
