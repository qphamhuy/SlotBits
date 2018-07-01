package com.quang.timeslots;

import android.app.AlertDialog;
import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.SharedPreferences;
import android.net.Uri;

import com.quang.timeslots.common.FileIO;
import com.quang.timeslots.common.HabitTimer;
import com.quang.timeslots.db.TimeSlotsDatabase;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Application singleton providing some global functionality
 */
public class TimeSlotsApplication extends Application {
    private static TimeSlotsApplication _instance;
    private TimeSlotsDatabase _db;
    private SharedPreferences _sharedPrefs;

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
}
