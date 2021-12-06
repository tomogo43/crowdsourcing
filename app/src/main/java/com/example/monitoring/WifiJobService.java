/**
 * Classe qui mesure la liste des points d'accès disponibles
 * @author ESCUDERO Thomas
 * @version 1.0
 */
package com.example.monitoring;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.util.Xml;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class WifiJobService extends JobService {

    private static final String TAG = "WifiJobService";
    private boolean jobCancelled = false;
    private JobParameters parm;

    private WifiManager wifiMan;

    // zone date
    private Long tsLong;
    private String timestamp;

    private GPSCalculator gps = null;


    Runnable run = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {


            wifiMan = (WifiManager) getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);

            DatabaseHelper dbHelper = new DatabaseHelper(getBaseContext());

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();


            while (!jobCancelled) {

                try {
                    List<ScanResult> scanResults = wifiMan.getScanResults();


                    // mise en forme timestamp
                    tsLong = System.currentTimeMillis() / 1000;
                    timestamp = tsLong.toString();

                    // parcourir la liste retournée par le scan
                    for (ScanResult s : scanResults) {


                        // timestamp
                        values.put(DatabaseHelper.FeedEntry.COLUMN_NAME_TIMESTAMP, timestamp);

                        // APs wifi
                        values.put(DatabaseHelper.FeedEntry.COLUMN_NAME_BSSID, s.BSSID);


                        // latitude
                        values.put(DatabaseHelper.FeedEntry.COLUMN_NAME_LATITUDE,
                                gps.getLatitude());

                        // longitude
                        values.put(DatabaseHelper.FeedEntry.COLUMN_NAME_LONGITUDE,
                                gps.getLongitude());



                        // ajout des données dans la base de données
                        db.insert(
                                DatabaseHelper.FeedEntry.TABLE_NAME, null, values);


                        // affiche le SSID & BSSID des points d'accès wifi
                        Log.d(TAG, "ssid = " + s.SSID);
                        Log.d(TAG, "bssid = " + s.BSSID);

                        // affiche les coordonnées GPS
                        Log.d(TAG, "Latitude : " + gps.getLatitude());
                        Log.d(TAG, "Longitude : " + gps.getLongitude());

                    }

                    Thread.sleep(10000); // effectue un scan toutes les 10 sec
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }




            // Lorsque le traitement est terminé appel jobFinished sans relancer le job
            Log.d(TAG, "job finished");
            gps.stop();
            jobFinished(parm, false);


        }
    };




    // Méthode appelée quand la tâche est lancée
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, " onStartJob id = " + params.getJobId());
        gps = new GPSCalculator();

        Log.d(TAG, "onStartJob: lancementGPS");
        gps.run(MainActivity.context);

        // attendre l'acquisition de la position GPS

        doBackgroundWork(params);

        return true;
    }

    /**
     * Lance un thread qui effectue la mesure des points d'acès
     *
     * @param params
     */
    private void doBackgroundWork(JobParameters params) {
        this.parm = params;

        // lancement de la mesure dans un Thread à part
        new Thread(run).start();
    }

    // Méthode appelée quand la tâche est arrêtée par le scheduler
    // Retourne vrai si le scheduler doit relancer la tâche
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob id= " + params.getJobId());

        // arrêter le thread du job ici
        jobCancelled = true;


        return true; // relancer la tâche
    }


}

