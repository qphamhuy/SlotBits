package com.quang.timeslots.common;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.quang.timeslots.R;
import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.db.Habit;
import com.quang.timeslots.db.Slot;
import com.quang.timeslots.habitdetails.HabitDetailsActivity;

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
     * @param duration - Duration of given habit, in minutes
     */
    public void startCountdown(Habit habit, int duration) {
        stopCountdown();
        _runningHabit = habit;
        int seconds = (TimeSlotsApplication.getInstance().isDevMode ? 5 : duration * 60);
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
     * Show notification when a slot is completed
     */
    private void _showNotification() {
        TimeSlotsApplication app = TimeSlotsApplication.getInstance();

        Intent intent = new Intent(app, HabitDetailsActivity.class);
        intent.putExtra("selectedHabit", _runningHabit);
        PendingIntent pendingIntent = PendingIntent.getActivity(app, 0, intent, 0);

        String channelID = app.getResources().getString(R.string.notification_channel_id);
        String notificationBody = String.format(
                app.getResources().getString(R.string.notification_body),
                _runningHabit.getName());
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(app, channelID)
                        .setSmallIcon(R.drawable.icon_focus)
                        .setContentTitle(
                                app.getResources().getString(R.string.notification_title))
                        .setContentText(notificationBody)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setChannelId(channelID);
        NotificationManager notificationManager =
                (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelID,
                    app.getResources().getString(R.string.notification_channel_description),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

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

            _showNotification();
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
