package com.kimjunu.neighborhoodweather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.kimjunu.neighborhoodweather.model.weather.Common;
import com.kimjunu.neighborhoodweather.model.weather.Result;
import com.kimjunu.neighborhoodweather.model.weather.Weather;
import com.kimjunu.neighborhoodweather.model.weather.WeatherInfo;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    final int REQUEST_PERMISSION = 1000;

    WeatherService APIService;
    LocationManager locationManager;
    LocationListener locationListener;

    boolean isGPSEnabled;
    boolean isNetworkEnabled;

    double latitude;
    double longitude;

    @BindView(R.id.tvLocation)
    TextView tvLocation;
    @BindView(R.id.tvSky)
    TextView tvSky;
    @BindView(R.id.tvTemperNow)
    TextView tvTemperNow;
    @BindView(R.id.tvTemperMin)
    TextView tvTemperMin;
    @BindView(R.id.tvTemperMax)
    TextView tvTemperMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        APIService = WeatherService.retrofit.create(WeatherService.class);

        if (checkLocationPermission()) {
            initLocationManager();
            getLocationInfo();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PERMISSION) {
            initLocationManager();
        }
    }

    boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);

            return false;
        }

        return true;
    }

    void initLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    void getLocationInfo() {
        if (isGPSEnabled) {
            Log.e("GPS Enable", "true");

            final List<String> m_lstProviders = locationManager.getProviders(false);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    Log.e("location", "[" + location.getProvider() + "] (" + location.getLatitude() + "," + location.getLongitude() + ")");

                    // 날짜 선택
                    tvLocation.setText(getAddressFromLocation(latitude, longitude));

                    // 현재 날씨 받아오기
                    getCurrentWeather(latitude, longitude);

                    locationManager.removeUpdates(locationListener);
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (String name : m_lstProviders) {
                        if (checkLocationPermission())
                            locationManager.requestLocationUpdates(name, 1000, 5, locationListener);
                    }

                }
            });
        }
        else {
            Log.e("GPS Enable", "false");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    }

    String getAddressFromLocation(double latitude, double longitude) {
        String address = "";

        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.KOREA);
        List<Address> addresses;

        try {
            if (geocoder != null) {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && addresses.size() > 0) {
                    address = addresses.get(0).getAddressLine(0);
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

                    Result result = body.result;
                    Common common = body.common;
                    Weather weather = body.weather;

                    // API 호출 결과 확인
                    if (result == null) {
                        Toast.makeText(MainActivity.this, "No Result.", Toast.LENGTH_SHORT).show();

                        return;
                    }

                    if (result.code.equals(9200) == false) {
                        Toast.makeText(MainActivity.this, result.message, Toast.LENGTH_SHORT).show();

                        return;
                    }

                    // 특보 사항 확인
                    if (common == null)
                        Toast.makeText(MainActivity.this, "No Common.", Toast.LENGTH_SHORT).show();

                    // 날씨 정보
                    if (weather == null)
                        Toast.makeText(MainActivity.this, "No Weather.", Toast.LENGTH_SHORT).show();
                    else {

                    }
                } else if (response.errorBody() != null) {
                    String msg = null;
                    try {
                        msg = response.errorBody().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherInfo> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to API calling.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
