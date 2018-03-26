package com.quang.timeslots.common;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.CountDownTimer;

import com.quang.timeslots.R;
import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.db.Slot;

import org.joda.time.DateTime;

/**
 * Global singleton that handles the countdown timer for a running habit
 */
public class HabitTimer {
    private int _runningHabitId = -1;
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
    public int getRunningHabitId() { return _runningHabitId; }

    /**
     * Subscribe a listener that reacts to timer ticks
     * @param listener - Listener to subscribe
     */
    public void subscribeListener(HabitTimerListener listener) {
        _habitTimerListener = listener;
    }

    /**
     * Start a new countdown
     * @param habitId - ID of habit to start countdown for
     * @param duration - Duration of given habit, in minutes
     */
    public void startCountdown(int habitId, int duration) {
        stopCountdown();
        _runningHabitId = habitId;
        _countdownTimer = new HabitCountdown(duration * 60 * 1000).start();
    }

    /**
     * Stop current countdown
     */
    public void stopCountdown() {
        _runningHabitId = -1;
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
            TimeSlotsApplication app = TimeSlotsApplication.getInstance();
            final int tempHabitId = _runningHabitId;

            //Show popup dialog
            AlertDialog dialog = new AlertDialog.Builder(_habitTimerListener.getActivity())
                    .create();
            dialog.setTitle(app.getString(R.string.title_slot_completed_dialog));
            dialog.setMessage(app.getString(R.string.message_slot_completed));
            dialog.setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    app.getString(R.string.button_dismiss),
                    (DialogInterface.OnClickListener) null);
            dialog.show();

            //Show notification
//            NotificationCompat.Builder notifBuilder =
//                    new NotificationCompat.Builder(app, app.getString(R.string.app_name))
//                    .setSmallIcon(R.drawable.icon_focus)
//                    .setContentTitle("Task completed")
//                    .setContentText("Hooray!")
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//            NotificationManagerCompat notifManager = NotificationManagerCompat.from(app);
//            notifManager.notify(0, notifBuilder.build());

            new TimerFinishAsyncTask().execute(_runningHabitId);

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
