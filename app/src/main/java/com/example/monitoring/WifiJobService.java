/**
 * Classe qui mesure la liste des points d'accès disponibles
 * @author ESCUDERO Thomas
 * @version 1.0
 */
package com.example.monitoring;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.util.Xml;

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

    // Enregistrer dans un fichier XML
    public static final String fileName = "sauvegarde.xml";
    private FileOutputStream fos;
    private XmlSerializer serializer;

    // zone date
    private Long tsLong;
    private String timestamp;


    Runnable run = new Runnable() {
        @Override
        public void run() {

            // initialisation du fichier de sauvegarde
            initializeOutputFile();

            wifiMan = (WifiManager) getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);

            DatabaseHelper dbHelper = new DatabaseHelper(getBaseContext());

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();


            while (!jobCancelled) {

                try {
                    List<ScanResult> scanResults = wifiMan.getScanResults();


                    try {


                        // début balise measurement
                        serializer.startTag(null, "measurement");
                        tsLong = System.currentTimeMillis()/1000;
                        timestamp = tsLong.toString();

                        serializer.startTag(null, "timestamp");
                        serializer.text(timestamp);
                        serializer.endTag(null, "timestamp");

                        serializer.startTag(null, "BSSID");
                        // parcourir la liste retournée par le scan
                        for (ScanResult s : scanResults) {

                            // ajout des données dans la base de données
                            values.put(DatabaseHelper.FeedEntry.COLUMN_NAME_TIMESTAMP, timestamp);
                            // APs wifi
                            values.put(DatabaseHelper.FeedEntry.COLUMN_NAME_BSSID, s.BSSID);
                            // température CPU de l'appareil
                            values.put(DatabaseHelper.FeedEntry.COLUMN_NAME_CPU_TEMP,
                                    CPUTemperature());

                            db.insert(
                                    DatabaseHelper.FeedEntry.TABLE_NAME, null, values);

                            // ouverture balise record
                            serializer.startTag(null, "record");
                            // affiche le SSID des points d'accès wifi
                            Log.d(TAG, "ssid = " + s.SSID);
                            Log.d(TAG, "ssid = " + s.BSSID);

                            // ajout de la donnée entre les balises record
                            serializer.text(s.BSSID);

                            //fermeture balise record
                            serializer.endTag(null, "record");

                        }
                        serializer.endTag(null, "BSSID");

                        // fin balise measurement
                        serializer.endTag(null, "measurement");



                    } catch (IOException e) {
                        e.printStackTrace();
                    }




                    Thread.sleep(10000); // effectue un scan toutes les 10 sec
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }




            // Lorsque le traitement est terminé appel jobFinished sans relancer le job
            Log.d(TAG, "job finished");
            jobFinished(parm, false);


        }
    };



    // Méthode appelée quand la tâche est lancée
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, " onStartJob id = " + params.getJobId());
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

        // fin de la sauvegarde desdonnées
        endSaveData();


        return true; // relancer la tâche
    }

    /**
     * Initialisation de l'en-tête du fichier XML
     */
    public void initializeOutputFile() {
        try {
            deleteFile(fileName);
            fos = openFileOutput(fileName, Context.MODE_APPEND);

            serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature(
                    "http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "root");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ecriture de l'objet sérialisé qui contient toutes les balises dans sauvegarde.xml
     * Ferme le fichier après avoir terminé l'écriture
     */
    public void endSaveData() {
        Log.d(TAG, "endSaveData: ici");
        try {
            serializer.endTag(null, "root");
            serializer.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Récupère la température du CPU
     * @return temperature
     */
    public float CPUTemperature() {
        Process process;
        try {
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if(line!=null) {
                float temp = Float.parseFloat(line);
                return temp / 1000.0f;
            }else{
                return 51.0f;
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            return 0.0f;
        } catch (IOException e2) {
            e2.printStackTrace();
            return 0.0f;
        }
    }

}

