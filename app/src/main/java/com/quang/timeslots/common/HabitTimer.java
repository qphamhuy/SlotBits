package com.quang.timeslots.common;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;

import com.quang.timeslots.R;
import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.db.Habit;
import com.quang.timeslots.db.Slot;

import org.joda.time.DateTime;

/**
 * Global singleton that handles the countdown timer for a running habit
 */
public class HabitTimer {
    private final static int NOTIFICATION_ID = 112;

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
     * Format the remaining seconds as ##m##s
     * @param remainingSecs
     * @return
     */
    public static String formatRemainingSecs(long remainingSecs) {
        long mins = remainingSecs / 60;
        long secs = remainingSecs % 60;
        return String.format("%dm%ds", mins, secs);
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
        TimeSlotsApplication.getInstance().getNotificationManager().cancel(NOTIFICATION_ID);
    }

    /**
     * Restart countdown for the same habit that just finished
     */
    public void restartCountdown() {
        if (_habitTimerListener != null)
            _habitTimerListener.onTimerRestart();
    }


    //////////


    /**
     * Custom implementation of CountDownTimer
     */
    private class HabitCountdown extends CountDownTimer {
        private final TimeSlotsApplication _app = TimeSlotsApplication.getInstance();
        private NotificationCompat.Builder _notificationBuilder;


        HabitCountdown(long millisInFuture) {
            super(millisInFuture, 1000);

            _notificationBuilder = new NotificationCompat.Builder(_app, TimeSlotsApplication.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_focus)
                .setContentTitle(_app.getResources().getString(R.string.notification_ongoing_title, _runningHabit.getName()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setVibrate(new long[]{0, 0})
                .setSound(null)
                .setOnlyAlertOnce(true);
        }

        @Override
        public void onTick(long millisRemaining) {
            if (_habitTimerListener != null)
                _habitTimerListener.timerUpdate(millisRemaining/1000);
            _updateTimerNotification(millisRemaining/1000);
        }

        @Override
        public void onFinish() {
            _app.showCompletionDialog(_runningHabit);
            _app.showCompletionNotification(_runningHabit);
            _app.startVibrator();
            new TimerFinishAsyncTask().execute(_runningHabit.id); // Write slot to DB
            stopCountdown();
        }

        private void _updateTimerNotification(long remainingSecs) {
            _notificationBuilder.setContentText(HabitTimer.formatRemainingSecs(remainingSecs));
            _app.getNotificationManager().notify(NOTIFICATION_ID, _notificationBuilder.build());
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
