package com.example.sl.photogallery.Service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.sl.photogallery.Model.FlickrFetcher;
import com.example.sl.photogallery.Model.GalleryItem;
import com.example.sl.photogallery.PhotoGalleryActivity;
import com.example.sl.photogallery.R;

import java.util.ArrayList;

/**
 * Created by sl on 2016/11/13.
 */

public class PollService extends IntentService{
    private static final String TAG = "PollService";

    private static final int POLL_INTERVAL = 1000*60;
    public static final String PREF_IS_ALARM_ON = "isAlarmOn";
    public static final String PERM_PRIVATE = "com.example.sl.photogallery.PRIVATE";
    public static final String ACTION_SHOW_NOTIFICATION = "com.example.sl.photogallery.SHOW_NOTIFICATION";

    public PollService(){
        super(TAG);
        Log.i(TAG, "Current thread name is: " + Thread.currentThread().getName());
        //12-12 12:08:44.386 25879-25879/com.example.sl.photogallery I/PollService: Current thread name is: main
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created.");
        Log.i(TAG, "Current thread name is: " + Thread.currentThread().getName() + ", Thread id: " + Thread.currentThread().getId());
        //12-12 12:17:42.046 1753-1753/com.example.sl.photogallery I/PollService: Current thread name is: main, Thread id: 1
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Current thread name is: " + Thread.currentThread().getName() + ", Thread id: " + Thread.currentThread().getId());
        //12-12 12:17:42.046 1753-3065/com.example.sl.photogallery I/PollService: Current thread name is: IntentService[PollService], Thread id: 3885
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;
        if (!isNetworkAvailable) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String query = prefs.getString(FlickrFetcher.PREF_SEARCH_QUERY, null);
        String lastResultId = prefs.getString(FlickrFetcher.PREF_LAST_RESULT_ID, null);

        int page = 1;
        ArrayList<GalleryItem> items;
        if (query != null){
            items = new FlickrFetcher().search(query, page);
        }else{
            items = new FlickrFetcher().fetchItems(page);
        }

        if (items.size() == 0) return;
        String resultId = items.get(0).getId();

        if (! resultId.equals(lastResultId)){
            Log.i(TAG, "Got a new result: " + resultId);

            Resources r = getResources();
            PendingIntent pi = PendingIntent.getActivity(this, 0,
                                new Intent(this, PhotoGalleryActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this)
                                        .setTicker(r.getString(R.string.new_pictures_title))
                                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                        .setContentTitle(r.getString(R.string.new_pictures_title))
                                        .setContentText(r.getString(R.string.new_pictures_text))
                                        .setContentIntent(pi)
                                        .setDefaults(Notification.DEFAULT_ALL)
                                        .setAutoCancel(true)
                                        .build();
            showBackgroundNotification(0, notification);

        }else{
            Log.i(TAG, "Got an old result: " + resultId);
        }
        prefs.edit().putString(FlickrFetcher.PREF_LAST_RESULT_ID, resultId).commit();

        Log.i(TAG, "Received an intent: " + intent);
    }

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent intent = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(context, 0 , intent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (isOn){
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pi);
            //alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000*60, pi);
        }else {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_IS_ALARM_ON, isOn).commit();
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent intent = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        //Flag_NO_CREATE indicating that if the described PendingIntent does not already exist, then simply return null instead of creating it.
        return pi != null;
    }

    void showBackgroundNotification(int requestCode, Notification notification){
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra("REQUEST_CODE", requestCode);
        i.putExtra("NOTIFICATION", notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "service destroyed.");
        super.onDestroy();
    }
}
