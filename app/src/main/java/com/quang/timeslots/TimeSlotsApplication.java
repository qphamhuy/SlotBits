package com.quang.timeslots;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
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
public class TimeSlotsApplication extends Application {
    private static TimeSlotsApplication _instance;
    private TimeSlotsDatabase _db;
    private SharedPreferences _sharedPrefs;
    private Vibrator _vibrator;

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
        _sharedPrefs = getSharedPreferences(getString(R.string.shared_preferences_id), MODE_PRIVATE);
        _vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        HabitTimer.getInstance();
        FileIO.getInstance();

        JodaTimeAndroid.init(this);
    }

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
     * Save selected data file to shared prefs
     * @param uri
     */
    public void saveDataFileUri(Uri uri) {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.putString(getString(R.string.shared_preferences_key_data_file_name), uri.toString());
        editor.commit();
    }

    /**
     * Retrieve selected data file URI from shared prefs
     * @return
     */
    public Uri getDataFileUri() {
        String uri = _sharedPrefs.getString(getString(R.string.shared_preferences_key_data_file_name), "");
        return uri.equals("") ? null : Uri.parse(uri);
    }

    /**
     * Delete selected data file from shared prefs
     */
    public void deleteDataFileUri() {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.remove(getString(R.string.shared_preferences_key_data_file_name));
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

        String channelID = getResources().getString(R.string.notification_channel_id);
        String notificationBody = getResources().getString(R.string.notification_body, runningHabit.getName());
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelID)
                        .setSmallIcon(R.drawable.icon_focus)
                        .setContentTitle(getResources().getString(R.string.notification_title))
                        .setContentText(notificationBody)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setChannelId(channelID);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelID,
                    getResources().getString(R.string.notification_channel_description),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
