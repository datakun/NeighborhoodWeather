package com.kimjunu.neighborhoodweather;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.kimjunu.neighborhoodweather.model.weather.Hourly;
import com.kimjunu.neighborhoodweather.model.weather.Weather;
import com.kimjunu.neighborhoodweather.model.weather.WeatherInfo;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlarmReceiver extends BroadcastReceiver {
    final int REQUEST_PERMISSION = 1000;
    final int NOTIFICATION_WEATHER_ONCE = 1200;

    WeatherService APIService;
    LocationManager locationManager;
    LocationListener locationListener;

    boolean isGPSEnabled;
    boolean isNetworkEnabled;

    double latitude;
    double longitude;

    String currentCity = "";

    Context mContext = null;

    private static PowerManager.WakeLock sCpuWakeLock;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        APIService = WeatherService.retrofit.create(WeatherService.class);

        if (checkLocationPermission()) {
            initLocationManager();
            getLocationInfo();
        }
    }

    public boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);

            return false;
        }

        return true;
    }

    public void initLocationManager() {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void getLocationInfo() {
        if (isGPSEnabled) {
            final List<String> providers = locationManager.getProviders(false);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationManager.removeUpdates(locationListener);

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    Address address = getAddressFromLocation(latitude, longitude);

                    if (address == null)
                        return;

                    String[] temp = address.getAddressLine(0).split(" ");

                    if (temp.length >= 3) {
                        if (temp[0].equals("대한민국"))
                            currentCity = temp[1] + " " + temp[2] + " " + temp[3];
                        else
                            currentCity = temp[0] + " " + temp[1] + " " + temp[2];
                    } else {
                        currentCity = address.getAddressLine(0);
                    }

                    // 현재 날씨 받아오기
                    getCurrentWeather(latitude, longitude);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.e("onStatusChanged", "onStatusChanged");
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.e("onProviderEnabled", "onProviderEnabled");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.e("onProviderDisabled", "onProviderDisabled");
                }
            };

            if (checkLocationPermission()) {
                for (String provider : providers) {
                    locationManager.requestLocationUpdates(provider, 1000, 5, locationListener);
                }
            }
        }
    }

    Address getAddressFromLocation(double latitude, double longitude) {
        Address address = null;

        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> addresses;

        try {
            if (geocoder != null) {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && addresses.size() > 0) {
                    address = addresses.get(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }

    void getCurrentWeather(double latitude, double longitude) {
        APIService.getCurrentWeather(1, String.valueOf(latitude), String.valueOf(longitude)).enqueue(new Callback<WeatherInfo>() {
            @Override
            public void onResponse(Call<WeatherInfo> call, Response<WeatherInfo> response) {
                if (response.body() != null) {
                    WeatherInfo body = response.body();

                    assert body != null;
                    Weather weather = body.weather;

                    // 날씨 정보
                    if (weather == null)
                        Toast.makeText(mContext, "No Weather.", Toast.LENGTH_SHORT).show();
                    else {
                        if (weather.hourly.isEmpty())
                            return;

                        Hourly hourly = weather.hourly.get(0);
                        String temp = ((int) Double.parseDouble(hourly.temperature.tc)) + "℃";

                        setNotification(currentCity, hourly.sky.name, temp);
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherInfo> call, Throwable t) {
            }
        });
    }

    public void setNotification(String city, String sky, String temper) {
        NotificationManager notificationmanager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.mipmap.neighborhood_weather_icon)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("날씨 알람")
                .setContentText(city)
                .setSubText(sky + ", " + temper)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX);

        assert notificationmanager != null;
        notificationmanager.notify(NOTIFICATION_WEATHER_ONCE, builder.build());

        if (sCpuWakeLock != null)
            return;

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);

        assert pm != null;
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "weather_notificatio");

        sCpuWakeLock.acquire(10*60*1000L /*10 minutes*/);


        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}