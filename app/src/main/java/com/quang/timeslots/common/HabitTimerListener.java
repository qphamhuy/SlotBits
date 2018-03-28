package com.quang.timeslots.common;

import android.app.Activity;

/**
 * Listener that subscribes to a habit countdown timer
 */
public interface HabitTimerListener {
    /**
     * React to a timer's tick
     * @param remainingSecs - Remaining seconds in the timer
     */
    void timerUpdate(long remainingSecs);

    /**
     * React to timer's completion
     */
    void onTimerFinish();

    /**
     * Get the host activity
     * @return Activity object
     */
    Activity getActivity();

    /**
     * React to request to restart timer
     */
    void onTimerRestart();
}
