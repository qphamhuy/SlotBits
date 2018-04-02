package com.quang.timeslots.habitlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.quang.timeslots.R;
import com.quang.timeslots.common.HabitTimer;
import com.quang.timeslots.common.HabitTimerListener;
import com.quang.timeslots.db.Habit;
import com.quang.timeslots.habitdetails.HabitDetailsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for the list of habits displayed in the main activity
 */
public class HabitListAdapter
        extends RecyclerView.Adapter<HabitListAdapter.HabitItemViewHolder>
        implements HabitTimerListener {
    private HabitListActivity _context;
    private Resources _resources;
    private List<Habit> _habitList;
    private HabitListAdapter.HabitItemViewHolder _runningHabitViewHolder;
    private HabitListAdapter.HabitItemViewHolder _lastCompletedHabitViewHolder;
    private LayoutInflater _layoutInflater;

    /**
     * Constructor
     * @param context - Host activity
     * @param habits - List of Habit objects
     */
    public HabitListAdapter(HabitListActivity context, ArrayList<Habit> habits) {
        _context = context;
        _resources = _context.getResources();
        _habitList = habits;
        _layoutInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Get number of items in the list; overriding for RecyclerView.Adapter
     * @return
     */
    @Override
    public int getItemCount() { return _habitList.size(); }

    /**
     * Callback to create item holder; overriding for RecyclerView.Adapter
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public HabitItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItemView = _layoutInflater.inflate(R.layout.habit_list_item, parent, false);
        return new HabitItemViewHolder(listItemView);
    }

    /**
     * Callback to bind item holder with data; overriding for RecyclerView.Adapter
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final HabitItemViewHolder holder, int position) {
        final HabitTimer habitTimer = HabitTimer.getInstance();
        final Habit habit = _habitList.get(position);
        if (habit.id == habitTimer.getRunningHabitId())
            _runningHabitViewHolder = holder;

        holder.nameView.setText(habit.getName());
        holder.detailsView.setText(_resources.getString(R.string.text_habit_list_item_details, habit.getSlotLength()));

        holder.button.setVisibility(View.VISIBLE);
        if (habit.id == habitTimer.getRunningHabitId()) { // This is the running habit
            holder.nameView.setTextColor(_resources.getColor(R.color.colorPrimaryDark));
            holder.nameView.setTypeface(null, Typeface.BOLD);
            holder.detailsView.setTextColor(_resources.getColor(R.color.colorPrimary));
            holder.button.setImageResource(R.drawable.icon_stop);
            holder.countdownView.setVisibility(View.VISIBLE);
        }
        else {
            holder.nameView.setTypeface(null, Typeface.NORMAL);
            holder.button.setImageResource(R.drawable.icon_play);
            holder.countdownView.setVisibility(View.INVISIBLE);
            int textColor = _resources.getColor(R.color.textColorPrimary);
            if (habitTimer.getRunningHabitId() != -1) { // Some other habit is running
                textColor = _resources.getColor(R.color.textDisabled);
                holder.button.setVisibility(View.INVISIBLE);
            }
            holder.nameView.setTextColor(textColor);
            holder.detailsView.setTextColor(textColor);
        }

        //Listen to click on habit list item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent habitDetailsIntent = new Intent(_context, HabitDetailsActivity.class);
                habitDetailsIntent.putExtra("selectedHabit", habit);
                _context.startActivity(habitDetailsIntent);
            }
        });

        //Listen to click on the start/stop button
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (habitTimer.getRunningHabitId() == -1) {
                    habitTimer.startCountdown(habit);
                    _lastCompletedHabitViewHolder = null;
                }
                else {
                    habitTimer.stopCountdown();
                }
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Callback for a HabitTimer's tick; implementation of HabitTimerListener interface
     * @param remainingSecs - Remaining seconds in the timer
     */
    @Override
    public void timerUpdate(long remainingSecs) {
        if (_runningHabitViewHolder != null) {
            long mins = remainingSecs / 60;
            long secs = remainingSecs % 60;
            _runningHabitViewHolder.countdownView.setText(String.format("%dm%ds", mins, secs));
        }
    }

    /**
     * Callback for HabitTimer's completion; implementation of HabitTimerListener interface
     */
    @Override
    public void onTimerFinish() {
        notifyDataSetChanged();
        _lastCompletedHabitViewHolder = _runningHabitViewHolder;
    }

    /**
     * Get the host activity; implementation of HabitTimerListener interface
     * @return
     */
    @Override
    public Activity getActivity() {
        return (Activity) _context;
    }

    /**
     * Callback for HabitTimer's request to restart; implementation of HabitTimerListener interface
     */
    @Override
    public void onTimerRestart() {
        _lastCompletedHabitViewHolder.button.callOnClick();
    }

    /**
     * Set list of habits to new list
     * @param habits - New list of habits
     */
    public void setHabitList(List<Habit> habits) {
        _habitList = habits;
        notifyDataSetChanged();
    }

    /**
     * Listen to habit item's move event, triggered by user's drag
     * @param fromPosition - Old position of item
     * @param toPosition - New position of item
     */
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(_habitList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    /**
     * Listen to habit's move completion, after user has dropped it
     * @param viewHolder - View holder of the moved item
     */
    public void onItemMoveCompleted(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(0);
        _context.onHabitsReorder(_habitList);
    }


    //////////


    /**
     * Item view holder
     */
    public static class HabitItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameView;
        public final TextView detailsView;
        public final TextView countdownView;
        public final ImageButton button;

        HabitItemViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.habit_list_item_name);
            detailsView = itemView.findViewById(R.id.habit_list_item_details);
            countdownView = itemView.findViewById(R.id.habit_list_item_countdown);
            button = itemView.findViewById(R.id.habit_list_item_button);
        }
    }
}
