/**
 * Denise Bradley
 * CS352 Advanced Android
 * Chapter 7 Earthquake Viewer
 * 11/21/16.
 */

package com.example.denise.ch6earthquakeviewer;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public  class UserPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userpreferences);
    }

}
