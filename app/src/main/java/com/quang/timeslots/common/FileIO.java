package com.quang.timeslots.common;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.R;
import com.quang.timeslots.db.Habit;
import com.quang.timeslots.db.Slot;
import com.quang.timeslots.db.TimeSlotsDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Singleton class with functionality to read/write data between a file on disk and app's DB
 */
public class FileIO {
    private static FileIO _instance;

    /**
     * Interface for listener to implement callback on completion of a file write
     */
    public interface FileWriteListener {
        void onFileWriteComplete();
    }

    /**
     * Interface for listener to implement callback on completion of a file read
     */
    public interface FileReadListener {
        void onFileReadComplete();
    }

    /**
     * Singleton method to return the instance
     * @return Singleton instance
     */
    public static FileIO getInstance() {
        if (_instance == null)
            _instance = new FileIO();
        return _instance;
    }

    /**
     * Read data from a file
     * @param uri - URI of given data file
     * @param listener - Listener with callback for the completion of data read
     */
    public void readFromFile(Uri uri, FileReadListener listener) {
        new ReadFromFileAsyncTask(listener).execute(uri);
    }

    /**
     * Write data to a file
     * @param uri - URI of given data file
     * @param listener - Listener with callback for the completion of data write
     */
    public void writeToFile(Uri uri, FileWriteListener listener) {
        new WriteToFileAsyncTask(listener).execute(uri);
    }


    //////////


    private final static String LOG_TAG = FileIO.class.getName();
    private final static String JSON_FIELD_HABITS = "habits";
    private final static String JSON_FIELD_SLOTS = "slots";

    private class HabitMap extends HashMap<Integer, Habit> {}
    private class SlotSet extends HashSet<Slot> {}

    /**
     * Parse JSON in data file into the map of habits and set of slots
     * @param uri - URI of given data file
     * @param habitMap - Map of habits to write into
     * @param slotSet - Set of slots to write into
     */
    private void parseDataFile(Uri uri, HabitMap habitMap, SlotSet slotSet) {
        TimeSlotsApplication app = TimeSlotsApplication.getInstance();

        try {
            //Read data into a JSONObject
            ParcelFileDescriptor pfd = app.getContentResolver().openFileDescriptor(uri, "r");
            FileInputStream inputStream = new FileInputStream(pfd.getFileDescriptor());
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            JSONObject jsonObject = new JSONObject(new String(buffer, "UTF-8"));

            //Write habits into map
            if (jsonObject.has(JSON_FIELD_HABITS)) {
                JSONArray jsonArray = jsonObject.getJSONArray(JSON_FIELD_HABITS);
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Habit habit = new Habit(jsonArray.getJSONObject(i));
                    habitMap.put(habit.id, habit);
                }
            }

            //Write slots into set
            if (jsonObject.has(JSON_FIELD_SLOTS)) {
                JSONArray jsonArray = jsonObject.getJSONArray(JSON_FIELD_SLOTS);
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Slot slot = new Slot(jsonArray.getJSONObject(i));
                    if (habitMap.containsKey(slot.getHabitId()))
                        slotSet.add(slot);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, app.getString(R.string.log_error_parse_data, e.getMessage()));
        }
    }

    /**
     * Async task to write data from DB to file
     */
    private class WriteToFileAsyncTask extends AsyncTask<Uri, Void, Void> {
        private FileWriteListener _listener;

        public WriteToFileAsyncTask(FileWriteListener listener) {
            _listener = listener;
        }

        @Override
        public Void doInBackground(final Uri... uris) {
            final TimeSlotsApplication app = TimeSlotsApplication.getInstance();
            final TimeSlotsDatabase db = app.getDB();

            try {
                //Merge existing data in the file with data in DB, using
                //a map and a set
                HabitMap habits = new HabitMap();
                SlotSet slots = new SlotSet();
                parseDataFile(uris[0], habits, slots);
                List<Habit> dbHabits = db.habitDAO().getAllHabitsSync();
                if (dbHabits != null) {
                    for (Habit habit : dbHabits)
                        habits.put(habit.id, habit);
                }
                List<Slot> dbSlots = db.slotDAO().getAllSlots();
                slots.addAll(dbSlots);

                JSONObject jsonObject = new JSONObject();

                //Put all habits in the map into JSON
                jsonObject.put(JSON_FIELD_HABITS, new JSONArray());
                JSONArray habitsField = jsonObject.getJSONArray(JSON_FIELD_HABITS);
                for (Habit habit : habits.values()) {
                    habitsField.put(habit.toJSON());
                }

                //Put all slots in the set into JSON
                jsonObject.put(JSON_FIELD_SLOTS, new JSONArray());
                JSONArray slotsField = jsonObject.getJSONArray(JSON_FIELD_SLOTS);
                for (Slot slot : slots) {
                    slotsField.put(slot.toJSON());
                }

                //Write new JSON back into the data file
                ParcelFileDescriptor pfd = app.getContentResolver().openFileDescriptor(uris[0], "w");
                FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                fileOutputStream.write(jsonObject.toString().getBytes("UTF-8"));
                fileOutputStream.close();
                pfd.close();
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, app.getString(R.string.log_error_data_to_file, e.getMessage()));
            }

            return null;
        }

        @Override
        public void onPostExecute(Void results) {
            if (_listener != null) {
                _listener.onFileWriteComplete();
            }
        }
    }

    /**
     * Async task to read data from file to DB
     * (if DB is not empty, it's wiped first)
     */
    private class ReadFromFileAsyncTask extends AsyncTask<Uri, Void, Void> {
        private FileReadListener _listener;

        public ReadFromFileAsyncTask(FileReadListener listener) {
            _listener = listener;
        }

        @Override
        public Void doInBackground(final Uri... uris) {
            TimeSlotsApplication app = TimeSlotsApplication.getInstance();
            TimeSlotsDatabase db = app.getDB();

            try {
                // Wipe out existing data from DB before importing new data from file
                db.habitDAO().deleteAllHabits();
                db.slotDAO().deleteAllSlots();

                HabitMap habitMap = new HabitMap();
                SlotSet slotSet = new SlotSet();
                parseDataFile(uris[0], habitMap, slotSet);

                db.habitDAO().createHabits(new ArrayList<>(habitMap.values()));
                db.slotDAO().createSlots(new ArrayList<>(slotSet));
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, app.getString(R.string.log_error_data_to_file, e.getMessage()));
            }

            return null;
        }

        @Override
        public void onPostExecute(Void results) {
            if (_listener != null) {
                _listener.onFileReadComplete();
            }
        }
    }

}
