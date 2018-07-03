package com.quang.timeslots.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.habitdetails.HabitDetailsActivity;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        TimeSlotsApplication app = TimeSlotsApplication.getInstance();
        app.cancelVibrator();
        Intent activityIntent = new Intent(app, HabitDetailsActivity.class);
        activityIntent.putExtra("selectedHabit", intent.getParcelableExtra("selectedHabit"));
        app.startActivity(activityIntent);
    }
}
