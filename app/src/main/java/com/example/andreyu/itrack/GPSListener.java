package com.example.andreyu.itrack;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

public class GPSListener implements LocationListener {

    public Context context;
    public Location initialLocation;
    public Location prevLocation;
    public TextView initLatTextView;
    public TextView initLongTextView;
    public TextView initAccTextView;
    public TextView speedTextView;
    public TextView distanceTextView;
    public TextView curLongTextView;
    public TextView curLatTextView;
    public TextView curAccTextView;
    public TextView logTextView;
    public TextView movTypeTextView;
    public TextView progressTextView;
    public TableLayout progressTable;
    public TableLayout dataTable;
    public int speedKMH;
    public float distance;
    public String stringLog;
    public int accuracyDepth;
    public boolean fillLog;
    public boolean isBetterLocation;
    public static final int ONE_MINUTE = 1000 * 60;
    public static final int ACCURACY_ATTEMPTS = 7;
    public ProgressBar progress;
    public String text;

    public GPSListener(Context c, Location initLoc) {

        context = c;
        initialLocation = initLoc;
        accuracyDepth = 0;
        fillLog = true;
        progressTable = (TableLayout) ((Activity) context).findViewById(R.id.tableLayoutProgress);
        dataTable = (TableLayout) ((Activity) context).findViewById(R.id.tableLayoutData);
        progress = (ProgressBar) ((Activity) context).findViewById(R.id.progressBarAccuracy);
        logTextView = (TextView) ((Activity) context).findViewById(R.id.textViewLog);
        logTextView.setMovementMethod(new ScrollingMovementMethod());
        initLongTextView = (TextView) ((Activity) context).findViewById(R.id.textViewInitLon);
        initLatTextView = (TextView) ((Activity) context).findViewById(R.id.textViewInitLat);
        initAccTextView = (TextView) ((Activity) context).findViewById(R.id.textViewInitAcc);
        speedTextView = (TextView) ((Activity) context).findViewById(R.id.textViewSpeed);
        distanceTextView = (TextView) ((Activity) context).findViewById(R.id.textViewDistance);
        curLongTextView = (TextView) ((Activity) context).findViewById(R.id.textViewCurLong);
        curLatTextView = (TextView) ((Activity) context).findViewById(R.id.textViewCurLat);
        curAccTextView = (TextView) ((Activity) context).findViewById(R.id.textViewCurrAcc);
        movTypeTextView = (TextView) ((Activity) context).findViewById(R.id.textViewMovType);
        progressTextView = (TextView) ((Activity) context).findViewById(R.id.textViewProgress);
        stringLog = stringLog + "\nGPSListener: constructor";
        logTextView.setText(stringLog);
        progressTable.setVisibility(View.VISIBLE);
    }


    @Override
    public void onLocationChanged(Location location) {

        if (accuracyDepth < ACCURACY_ATTEMPTS) {
            initialLocation = location;
            prevLocation = location;
            accuracyDepth = accuracyDepth + 1;
            text = "Setting GPS location " + accuracyDepth * ACCURACY_ATTEMPTS + "%";
            progressTextView.setText(text);
            progress.setProgress(accuracyDepth);
            stringLog = logTextView.getText() + "\nonLocationChanged: accuracy depth =" + accuracyDepth;
            logTextView.setText(stringLog);
        } else {
            if (initialLocation == null) {
                stringLog = logTextView.getText() + "\nonLocationChanged: initial location is null";
                logTextView.setText(stringLog);
            } else if (location == null) {
                stringLog = logTextView.getText() + "\nonLocationChanged: current location is null";
                logTextView.setText(stringLog);
            } else {
                isBetterLocation = isBetterLocation(prevLocation, location);
                if (isBetterLocation) {
                    prevLocation = location;
                    if (fillLog) {
                        stringLog = logTextView.getText() + "\nonLocationChanged: got better location";
                        logTextView.setText(stringLog);
                        fillLog = false;
                        progressTable.setVisibility(View.GONE);
                        dataTable.setVisibility(View.VISIBLE);
                    }
                    printResults(initialLocation, location);
                } else {
                    stringLog = logTextView.getText() + "\nonLocationChanged: isn't better location";
                    logTextView.setText(stringLog);
                    fillLog = true;
                }
            }
        }
    }

    public boolean isBetterLocation(Location currentBestLocation, Location location) {

        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
        boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 100;

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate) {
            return true;
        }
        return false;
    }

    public void printResults(Location initialLocation, Location location) {

        text = "Init. Long. " + initialLocation.getLongitude();
        initLongTextView.setText(text);
        text = "Init. Lat. " + initialLocation.getLatitude();
        initLatTextView.setText(text);
        text = "Init. Accuracy " + initialLocation.getAccuracy() + " meters";
        initAccTextView.setText(text);

        speedKMH = (int) ((location.getSpeed() * 3600) / 1000);
        text = speedKMH + " Km/H";
        speedTextView.setText(text);


        if (speedKMH == 0) {
            text = "Moving Type - Stopped";
        } else if (speedKMH <= 5) {
            text = "Moving Type - Walking";
        } else {
            text = "Moving Type - Driving";
        }
        movTypeTextView.setText(text);

        distance = (int) initialLocation.distanceTo(location);
        text = "Distance: " + distance + " meters";
        distanceTextView.setText(text);

        text = "Curr. Long. " + location.getLongitude();
        curLongTextView.setText(text);
        text = "Curr. Lat. " + location.getLatitude();
        curLatTextView.setText(text);
        text = "Curr. Accuracy " + location.getAccuracy() + " meters";
        curAccTextView.setText(text);
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
}