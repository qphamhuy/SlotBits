package com.quang.timeslots.common;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.CountDownTimer;

import com.quang.timeslots.R;
import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.db.Habit;
import com.quang.timeslots.db.Slot;

import org.joda.time.DateTime;

/**
 * Global singleton that handles the countdown timer for a running habit
 */
public class HabitTimer {
    private Habit _runningHabit = null;
    private HabitTimerListener _habitTimerListener;
    private CountDownTimer _countdownTimer;
    private static HabitTimer _instance;

    /**
     * Singleton method to return the instance
     * @return Singleton instance
     */
    public static HabitTimer getInstance() {
        if (_instance == null)
            _instance = new HabitTimer();
        return _instance;
    }

    /**
     * Get ID of currently running habit
     * @return ID of running habit
     */
    public int getRunningHabitId() { return (_runningHabit != null ? _runningHabit.id : -1); }

    /**
     * Subscribe a listener that reacts to timer ticks
     * @param listener - Listener to subscribe
     */
    public void subscribeListener(HabitTimerListener listener) {
        _habitTimerListener = listener;
    }

    /**
     * Start a new countdown
     * @param habit - Habit to start countdown for
     */
    public void startCountdown(Habit habit) {
        stopCountdown();
        _runningHabit = habit;
        int seconds = (TimeSlotsApplication.getInstance().isDevMode ? 5 : habit.getSlotLength() * 60);
        _countdownTimer = new HabitCountdown(seconds * 1000).start();
    }

    /**
     * Stop current countdown
     */
    public void stopCountdown() {
        _runningHabit = null;
        if (_countdownTimer != null) {
            _countdownTimer.cancel();
            _countdownTimer = null;
        }
    }


    //////////


    /**
     * Custom implementation of CountDownTimer
     */
    private class HabitCountdown extends CountDownTimer {
        HabitCountdown(long millisInFuture) {
            super(millisInFuture, 1000);
        }

        @Override
        public void onTick(long millisRemaining) {
            if (_habitTimerListener != null)
                _habitTimerListener.timerUpdate(millisRemaining/1000);
        }

        @Override
        public void onFinish() {
            final TimeSlotsApplication app = TimeSlotsApplication.getInstance();

            //Show popup dialog
            AlertDialog dialog = new AlertDialog.Builder(_habitTimerListener.getActivity())
                    .create();
            dialog.setTitle(app.getString(R.string.title_slot_completed_dialog));
            dialog.setMessage(app.getString(R.string.text_slot_completed, _runningHabit.getName()));
            dialog.setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    app.getString(R.string.button_dismiss),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            app.cancelVibrator();
                        }
                    });
            dialog.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    app.getString(R.string.button_repeat),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            _habitTimerListener.onTimerRestart();
                        }
                    });
            dialog.show();

            app.showCompletionNotification(_runningHabit);
            app.startVibrator();
            new TimerFinishAsyncTask().execute(_runningHabit.id);
            stopCountdown();
        }
    }

    /**
     * Async task to execute operations on completion of timer (save to db)
     */
    private class TimerFinishAsyncTask extends AsyncTask<Integer, Void, Void> {
        @Override
        public Void doInBackground(final Integer... habitIDs) {
            TimeSlotsApplication.getInstance().getDB().slotDAO().createSlot(
                    new Slot(habitIDs[0], new DateTime()));
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            _habitTimerListener.onTimerFinish();
        }
    }
}
