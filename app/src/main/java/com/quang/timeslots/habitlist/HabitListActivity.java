package com.quang.timeslots.habitlist;

import android.app.DialogFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;

import com.quang.timeslots.db.Habit;
import com.quang.timeslots.habitdetails.HabitDetailsActivity;
import com.quang.timeslots.R;
import com.quang.timeslots.common.HabitEditDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class HabitListActivity extends AppCompatActivity
                                implements HabitEditDialogFragment.HabitEditDialogListener {
    private HabitListAdapter _habitListAdapter;
    private HabitListViewModel _viewModel;

    private Observer<List<Habit>> _habitListObserver = new Observer<List<Habit>>() {
        @Override
        public void onChanged(@Nullable List<Habit> habits) {
            _habitListAdapter.clear();
            _habitListAdapter.addAll(habits);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit_list);

        ListView habitListView = findViewById(R.id.habit_list);
        _habitListAdapter = new HabitListAdapter(this, habitListView, new ArrayList<Habit>());
        habitListView.setAdapter(_habitListAdapter);
        habitListView.setOnItemClickListener(new HabitOnClickListener());

        _viewModel = ViewModelProviders.of(this).get(HabitListViewModel.class);
        _viewModel.getHabitList().observe(this, _habitListObserver);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        FloatingActionButton fab = findViewById(R.id.create_habit_button);
        fab.setOnClickListener(new FABOnClickListener());
    }

    private class HabitOnClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent habitDetailsIntent = new Intent(HabitListActivity.this, HabitDetailsActivity.class);
            habitDetailsIntent.putExtra("selectedHabit", (Habit) view.getTag());
            startActivity(habitDetailsIntent);
        }
    }

    private class FABOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            DialogFragment editDialog = HabitEditDialogFragment.newInstance(new Habit("",15));
            editDialog.show(getFragmentManager(), "HabitEditDialogFragment");
        }
    }

    @Override
    public void onEditDialogPositiveClick(Habit habit) {
        _viewModel.addHabit(habit);
    }
}
