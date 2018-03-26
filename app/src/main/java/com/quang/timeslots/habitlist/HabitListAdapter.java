package com.quang.timeslots.habitlist;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.quang.timeslots.common.HabitTimer;
import com.quang.timeslots.common.HabitTimerListener;
import com.quang.timeslots.db.Habit;
import com.quang.timeslots.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the list of habits displayed in the main activity
 */
public class HabitListAdapter extends ArrayAdapter<Habit> implements HabitTimerListener {
    private Context _context;
    private Resources _resources;
    private List<Habit> _habitList;
    private ListView _listView;
    private View _runningHabitView;
    private LayoutInflater _layoutInflater;

    /**
     * Constructor
     * @param context - Host activity
     * @param listView - List view of the entire list
     * @param habits - List of Habit objects
     */
    public HabitListAdapter(Context context, ListView listView, ArrayList<Habit> habits) {
        super(context, 0, habits);
        _context = context;
        _resources = _context.getResources();
        _habitList = habits;
        _layoutInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _listView = listView;

        HabitTimer.getInstance().subscribeListener(this);
    }

    /**
     * Get view for a single list item
     * @param position - Item's position in the list
     * @param convertView
     * @param parent - Parent of item view
     * @return List item view
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final HabitTimer habitTimer = HabitTimer.getInstance();
        final Habit habit = _habitList.get(position);
        View listItemView = _layoutInflater.inflate(R.layout.habit_list_item, parent, false);
        if (habit.id == habitTimer.getRunningHabitId())
            _runningHabitView = listItemView;

        TextView habitCountdownView = listItemView.findViewById(R.id.habit_countdown);
        TextView habitNameView = listItemView.findViewById(R.id.habit_name);
        TextView habitDetailsView = listItemView.findViewById(R.id.habit_details);
        ImageButton habitButton = listItemView.findViewById(R.id.habit_button);

        habitNameView.setText(habit.getName());
        habitDetailsView.setText(String.format("Slot length: %d mins", habit.getSlotLength()));

        Typeface typeface = Typeface.createFromAsset(_context.getAssets(), "fonts/Roboto-Regular.ttf");
        habitNameView.setTypeface(typeface);
        habitDetailsView.setTypeface(typeface);

        if (habit.id == habitTimer.getRunningHabitId()) {
            habitNameView.setTextColor(_resources.getColor(R.color.colorPrimaryDark));
            habitNameView.setTypeface(typeface, Typeface.BOLD);
            habitDetailsView.setTextColor(_resources.getColor(R.color.colorPrimary));
            habitButton.setImageResource(R.drawable.icon_stop);
            habitCountdownView.setVisibility(View.VISIBLE);
        }
        else if (habitTimer.getRunningHabitId() != -1) {
            habitNameView.setTextColor(_resources.getColor(R.color.textDisabled));
            habitDetailsView.setTextColor(_resources.getColor(R.color.textDisabled));
            habitButton.setVisibility(View.INVISIBLE);
        }

        habitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (habitTimer.getRunningHabitId() == -1)
                    habitTimer.startCountdown(habit.id, habit.getSlotLength());
                else
                    habitTimer.stopCountdown();
                notifyDataSetChanged();
            }
        });

        listItemView.setTag(habit);
        return listItemView;
    }

    /**
     * Callback for a HabitTimer's tick; implementation of HabitTimerListener interface
     * @param remainingSecs - Remaining seconds in the timer
     */
    @Override
    public void timerUpdate(long remainingSecs) {
        if (_runningHabitView != null) {
            TextView countdownView = _runningHabitView.findViewById(R.id.habit_countdown);
            long mins = remainingSecs / 60;
            long secs = remainingSecs % 60;
            countdownView.setText(String.format("%dm%ds", mins, secs));
        }
    }

    /**
     * Callback for HabitTimer's completion; implementation of HabitTimerListener interface
     */
    @Override
    public void onTimerFinish() {
        notifyDataSetChanged();
    }

    /**
     * Get the host activity; implementation of HabitTimerListener interface
     * @return
     */
    @Override
    public Activity getActivity() {
        return (Activity) _context;
    }
}
