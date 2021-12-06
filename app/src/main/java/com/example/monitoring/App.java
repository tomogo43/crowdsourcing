/**
 * création du channel de la notification
 * @author ESCUDERO Thomas
 * @version 1.0
 */
package com.example.monitoring;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * Gestion d'un channel pour la notification du job en premier plan
 * @author Thomas ESCUDERO
 * @version 1.0
 */
public class App extends Application {
    public static final String CHANNEL_ID = "foregroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    public void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
