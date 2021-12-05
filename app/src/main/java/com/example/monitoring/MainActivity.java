/**
 * @author ESCUDERO Thomas
 * @version 1.0
 */
package com.example.monitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    public static Context context;

    private static final String TAG = "MainActivity";
    private ForegroundService foregroundService;
    private boolean foregroundServiceBounded = false;
    private ArrayList wordList;
    private ArrayAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;



        wordList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, wordList);
        ListView lv = (ListView) findViewById(R.id.list_view);
        lv.setAdapter(adapter);


    }



    public void startJob(View v) {
        // désactive le bouton start et active le bouton stop
        Button btn_stop = findViewById(R.id.button2);
        btn_stop.setEnabled(true);
        v.setEnabled(false);

        scheduleJobWifi();
    }

    public void stopJob(View v) {
        // désactive le bouton start et active le bouton stop
        Button btn_start = findViewById(R.id.btn_start);
        btn_start.setEnabled(true);
        v.setEnabled(false);

        cancelJob();
    }

    public void afficheData(View v) {
        Intent plotActivity =  new Intent(this, PlotActivity.class);
        startActivity(plotActivity);

    }




    /**
     * Démarre un job au premier plan
     */
    private void scheduleJobWifi() {

        Intent serviceIntent = new Intent(this, ForegroundService.class);

        startService(serviceIntent);

    }


    /**
     * Arrête un job lancé
     */
    private void cancelJob() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);

    }

    /**
     * Lecture des données brutes du fichier sauvegarde.xml
     * @param filename nom du fichier
     * @param data données brutes
     * @return les données brutes du fichier XML sauvegarde.xml
     */
    private String readRawData(String filename, String data) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        try {
            fis = openFileInput(filename);
            isr = new InputStreamReader(fis);
            char[] inputBuffer = new char[fis.available()];
            isr.read(inputBuffer);
            data = new String(inputBuffer);
            Log.i(TAG, "read data from file " + filename);
            isr.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Charge les données pour les afficher dans le list view
     */
    public void loadData(View v) {


        adapter.clear();

        DatabaseHelper dbHelper = new DatabaseHelper(getBaseContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM data_save", null);
        Log.d(TAG, "there are " + c.getCount() + " in result query");

        while(c.moveToNext()) {
            String result =
                    "BSSID: " +
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_BSSID))
                    + " timestamp: " +
                    c.getString(c.getColumnIndexOrThrow(
                            DatabaseHelper.FeedEntry.COLUMN_NAME_TIMESTAMP))
                    + " latitude: " +
                    c.getString(c.getColumnIndexOrThrow(
                            DatabaseHelper.FeedEntry.COLUMN_NAME_LATITUDE))
                    + " longitude: " +
                            c.getString(c.getColumnIndexOrThrow(
                                    DatabaseHelper.FeedEntry.COLUMN_NAME_LONGITUDE));
            adapter.add(result);
        }


    }

    /**
     * reset les données de l'application
     * @param v
     */
    public void onReset(View v) {
        DatabaseHelper dbHelper = new DatabaseHelper(getBaseContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        db.execSQL("DELETE FROM " + DatabaseHelper.FeedEntry.TABLE_NAME);

        // supprime les données de la liste
        adapter.clear();

    }

    /**
     * Affiche les points d'accès sur la map
     * @param v
     */
    public void onShMap(View v) {
        Intent i = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(i);
    }


}