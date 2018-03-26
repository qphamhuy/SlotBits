package com.quang.timeslots.habitdetails;

import android.arch.lifecycle.ViewModelProvider;

import com.quang.timeslots.db.Habit;

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
