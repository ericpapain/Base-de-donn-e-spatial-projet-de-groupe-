package com.example.tpbda.com.example.tpbda.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.tpbda.DatabaseManager;
import com.example.tpbda.R;
import com.example.tpbda.com.example.tpbda.directionhelpers.FetchURL;
import com.example.tpbda.com.example.tpbda.directionhelpers.TaskLoadedCallback;
import com.example.tpbda.com.example.tpbda.modele.SphericalUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, TaskLoadedCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final int PERMS_CALL_ID = 777;
    private DatabaseManager databaseManager;

    private double latitude;
    private double longitude;
    Polyline currentLine;
    TextView textView;
    String bank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseManager  = new DatabaseManager(this);
        setTitle("Found ATM");
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        bank = getIntent().getStringExtra("_bank");
        textView = (TextView) findViewById(R.id.dist);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPermissions();
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        setMakers(googleMap);
        Log.e("DEBOG", "DB");

        mMap.moveCamera(CameraUpdateFactory.zoomBy(10));

        mMap.setOnMarkerClickListener(this);
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        if(mMap != null){

            LatLng latLng = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }


    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    private void checkPermissions(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMS_CALL_ID);

            return;
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
        }
        if(locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 10000, 0, this);
        }
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
        }
    }
    public void setMakers(GoogleMap map){

        ArrayList<LatLng> latlongAtm = databaseManager.getAtmForBank(bank);

        if(map != null){

            for(int i = 0; i < latlongAtm.size(); i++) {

               map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(latlongAtm.get(i)).draggable(false).title("ATM : " + i + 1));
            }

        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Marker myPosittion = mMap.addMarker( new MarkerOptions().position(new LatLng(latitude, longitude)));

        String title = marker.getTitle();

        showDistance(myPosittion, marker, title);

        new FetchURL(MapsActivity.this).execute(getUrl(myPosittion.getPosition(), marker.getPosition(), "driving"), "driving");


        return false;
    }
    /*
    * This methode return a distance between two points in format number
     */
    private String formatNumber(double distance) {

        String unit = "m";
        if (distance < 1) {
            distance *= 1000;
            unit = "mm";
        } else if (distance > 1000) {
            distance /= 1000;
            unit = "km";
        }

        return String.format("%4.3f%s", distance, unit);
    }
    private void showDistance(Marker point1, Marker point2, String title) {

        double distance = SphericalUtil.computeDistanceBetween(point1.getPosition(), point2.getPosition());

        textView.setText(title + " est à " + formatNumber(distance));
    }
    /*
     *
    */

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);

        return url;
    }

    @Override
    public void onTaskDone(Object... values) {

        if (currentLine != null)
            currentLine.remove();
        currentLine = mMap.addPolyline((PolylineOptions) values[0]);
    }
}
