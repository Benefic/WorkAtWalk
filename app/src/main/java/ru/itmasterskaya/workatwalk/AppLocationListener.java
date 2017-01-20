/*
 * Copyright abenefic (c) 2017.
 */

package ru.itmasterskaya.workatwalk;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

class AppLocationListener implements LocationListener {

    static Location imHere; // здесь будет всегда доступна самая последняя информация о местоположении пользователя.
    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private static Context mContext;

    static void setUpLocationListener(Context context) // это нужно запустить в самом начале работы программы
    {
        mContext = context;
        locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new AppLocationListener();
        if (checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        List<String> providers = locationManager.getProviders(true);

        for (String provider : providers) {
            locationManager.requestLocationUpdates(
                    provider,
                    5000,
                    10,
                    locationListener); // здесь можно указать другие более подходящие вам параметры

        }

        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);

            if (l == null) {
                continue;
            }
            if (imHere == null || l.getAccuracy() < imHere.getAccuracy()) {
                imHere = l;
            }
        }

    }

    static void stopLocationListener() {
        if (checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onLocationChanged(Location loc) {
        imHere = loc;
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        if (checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(
                provider,
                5000,
                10,
                locationListener);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
