package com.quang.timeslots.habitdetails;

import android.app.Activity;
import android.app.DialogFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.quang.timeslots.R;
import com.quang.timeslots.common.HabitEditDialogFragment;
import com.quang.timeslots.common.HabitTimer;
import com.quang.timeslots.common.HabitTimerListener;
import com.quang.timeslots.db.Habit;
import com.quang.timeslots.habitlist.HabitListActivity;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity showing details of a single habit
 */
public class HabitDetailsActivity extends AppCompatActivity
        implements HabitEditDialogFragment.HabitEditDialogListener,
        HabitDeleteDialogFragment.HabitDeleteDialogListener,
        HabitTimerListener {

    private Resources _resources;
    private HabitTimer _habitTimer;
    private Habit _selectedHabit;
    private HabitDetailsViewModel _viewModel;
    private LineChart _historyChart;
    private String _inProgressText;
    private String _waitingText;
    private int _disabledTextColor;
    private int _primaryTextColor;

    /**
     * Callback for when activity is created
     * @param savedInstanceState - Previous state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit_details);
        _resources = getResources();
        _habitTimer = HabitTimer.getInstance();
        _selectedHabit = this.getIntent().getParcelableExtra("selectedHabit");

        HabitDetailsViewModelFactory viewModelFactory = new HabitDetailsViewModelFactory(_selectedHabit);
        _viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HabitDetailsViewModel.class);
        _viewModel.getHabit().observe(this, new HabitObserver());
        _viewModel.getHabitHistory().observe(this, new HabitHistoryObserver());

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _inProgressText = _resources.getString(R.string.text_habit_status_inprogress);
        _waitingText = _resources.getString(R.string.text_habit_status_waiting);
        _disabledTextColor = _resources.getColor(R.color.textDisabled);
        _primaryTextColor = _resources.getColor(R.color.textColorPrimary);

        //Initialize chart view to display habit history
        _historyChart = findViewById(R.id.habit_history_chart);
        _historyChart.getDescription().setEnabled(false);
        _historyChart.getLegend().setEnabled(false);
        _historyChart.setScaleYEnabled(false);
        _historyChart.setDoubleTapToZoomEnabled(false);
        _historyChart.getAxisLeft().setGranularity(1f);
        _historyChart.getAxisRight().setGranularity(1f);

        XAxis xAxis = _historyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
    }

    /**
     * Callback for when activity is resumed
     */
    @Override
    public void onResume() {
        super.onResume();
        if (_selectedHabit.id == _habitTimer.getRunningHabitId())
            _habitTimer.subscribeListener(this);
        _updateViewsWithHabitStatus();
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
        TextView countdownView = this.findViewById(R.id.habit_status_remaining_time);
        long mins = remainingSecs / 60;
        long secs = remainingSecs % 60;
        countdownView.setText(String.format("%dm%ds", mins, secs));
    }

    /**
     * Callback for HabitTimer's completion; implementation of HabitTimerListener interface
     */
    @Override
    public void onTimerFinish() {
        _viewModel.updateHabitHistory();
        _updateViewsWithHabitStatus();
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
     * Callback for HabitTimer's request to restart; implementation of HabitTimerListener interface
     */
    @Override
    public void onTimerRestart() {
        onStartButtonClick(findViewById(R.id.habit_details_start));
    }

    /**
     * Callback for the click to start the habit countdown
     * @param view - Button view
     */
    public void onStartButtonClick(View view) {
        _habitTimer.startCountdown(_selectedHabit);
        HabitDetailsActivity.this.onResume();
    }
    /**
     * Callback for the click to stop the habit countdown
     * @param view - Button view
     */
    public void onStopButtonClick(View view) {
        _habitTimer.stopCountdown();
        _updateViewsWithHabitStatus();
    }


    //////////


    private static final float MIN_X_RANGE = 3f;    /** Min scale that the history chart can be zoomed in to on the X-axis */
    private static final float MAX_X_RANGE = 20f;   /** Max scale that the history chart can be zoomed out to on the X-axis */
    private static final float MIN_Y_RANGE = 5f;    /** Min number of units that the Y-axis must have */

    /**
     * Update UI views to reflect current habit's status
     */
    private void _updateViewsWithHabitStatus() {
        if (_selectedHabit.id == _habitTimer.getRunningHabitId()) {
            findViewById(R.id.habit_status_progress).setVisibility(View.VISIBLE);
            ((Button) findViewById(R.id.habit_details_start)).setTextColor(_disabledTextColor);
            ((Button) findViewById(R.id.habit_details_stop)).setTextColor(_primaryTextColor);
            //Get time of completion to display in the status text
            DateTime completionTime = (new DateTime()).plusMinutes(_selectedHabit.getSlotLength());
            String statusText = _inProgressText + " " + completionTime.toString("hh:mmaa");
            ((TextView)findViewById(R.id.habit_status_text)).setText(statusText);
        }
        else {
            findViewById(R.id.habit_status_progress).setVisibility(View.INVISIBLE);
            ((Button) findViewById(R.id.habit_details_start)).setTextColor(_primaryTextColor);
            ((Button) findViewById(R.id.habit_details_stop)).setTextColor(_disabledTextColor);
            ((TextView) findViewById(R.id.habit_status_text)).setText(_waitingText);
        }
    }

    /**
     * Update the history chart with new data
     * @param habitHistory - New history data
     */
    private void _updateHistoryChart(HabitHistory habitHistory) {
        List<HabitHistory.DailyCount> dailyCounts = habitHistory.getDailyCounts();
        if (dailyCounts.size() == 0)
            return;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dailyCounts.size(); ++i) {
            entries.add(new BarEntry(i, dailyCounts.get(i).count));
        }
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setValueFormatter(new HistoryValueFormatter());
        dataSet.setValueTextSize(9f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(_resources.getColor(R.color.lightBackground));
        dataSet.setColor(_resources.getColor(R.color.lightBackground));
        dataSet.setCircleColor(_resources.getColor(R.color.colorPrimary));
        LineData data = new LineData(dataSet);

        _historyChart.setData(data);
        HistoryXAxisFormatter xAxisFormatter = new HistoryXAxisFormatter(dailyCounts.get(0).date);
        _historyChart.getXAxis().setValueFormatter(xAxisFormatter);
        _historyChart.setVisibleXRangeMinimum(MIN_X_RANGE);
        _historyChart.setVisibleXRangeMaximum(MAX_X_RANGE);
        _historyChart.setVisibleYRangeMinimum(MIN_Y_RANGE, YAxis.AxisDependency.LEFT);

        // Zoom in on the x-axis so that there are 5 bars displayed
        _historyChart.fitScreen();
        float xScale = (float)(entries.size() < MAX_X_RANGE ? entries.size() : MAX_X_RANGE)/5;
        _historyChart.zoom(xScale,1, 0, 0);
        _historyChart.moveViewToX(entries.size() - 5);
        _historyChart.invalidate();

        //Set custom marker view
        HistoryMarkerView markerView = new HistoryMarkerView(this, xAxisFormatter);
        markerView.setChartView(_historyChart);
        _historyChart.setMarker(markerView);
    }

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
     * Observer of the habit history to update the history chart
     */
    private class HabitHistoryObserver implements Observer<HabitHistory> {
        @Override
        public void onChanged(@Nullable HabitHistory habitHistory) {
            TextView historyText = HabitDetailsActivity.this.findViewById(R.id.habit_history_text);
            String s = String.format(_resources.getString(R.string.text_habit_history_total_count) + " %d", habitHistory.getTotalCount());
            historyText.setText(s);
            _updateHistoryChart(habitHistory);

            HabitDetailsActivity.this.onResume();
        }
    }

    /**
     * Formatter for labels on the X-axis of the history chart
     */
    private class HistoryXAxisFormatter implements IAxisValueFormatter {
        LocalDate _firstDate;

        HistoryXAxisFormatter(LocalDate firstDate) {
            _firstDate = firstDate;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            LocalDate currDate = _firstDate.plusDays((int) value);
            return currDate.toString("MM/dd");
        }

        public String getFullDate(float value) {
            LocalDate currDate = _firstDate.plusDays((int) value);
            return currDate.toString("MMM dd yyyy");
        }
    }

    /**
     * Formatter for values displayed on top of bars in the history chart
     */
    private class HistoryValueFormatter implements IValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return Integer.toString((int) value);
        }
    }

    /**
     * Custom marker view which is displayed when a value is highlighted
     */
    private class HistoryMarkerView extends MarkerView {
        private HistoryXAxisFormatter _xAxisFormatter;
        private TextView _markerViewTextView;

        HistoryMarkerView(Context context, HistoryXAxisFormatter xAxisFormatter) {
            super(context, R.layout.habit_history_chart_marker_view);
            _xAxisFormatter = xAxisFormatter;
            _markerViewTextView = findViewById(R.id.habit_history_marker_view_text);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            _markerViewTextView.setText(_xAxisFormatter.getFullDate(e.getX()));
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), 0);//-getHeight());
        }

    }
}
