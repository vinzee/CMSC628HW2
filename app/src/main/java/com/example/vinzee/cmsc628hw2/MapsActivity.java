package com.example.vinzee.cmsc628hw2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LatLng currLocation;
    private String username;
    private Float zoomLevel = 14.5f;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Bundle b = getIntent().getExtras();
        username = b.getString("username");
        Log.d("onCreate", "username: " + username);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("Friend Finder");
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 99);

            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        locationManager.removeUpdates(this);
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
        mMap = googleMap;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Log.d("onLocationChanged: ", latitude + " , " + longitude);
            currLocation = new LatLng(latitude, longitude);

            if (mMap != null) {
                JSONObject params = new JSONObject();
                try {
                    params.put("username", username);
                    params.put("latitude", latitude);
                    params.put("longitude", longitude);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("Params: ", params.toString());

                new MapsActivity.WebserviceAsyncTask().execute(params);
            }
        }
    }

    private class WebserviceAsyncTask extends AsyncTask<JSONObject, Integer, String[]> {

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            Log.w("WebserviceAsyncTask","onPostExecute called: " + System.currentTimeMillis());
            Toast.makeText(MapsActivity.this, s[1].toString(), Toast.LENGTH_SHORT).show();

            if(s[0] == "true"){
                mMap.clear();

                // Fill color of the circle
                // 0x represents, this is an hexadecimal code
                // 55 represents percentage of transparency. For 100% transparency, specify 00.
                // For 0% transparency ( ie, opaque ) , specify ff
                // The remaining 6 characters(00ff00) specify the fill color

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLocation, zoomLevel));

                mMap.addCircle(new CircleOptions()
                        .center(currLocation)
                        .radius(1000)
                        .strokeColor(0x990060ba)
                        .strokeWidth(2)
                        .fillColor(0x6075b7f5));

                mMap.addMarker(new MarkerOptions()
                        .position(currLocation)
                        .title(username)
                        .snippet("Your location")
                ).showInfoWindow();

                try {
                    JSONArray nearByFriends = new JSONArray(s[2]);

                    for (int i = 0 ; i < nearByFriends.length(); i++) {
                        JSONArray friend = nearByFriends.getJSONArray(i);

                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(friend.getJSONArray(2).getDouble(0), friend.getJSONArray(2).getDouble(1)))
                                .title(friend.getString(0))
                                .snippet(String.valueOf(friend.getDouble(1)) + "m")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        );

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected String[] doInBackground(JSONObject... jsonObjects) {
            Log.w("WebserviceAsyncTask","doInBackground");

            try {
                URL url = new URL(Constants.BASE_URL + "/update_location");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(5000);
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("PUT");
                connection.setDoInput(true);
                connection.setChunkedStreamingMode(0);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                connection.connect();

                Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                writer.write(jsonObjects[0].toString());
                writer.close();

                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)   {
                    StringBuilder line = new StringBuilder();
                    BufferedReader bread = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String temp;
                    while ((temp = bread.readLine()) != null) {
                        line.append(temp);
                    }
                    bread.close();

                    Log.d("WebserviceAsyncTask","executed successfully: " + line);

                    return new String[]{"true", "Location Updated", line.toString()};
                } else {
                    Log.d("WebserviceAsyncTask", "Invalid Request: " + connection.getResponseCode() + " , " + connection.getResponseMessage());

                    return new String[] {"false", "Error: " + connection.getResponseCode()};
                }
            } catch (IOException e) {
                Log.d("WebserviceAsyncTask: ", "IOException");
                e.printStackTrace();
                return new String[] {"false", "IOException: " + e.getMessage()};
            } catch (Exception e) {
                Log.d("WebserviceAsyncTask: ", "Exception");
                e.printStackTrace();
                return new String[]{"false", "Exception: " + e.getMessage()};
            }
        }
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem usernameMenuItem = menu.findItem(R.id.username_label);
        usernameMenuItem.setTitle(username);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                sharedpreferences.edit().remove("username").commit();

                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("username", username);
                intent.putExtras(b);
                MapsActivity.this.startActivity(intent);

                Toast.makeText(MapsActivity.this, "User logged out", Toast.LENGTH_SHORT).show();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
