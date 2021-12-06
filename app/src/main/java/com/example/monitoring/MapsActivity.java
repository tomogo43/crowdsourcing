package com.example.monitoring;

import androidx.fragment.app.FragmentActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.monitoring.databinding.ActivityMapsBinding;

import java.util.List;

/**
 * Affiche sur une map les endroits visités et le nombre de point d'accès WiFi scanné
 * @author Thomas ESCUDERO
 * @version 1.0
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        double latitude = 0;
        double longitude = 0;
        mMap = googleMap;




        LatLng lastLocationPlaced = new LatLng(latitude, longitude);

        DatabaseHelper dbHelper = new DatabaseHelper(getBaseContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // regarde les différentes positions où a été fait les mesures et comptabilise le nombre d'APs
        Cursor c = db.rawQuery("SELECT COUNT(DISTINCT bssid), latitude, longitude " +
                "FROM data_save GROUP BY latitude, longitude", null);

        while(c.moveToNext()) {
            latitude = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_LATITUDE));
            longitude = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.FeedEntry.COLUMN_NAME_LONGITUDE));

            // place un marker sur la map
            LatLng latLng = new LatLng(latitude, longitude);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("nb AP = " + c.getInt(0));
            mMap.addMarker(markerOptions);

            // sauvegarde la dernière position placée
            lastLocationPlaced = latLng;

        }

        // animation zoom sur la dernière position GPS ajoutée
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocationPlaced, 12.0f));
    }
}