package com.quang.timeslots;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.quang.timeslots.common.FileIO;
import com.quang.timeslots.common.HabitTimer;
import com.quang.timeslots.common.NotificationBroadcastReceiver;
import com.quang.timeslots.db.Habit;
import com.quang.timeslots.db.TimeSlotsDatabase;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Application singleton providing some global functionality
 */
public class TimeSlotsApplication extends Application implements Application.ActivityLifecycleCallbacks {
    public final static String NOTIFICATION_CHANNEL_ID = "timeslots_channel_001";
    public final static String NOTIFICATION_CHANNEL_DESCRIPTION = "TimeSlots Notifications";
    private final static int NOTIFICATION_ID = 111;
    private final static String SHARED_PREFERENCES_ID = "com.quang.timeslots.sharedpreferences";
    private final static String SHARED_PREFERENCES_DATA_FILE_NAME = "DATA_FILE_NAME";

    private static TimeSlotsApplication _instance;
    private TimeSlotsDatabase _db;
    private SharedPreferences _sharedPrefs;
    private Vibrator _vibrator;
    private Activity _visibleActivity;
    private AlertDialog _completionDialog;
    private NotificationManager _notificationManager;

    public boolean isDevMode = false; /** Toggleable dev mode (fixed 5s timer) */

    /**
     * Called when the application is first started
     */
    @Override
    public void onCreate() {
        super.onCreate();

        _instance = this;
        _db = Room.databaseBuilder(getApplicationContext(), TimeSlotsDatabase.class, "TimeSlots")
                .build();
        _sharedPrefs = getSharedPreferences(SHARED_PREFERENCES_ID, MODE_PRIVATE);
        _vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        HabitTimer.getInstance();
        FileIO.getInstance();
        registerActivityLifecycleCallbacks(this); // Listen to activity state changes
        JodaTimeAndroid.init(this);

        // Initialize notification manager with channel
        _notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_DESCRIPTION,
                    NotificationManager.IMPORTANCE_DEFAULT);
            _notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override
    public void onActivityDestroyed(Activity activity) {}
    @Override
    public void onActivityPaused(Activity activity) { _visibleActivity = null; }
    @Override
    public void onActivityResumed(Activity activity) { _visibleActivity = activity; }
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override
    public void onActivityStarted(Activity activity) {}
    @Override
    public void onActivityStopped(Activity activity) {}

    /**
     * Get the singleton object
     * @return
     */
    public static TimeSlotsApplication getInstance() {
        return _instance;
    }

    /**
     * Get application's database
     * @return
     */
    public TimeSlotsDatabase getDB() {
        return _db;
    }

    /**
     * Get application's notification manager
     * @return
     */
    public NotificationManager getNotificationManager() {
        return _notificationManager;
    }

    /**
     * Save selected data file to shared prefs
     * @param uri
     */
    public void saveDataFileUri(Uri uri) {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.putString(SHARED_PREFERENCES_DATA_FILE_NAME, uri.toString());
        editor.commit();
    }

    /**
     * Retrieve selected data file URI from shared prefs
     * @return
     */
    public Uri getDataFileUri() {
        String uri = _sharedPrefs.getString(SHARED_PREFERENCES_DATA_FILE_NAME, "");
        return uri.equals("") ? null : Uri.parse(uri);
    }

    /**
     * Delete selected data file from shared prefs
     */
    public void deleteDataFileUri() {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.remove(SHARED_PREFERENCES_DATA_FILE_NAME);
        editor.commit();
    }

    /**
     * Start vibrator on slot completion
     */
    public void startVibrator() {
        _vibrator.vibrate(new long[] {0, 500, 1500}, 0);
    }

    /**
     * Cancel vibrator if it's running after slot completion
     */
    public void cancelVibrator() {
        _vibrator.cancel();
    }

    /**
     * Show notification when a slot is completed
     */
    public void showCompletionNotification(Habit runningHabit) {
        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.putExtra("selectedHabit", runningHabit);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        String notificationBody = getResources().getString(R.string.notification_completion_body, runningHabit.getName());
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.icon_focus)
                        .setContentTitle(getResources().getString(R.string.notification_completion_title))
                        .setContentText(notificationBody)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        _notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Show popup dialog on top of current activity when slot is completed
     * @param habit - Habit for which slot was completed
     */
    public void showCompletionDialog(Habit habit) {
        if (_visibleActivity == null)
            return;

        if (_completionDialog == null) {
            _completionDialog = new AlertDialog.Builder(_visibleActivity)
                    .create();
            _completionDialog.setTitle(getString(R.string.title_slot_completed_dialog));
            _completionDialog.setMessage(getString(R.string.text_slot_completed, habit.getName()));
            _completionDialog.setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    getString(R.string.button_dismiss),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelVibrator();
                            _completionDialog = null;
                        }
                    });
            _completionDialog.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    getString(R.string.button_repeat),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            HabitTimer.getInstance().restartCountdown();
                            _completionDialog = null;
                        }
                    });
            _completionDialog.show();
        }
    }
}
