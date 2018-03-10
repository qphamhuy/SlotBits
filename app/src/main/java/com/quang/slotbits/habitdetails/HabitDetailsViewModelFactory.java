package com.quang.slotbits.habitdetails;

import android.arch.lifecycle.ViewModelProvider;

import com.quang.slotbits.db.Habit;
import com.quang.slotbits.habitlist.HabitListViewModel;

public class HabitDetailsViewModelFactory implements ViewModelProvider.Factory {
    private final Habit _habit;

    public HabitDetailsViewModelFactory(Habit habit) {
        _habit = habit;
    }

    @Override
    public HabitDetailsViewModel create(Class modelClass) {
        return new HabitDetailsViewModel(_habit);
    }
}
