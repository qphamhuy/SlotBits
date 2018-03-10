package com.quang.slotbits.habitdetails;

import android.app.Activity;
import android.app.DialogFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.quang.slotbits.common.HabitTimer;
import com.quang.slotbits.common.HabitTimerListener;
import com.quang.slotbits.R;
import com.quang.slotbits.common.HabitEditDialogFragment;
import com.quang.slotbits.db.Habit;
import com.quang.slotbits.db.Slot;
import com.quang.slotbits.habitlist.HabitListActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Activity showing details of a single habit
 */
public class HabitDetailsActivity extends AppCompatActivity
        implements HabitEditDialogFragment.HabitEditDialogListener,
        HabitDeleteDialogFragment.HabitDeleteDialogListener,
        HabitTimerListener {
    private HabitTimer _habitTimer;
    private Habit _selectedHabit;
    private HabitDetailsViewModel _viewModel;

    /**
     * Callback for when activity is created
     * @param savedInstanceState - Previous state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit_details);
        _habitTimer = HabitTimer.getInstance();
        _selectedHabit = this.getIntent().getParcelableExtra("selectedHabit");

        HabitDetailsViewModelFactory viewModelFactory = new HabitDetailsViewModelFactory(_selectedHabit);
        _viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HabitDetailsViewModel.class);
        _viewModel.getHabit().observe(this, new HabitObserver());
        _viewModel.getHabitHistory().observe(this, new HabitHistoryObserver());

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Callback for when activity is resumed
     */
    @Override
    public void onResume() {
        super.onResume();
        if (_selectedHabit.id == _habitTimer.getRunningHabitId()) {
            _habitTimer.subscribeListener(this);
            this.findViewById(R.id.habit_progress_card).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Callback to create the action bar menu
     * @param menu
     * @return True
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.habit_details_menu, menu);
        return true;
    }

    /**
     * Callback to react to a button press on the action bar
     * @param menuItem - One of the menu items on the action bar
     * @return True
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.habit_start_button:
                if (_selectedHabit.id != _habitTimer.getRunningHabitId()) {
                    _habitTimer.startCountdown(_selectedHabit.id, _selectedHabit.getSlotLength());
                    _habitTimer.subscribeListener(this);
                    this.findViewById(R.id.habit_progress_card).setVisibility(View.VISIBLE);
                }
                break;
            case R.id.habit_edit_button:
                DialogFragment editDialog = HabitEditDialogFragment.newInstance(_selectedHabit);
                editDialog.show(getFragmentManager(), "HabitEditDialogFragment");
                break;
            case R.id.habit_delete_button:
                DialogFragment deleteDialog = new HabitDeleteDialogFragment();
                deleteDialog.show(getFragmentManager(), "HabitDeleteDialogFragment");
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }

        return true;
    }

    /**
     * Callback for the positive button click in the dialog to edit the habit
     * @param habit - Updated habit
     */
    @Override
    public void onEditDialogPositiveClick(Habit habit) {
        habit.id = _selectedHabit.id;
        _viewModel.updateHabit(habit);
    }

    /**
     * Callback for the positive button click in the dialog to delete the habit
     */
    @Override
    public void onDeleteDialogPositiveClick() {
        _viewModel.deleteHabit();
    }

    /**
     * Callback for a HabitTimer's tick; implementation of HabitTimerListener interface
     * @param remainingSecs - Remaining seconds in the timer
     */
    @Override
    public void timerUpdate(long remainingSecs) {
        TextView countdownView = (TextView) this.findViewById(R.id.habit_status_remaining_time);
        long mins = remainingSecs / 60;
        long secs = remainingSecs % 60;
        countdownView.setText(String.format("%dm%ds", mins, secs));
    }

    /**
     * Callback for HabitTimer's completion; implementation of HabitTimerListener interface
     */
    @Override
    public void onTimerFinish() {
        findViewById(R.id.habit_progress_card).setVisibility(View.GONE);
        _viewModel.updateHabitHistory();
    }

    /**
     * Get the host activity; implementation of HabitTimerListener interface
     * @return
     */
    @Override
    public Activity getActivity() {
        return this;
    }

    /**
     * Callback for the click to stop the habit countdown
     * @param view - Button view
     */
    public void onStopButtonClick(View view) {
        _habitTimer.stopCountdown();
        this.findViewById(R.id.habit_progress_card).setVisibility(View.GONE);
    }


    //////////


    /**
     * Observer for the selected habit
     */
    private class HabitObserver implements Observer<Habit> {
        @Override
        public void onChanged(@Nullable Habit habit) {
            if (habit == null) {
                startActivity(new Intent(HabitDetailsActivity.this, HabitListActivity.class));
            }
            else {
                _selectedHabit = habit;
                ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle(_selectedHabit.getName());
                actionBar.setSubtitle(String.format("%d minutes", _selectedHabit.getSlotLength()));

                HabitDetailsActivity.this.onResume();
            }
        }
    }

    /**
     * Observer for the habit history
     */
    private class HabitHistoryObserver implements Observer<HabitHistory> {
        @Override
        public void onChanged(@Nullable HabitHistory habitHistory) {
            TextView historyText = HabitDetailsActivity.this.findViewById(R.id.habit_history_text);
            String s = "";
            s += String.format("Total number of slots of completed: %d", habitHistory.getNumOfSlots());
            s += "\nLast slot completed on ";
            s += (new SimpleDateFormat("MM/dd/yy HH:mm")).format(habitHistory.getLastCompleted());
            historyText.setText(s);

            HabitDetailsActivity.this.onResume();
        }
    };

}
