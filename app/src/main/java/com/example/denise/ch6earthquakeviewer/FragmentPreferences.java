/**
 * Denise Bradley
 * CS352 Advanced Android
 * Chapter 7 Earthquake Viewer
 * 11/21/16.
 */

package com.example.denise.ch6earthquakeviewer;

import java.util.List;

import android.preference.PreferenceActivity;

public class FragmentPreferences extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }
}