/**
 * Denise Bradley
 * CS352 Advanced Android
 * Chapter 6 Earthquake Viewer
 * 11/15/16.
 */

package com.example.denise.ch6earthquakeviewer;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

public class Earthquake extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);
    }
}
