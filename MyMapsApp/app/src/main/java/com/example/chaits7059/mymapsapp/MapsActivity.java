package com.example.chaits7059.mymapsapp;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    private Location myLocation;
    private static final int MY_LOC_ZOOM_FACTOR = 17;
    EditText editSearch;
    private int stopper = 0;
    private List<android.location.Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editSearch = (EditText) findViewById(R.id.editText_search);
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

        LatLng sanDiego = new LatLng(32.7, -117.15);
        mMap.addMarker(new MarkerOptions().position(sanDiego).title("My Birthplace"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sanDiego));


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsAppp", "Failed Permission check 1");
            Log.d("MyMapsAppp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsAppp", "Failed Permission check 2");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }


        //mMap.setMyLocationEnabled(true);
    }

    public void switchView(View v) {
        int viewer = 0;
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE && viewer == 0) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            viewer = 1;
        }
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL && viewer == 0) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            viewer = 1;
        }
    }

    public void SearchPOI(View v) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSenabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            myLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        }

        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(isNetworkEnabled){
            myLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        }

        //https://stackoverflow.com/questions/11538366/search-for-a-location-in-google-maps-without-giving-its-geo-coordinates
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        if (!editSearch.getText().toString().equals("")) {
            if (myLocation != null) {

                try {
                    addresses = geoCoder.getFromLocationName(
                            editSearch.getText().toString(), 5, myLocation.getLatitude()-0.0833, myLocation.getLongitude()-0.0833,
                            myLocation.getLatitude() + 0.0833, myLocation.getLongitude()+0.0833);

                    for (int i = 0; i < addresses.size(); i++) {
                        LatLng search = new LatLng(addresses.get(i).getLatitude(), addresses.get(i).getLongitude());
                        mMap.addMarker(new MarkerOptions().position(search).title("" + addresses.get(i).getFeatureName()));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(search, 10));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeMarkers(View v){
        mMap.clear();

    }


    public void getLocation(View v) {
        if(stopper >0){
            locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGPS);
            Log.d("MyMapsAppp", "STOPPED TRACKING");
            Toast.makeText(this, "STOPPED TRACKING", Toast.LENGTH_SHORT).show();
            stopper = 0;
            return;
        }
        stopper = 0;
        try {

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled) {
                Log.d("MyMapsAppp", "getLocation: GPS is enabled");
                Toast.makeText(this, "GPS Enabled", Toast.LENGTH_SHORT).show();
            }
            //get Network Status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) {
                Log.d("MyMapsAppp", "getLocation: Network is enabled");
                Toast.makeText(this, "Network Enabled", Toast.LENGTH_SHORT).show();
            }
            if (!isGPSenabled && !isNetworkEnabled) {
                Log.d("MyMapsAppp", "getLocation: No provider is enabled");
                Toast.makeText(this, "Nothing Enabled", Toast.LENGTH_SHORT).show();
            } else {
                this.canGetLocation = true;

                if (isGPSenabled) {
                    Log.d("MyMapsApppp", "getLocation: GPS enabled - requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGPS);

                    Log.d("MyMapsApppp", "getLocation: GPS log update request successful");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT).show();
                }

               if (isNetworkEnabled) {
                    Log.d("MyMapsApppp", "getLocation: Network enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                    Log.d("MyMapsApppp", "getLocation: Network log update request successful");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT).show();
                }

            }


        } catch (Exception e) {
            Log.d("MyMapsAppp", "Caught exception in getLocation");
            e.printStackTrace();
        }


    }


    android.location.LocationListener locationListenerGPS = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            //output in Log.d and Toast that GPS is enabled and working
            Log.d("MyMapsAppp", "locationListenerGPS: GPS enabled and working");
            //Toast.makeText(MapsActivity.this, "Using GPS", Toast.LENGTH_SHORT).show();

            //Drop a marker on the map - create a method called dropMarker
            dropMarker(locationManager.GPS_PROVIDER);

            //remove the network location updates.
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(locationListenerNetwork);
            Toast.makeText(MapsActivity.this, "Removed Network", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output in Log.d and Toast that GPS is enabled and working
            Log.d("MyMapsAppp", "GPS Status Change");
            Toast.makeText(MapsActivity.this, "GPS Status Change", Toast.LENGTH_SHORT).show();

            //setup a switch statement to check the status input parameter
            //case LocationProvider.AVAILABLE --> output message to Log.d and Toast
            if (status == LocationProvider.AVAILABLE) {
                Log.d("MyMapsAppp", "GPS AVAILABLE");
            }

            //case LocationProvider.OUT_OF_SERVICE --> request updates from NETWORK_PROVIDER
            if (status == LocationProvider.OUT_OF_SERVICE) {
                Log.d("MyMapsAppp", "GPS UNAVAILABLE");
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        locationListenerNetwork);
            }
            //look at the boolean isNetworkEnabled above
            //case LocationProvider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER

            if(status == LocationProvider.TEMPORARILY_UNAVAILABLE){
                Log.d("MyMapsAppp", "GPS UNAVAILABLE");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        locationListenerNetwork);
            }
            //case default --> request updates from NETWORK_PROVIDER

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            //output in Log.d and Toast that GPS is enabled and working
            Log.d("MyMapsAppp", "locationListenerNetwork: Network enabled and working");
            //Toast.makeText(MapsActivity.this, "Using Network", Toast.LENGTH_SHORT).show();

            //Drop a marker on the map - create a method called dropMarker
            dropMarker(locationManager.NETWORK_PROVIDER);

            //Relaunch the network provider request (requestLocationUpdates (NETWORK_PROVIDER) )
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //ouptut message in Log.d and Toast
            Log.d("MyMapsAppp", "Network Lost");
            Toast.makeText(MapsActivity.this, "Network Lost", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void dropMarker(String provider) {

        LatLng userLocation = null;

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);
        }

        if(myLocation == null){
            //Display a message via log.d and/or Toast
            Log.d("MyMapsAppp", "dropMarker: Location is null");
            Toast.makeText(MapsActivity.this, "Location Does Not Exist", Toast.LENGTH_SHORT).show();
        } else {

            //Get the User location
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            //Dispaly a mesage with the lat/long (Toast or log.d)
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
            Toast.makeText(this, ""+userLocation, Toast.LENGTH_SHORT).show();

            //Drop the actual marker on the map
            //if using circles, reference the android circle class
            if(provider.equals(locationManager.GPS_PROVIDER)) {
                Log.d("MyMapsApppp", "GPS GPS");
                stopper++;
                Circle circle = mMap.addCircle(new CircleOptions().center(userLocation).
                        radius(1).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.BLUE));
            }

            if(provider.equals(locationManager.NETWORK_PROVIDER)) {
                Log.d("MyMapsApppp", "Network Network");
                stopper++;
                Circle circle = mMap.addCircle(new CircleOptions().center(userLocation).
                        radius(1).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.GREEN));

            }

            mMap.animateCamera(update);

        }

    }


}
