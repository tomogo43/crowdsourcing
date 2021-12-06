package com.example.monitoring;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

/**
 * GPSCalculator : classe qui analyse chaque nouvelle localisation
 * @author Thomas ESCUDERO
 * @version 1.0
 */
public class GPSCalculator {
    private LocationManager locationManager = null;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 20; // 20 metres
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1; // 10 secondes
    private static final String TAG = "GPSCalculator";
    private double latitude = 0;
    private double longitude = 0;

    // Les autorisations ont été au préalable ajoutées
    @SuppressLint("MissingPermission")
    public void run(Context context) {

        locationManager = (LocationManager) context.getSystemService(Service.LOCATION_SERVICE);


        // Données cellulaires + WiFi
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

        // GPS
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

    }

    /**
     * stop : arrête la mise à jour de la nouvelle localisation
     */
    public void stop()
    {
        locationManager.removeUpdates(locationListener);
    }

    /**
     * locationListener appelé à chaque nouvelle localisation détectée
     */
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location newLocation) {
            Log.d(TAG, "onLocationChanged: ici");
            latitude = newLocation.getLatitude();
            Log.d(TAG, "onLocationChanged: lat = " + latitude);
            longitude = newLocation.getLongitude();
            Log.d(TAG, "onLocationChanged: long = " + longitude);
        }
    };

    /**
     * getLatitude : obtenir la latitude
     * @return latitude
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * getLongitude : obtenir la longitude
     * @return longitude
     */
    public double getLongitude() {
        return this.longitude;
    }
}
