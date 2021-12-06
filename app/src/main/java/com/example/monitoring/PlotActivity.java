package com.example.monitoring;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.Arrays;

/**
 * Affiche les données sous format graphique
 * @author Thomas ESCUDERO
 * @version 1.0
 */
public class PlotActivity extends AppCompatActivity {

    public static final String TAG = "PlotActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot2);

        // initialiser la référence sur XPLot :
        XYPlot plot = (XYPlot) findViewById(R.id.plot);

        // lire les données dans la base
        DatabaseHelper dbHelper = new DatabaseHelper(getBaseContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT timestamp, COUNT(BSSID) FROM data_save " +
                "GROUP BY timestamp", null);
        Log.d(TAG, "Il y'a " + c.getCount() + " lignes dans le résultat");

        if (c.getCount() > 0) {
            Integer[] ids = new Integer[c.getCount()];
            Integer[] times = new Integer[c.getCount()];
            Integer[] nbApps = new Integer[c.getCount()];
            c.moveToFirst();

            // Le graphe ne s'affiche pas si seulement les mêmes valeurs de nbApp
            // met par défaut une valeur à 0 à timestamp 0 pour contrer ce problème
            ids[0] = 0;
            times[0] = 0;
            nbApps[0] = 0;

             for (int i = 1; i < c.getCount(); i++) {
                // Lire les colonnes de chaque lignes
                ids[i] = i; // measurement id
                times[i] = c.getInt(0); // colonne 0 : timestamp
                nbApps[i] = c.getInt(1); // colonne 1 : nombre d'aps
                Log.d(TAG, "Measurement id " + i + " timestamp " + times[i] +
                        "nbApps " + nbApps[i]);
                c.moveToNext();

            }

            // Transformer les données en série XY
            XYSeries apsXY = new SimpleXYSeries(Arrays.asList(nbApps),
                    SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Nb APs");

            // Définir le format de la courbe (line rouge marqueurs bleus)
            LineAndPointFormatter seriesFormat =
                    new LineAndPointFormatter(Color.RED, Color.BLUE, null, null);

            // AJouter la série XY au plot
            plot.addSeries(apsXY, seriesFormat);

        } else {
            Log.d(TAG, "No items in result query");
        }
    }
}