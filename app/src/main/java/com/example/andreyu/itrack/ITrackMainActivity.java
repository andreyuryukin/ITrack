package com.example.andreyu.itrack;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

public class ITrackMainActivity extends AppCompatActivity {

    public LocationManager manager;
    public GPSListener listener;
    public Location initialLocation;
    private static final int REQUEST_LOCATION = 0;
    public TextView logTextView;
    public String stringLog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itrack_main);

        logTextView = (TextView) findViewById(R.id.textViewLog);
        logTextView.setMovementMethod(new ScrollingMovementMethod());
        stringLog = "start of the execution";
        logTextView.setText(stringLog);
    }

    public void startTracking(View v) {

        stringLog = logTextView.getText() + "\nstartTracking: beginning";
        logTextView.setText(stringLog);

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        stringLog = logTextView.getText() + "\nstartTracking: getting initial location";
        logTextView.setText(stringLog);
        initialLocation = getInitLocation();

        stringLog = logTextView.getText() + "\nstartTracking: creating listener";
        logTextView.setText(stringLog);
        listener = new GPSListener(ITrackMainActivity.this, initialLocation);

        logTextView.setText(stringLog);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        stringLog = logTextView.getText() + "\nstartTracking: requesting updates";
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
    }

    public Location getInitLocation() {

        Location location = null;

        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                stringLog = logTextView.getText() + "\ngetLocation: checking permissions";
                logTextView.setText(stringLog);
            }

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                stringLog = logTextView.getText() + "\ngetLocation: requesting location";
                logTextView.setText(stringLog);
            }
        } else {
            Intent locIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(locIntent, REQUEST_LOCATION);

        }
        return location;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                        || manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    stringLog = logTextView.getText() + "\nonActivityResult: GPS was enabled";
                    logTextView.setText(stringLog);
                } else {
                    stringLog = logTextView.getText() + "\nonActivityResult: GPS was not enabled";
                    logTextView.setText(stringLog);
                }
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.removeUpdates(listener);
        stringLog = logTextView.getText() + "\nonStop removing listener updates";
        logTextView.setText(stringLog);
    }
}