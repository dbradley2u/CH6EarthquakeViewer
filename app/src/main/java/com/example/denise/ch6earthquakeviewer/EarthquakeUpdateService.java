/**
 * Denise Bradley
 * CS352 Advanced Android
 * Chapter 7 Earthquake Viewer
 * 11/21/16.
 */

package com.example.denise.ch6earthquakeviewer;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class EarthquakeUpdateService extends IntentService {

    public static String TAG = "EARTHQUAKE_UPDATE_SERVICE";

    public EarthquakeUpdateService() {
        super("EarthquakeUpdateService");
    }

    public EarthquakeUpdateService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        refreshEarthquakes();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addNewQuake(Quake quake) {
        ContentResolver cr = getContentResolver();

        // Construct a where clause to make sure we don't already have this
        // earthquake in the provider.
        String w = EarthquakeProvider.KEY_DATE + " = " + quake.getDate().getTime();

        // If the earthquake is new, insert it into the provider.
        Cursor query = cr.query(EarthquakeProvider.CONTENT_URI, null, w, null, null);

        if (query.getCount()=0) {
            ContentValues values = new ContentValues();

            values.put(EarthquakeProvider.KEY_DATE, quake.getDate().getTime());
            values.put(EarthquakeProvider.KEY_DETAILS, quake.getDetails());
            values.put(EarthquakeProvider.KEY_SUMMARY, quake.toString());

            double lat = quake.getLocation().getLatitude();
            double lng = quake.getLocation().getLongitude();
            values.put(EarthquakeProvider.KEY_LOCATION_LAT, lat);
            values.put(EarthquakeProvider.KEY_LOCATION_LNG, lng);
            values.put(EarthquakeProvider.KEY_LINK, quake.getLink());
            values.put(EarthquakeProvider.KEY_MAGNITUDE, quake.getMagnitude());

            cr.insert(EarthquakeProvider.CONTENT_URI, values);
        }
        query.close();
    }

    public void refreshEarthquakes() {
        // Get the XML
        URL url;
        try {
            //String quakeFeed = getString(R.string.quake_feed);
            String quakeFeed = getString(R.string.quake_feed);
            url = new URL(quakeFeed);

            URLConnection connection;
            connection = url.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection)connection;
            int responseCode = httpConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = httpConnection.getInputStream();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                // Parse the earthquake feed.
                Document dom = db.parse(in);
                Element docEle = dom.getDocumentElement();

                // Get a list of each earthquake entry.
                NodeList nl = docEle.getElementsByTagName("entry");
                if (nl != null && nl.getLength() > 0) {
                    for (int i = 0 ; i < nl.getLength(); i++) {
                        Element entry = (Element)nl.item(i);
                        Element title = (Element)entry.getElementsByTagName("title").item(0);
                        Element g = (Element)entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element)entry.getElementsByTagName("updated").item(0);
                        Element link = (Element)entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        String hostname = "http://earthquake.usgs.gov";
                        String linkString = hostname + link.getAttribute("href");

                        String point = g.getFirstChild().getNodeValue();
                        String dt = when.getFirstChild().getNodeValue().replaceFirst("Z", "+0000");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        Date qdate = new GregorianCalendar(0,0,0).getTime();
                        try {
                            qdate = sdf.parse(dt);
                        } catch (ParseException e) {
                            //Log.d(TAG, "Date parsing exception.", e);
                            Log.d(TAG, "Date parsing exception.", e);
                        }

                        String[] location = point.split(" ");
                        Location l = new Location("dummyGPS");
                        l.setLatitude(Double.parseDouble(location[0]));
                        l.setLongitude(Double.parseDouble(location[1]));

                        String magnitudeString = details.split(" ")[1];
                        int end =  magnitudeString.length();
                        double magnitude = Double.parseDouble(magnitudeString.substring(0, end));

                        if(details.contains(","))
                        {
                            details = details.split(",")[1].trim();
                        }
                        else{
                            details = details.split("-")[1].trim();
                        }

                        Quake quake = new Quake(qdate, details, l, magnitude, linkString);

                        // Process a newly found earthquake
                        addNewQuake(quake);
                    }
                }
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed URL Exception", e);
        } catch (IOException e) {
            Log.e(TAG, "IO Exception", e);
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "Parser Configuration Exception", e);
        } catch (SAXException e) {
            Log.e(TAG, "SAX Exception", e);
        }
        finally {
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Retrieve the shared preferences
        Context context = getApplicationContext();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);

        int updateFreq =
                Integer.parseInt(prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "60"));
        boolean autoUpdateChecked =
                prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);

        if (autoUpdateChecked) {
            int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
            long timeToRefresh = SystemClock.elapsedRealtime() +
                    updateFreq*60*1000;
            alarmManager.setInexactRepeating(alarmType, timeToRefresh,
                    updateFreq*60*1000, alarmIntent);
        }
        else
            alarmManager.cancel(alarmIntent);

        return super.onStartCommand(intent, flags, startId);
    };


    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        String ALARM_ACTION =
                EarthquakeAlarmReceiver.ACTION_REFRESH_EARTHQUAKE_ALARM;
        Intent intentToFire = new Intent(ALARM_ACTION);
        alarmIntent =
                PendingIntent.getBroadcast(this, 0, intentToFire, 0);
    }

}