/**
 * Classe qui lance le job WifiService en premier plan
 * @author ESCUDERO Thomas
 * @version 1.0
 */
package com.example.monitoring;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "foregroundServiceChannel";
    public static final int NOTIFICATION_ID = 1337;
    final int JOB_ID = 123;
    private static final String TAG = "ForegroundService";

    @Override
    public void onCreate() {
        super.onCreate();

        // lancement du job
        ComponentName serviceName = new ComponentName(this, WifiJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, serviceName)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "scheduleJobWifi success");
            Toast.makeText(this, "schedule job avec success", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "scheduleJobWifi fail");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this,0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("NetMonitor is running")
                .setContentText("Click to open app")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        return START_NOT_STICKY;


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Arrêt du scheduler
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JOB_ID);
        Log.d(TAG, "Job cancelled");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
