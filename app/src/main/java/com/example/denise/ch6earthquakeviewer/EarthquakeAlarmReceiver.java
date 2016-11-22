/**
 * Denise Bradley
 * CS352 Advanced Android
 * Chapter 7 Earthquake Viewer
 * 11/21/16.
 */

package com.example.denise.ch6earthquakeviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EarthquakeAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_REFRESH_EARTHQUAKE_ALARM =
            "com.example.denise.earthquake.ACTION_REFRESH_EARTHQUAKE_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startIntent = new Intent(context, EarthquakeUpdateService.class);
        context.startService(startIntent);
    }

}