package com.kimjunu.neighborhoodweather;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kimjunu.neighborhoodweather.model.forecast.Forecast3day;
import com.kimjunu.neighborhoodweather.model.forecast.ForecastInfo;
import com.kimjunu.neighborhoodweather.model.weather.Hourly;
import com.kimjunu.neighborhoodweather.model.weather.Weather;
import com.kimjunu.neighborhoodweather.model.weather.WeatherInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
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

    ArrayList<String> cities = new ArrayList<>();
    String currentCity = "";

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
    @BindView(R.id.layoutTemperature)
    LinearLayout layoutTemperature;
    @BindView(R.id.lvForecast)
    ListView lvForecast;
    @BindView(R.id.lvCity)
    ListView lvCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        APIService = WeatherService.retrofit.create(WeatherService.class);

        cities.add("+");

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

                    // 날짜 선택
                    currentCity = getAddressFromLocation(latitude, longitude);
                    tvLocation.setText(currentCity);

                    if (cities.contains(currentCity) == false)
                        cities.add(0, currentCity);

                    ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, R.layout.simple_list_item, cities);
                    lvCity.setAdapter(adapter);

                    // 현재 날씨 받아오기
                    getCurrentWeather(latitude, longitude);

                    // 일기 예보 받아오기
                    getForecast3Days(latitude, longitude);

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
        } else {
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
                    address = addresses.get(0).getAdminArea() + " " + addresses.get(0).getLocality()
                            + " " + addresses.get(0).getThoroughfare();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }

    ArrayList<Location> getLocationFromAddress(String address) {
        ArrayList<Location> locations = new ArrayList<>();
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocationName(address, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses != null) {
            for (int i = 0; i < addresses.size(); i++) {
                Location location = new Location("");
                double lat = addresses.get(i).getLatitude();
                double lon = addresses.get(i).getLongitude();

                location.setLatitude(lat);
                location.setLongitude(lon);

                locations.add(location);

                Log.e("location", lat + " " + lon + ", " + addresses.get(i).getAdminArea() + " " + addresses.get(i).getLocality() + " " + addresses.get(i).getThoroughfare());
            }
        }

        return locations;
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
                        Toast.makeText(MainActivity.this, "No Weather.", Toast.LENGTH_SHORT).show();
                    else {
                        if (weather.hourly.isEmpty())
                            return;

                        Hourly hourly = weather.hourly.get(0);
                        tvSky.setText(hourly.sky.name);
                        String temperatureNow = ((int) Double.parseDouble(hourly.temperature.tc)) + "°";
                        tvTemperNow.setText(temperatureNow);
                        tvTemperMin.setText(String.valueOf((int) Double.parseDouble(hourly.temperature.tmin)));
                        tvTemperMax.setText(String.valueOf((int) Double.parseDouble(hourly.temperature.tmax)));

                        layoutTemperature.setVisibility(View.VISIBLE);
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

    void getForecast3Days(double latitude, double longitude) {
        APIService.getForecast3Days(1, String.valueOf(latitude), String.valueOf(longitude)).enqueue(new Callback<ForecastInfo>() {
            @Override
            public void onResponse(Call<ForecastInfo> call, Response<ForecastInfo> response) {
                if (response.body() != null) {
                    ForecastInfo body = response.body();

                    assert body != null;
                    com.kimjunu.neighborhoodweather.model.forecast.Weather weather = body.weather;

                    // 날씨 정보
                    if (weather == null)
                        Toast.makeText(MainActivity.this, "No Weather.", Toast.LENGTH_SHORT).show();
                    else {
                        if (weather.forecast3days.isEmpty())
                            return;

                        Forecast3day forecast = weather.forecast3days.get(0);

                        ArrayList<String> forecastInfos = new ArrayList<>();

                        int offsetHour = 3;
                        int maxHour = 49;

                        Date today = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 a hh시");

                        for (int hour = 4; hour <= maxHour; hour += offsetHour) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(today);
                            calendar.add(Calendar.HOUR, hour);

                            String time = sdf.format(calendar.getTime());
                            String sky = "";
                            String temp = "";

                            try {
                                Field skyField = forecast.fcst3hour.sky.getClass().getField("name" + hour + "hour");
                                sky = skyField.get(forecast.fcst3hour.sky).toString();

                                Field tempField = forecast.fcst3hour.temperature.getClass().getField("temp" + hour + "hour");
                                temp = tempField.get(forecast.fcst3hour.temperature).toString();
                                if (temp.isEmpty() == false)
                                    temp = String.valueOf((int) Double.parseDouble(temp));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            forecastInfos.add(time + ",  " + sky + ",  " + temp + "°");
                        }

                        ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, R.layout.simple_list_item, forecastInfos);
                        lvForecast.setAdapter(adapter);
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
            public void onFailure(Call<ForecastInfo> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to API calling.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnItemClick(R.id.lvCity)
    public void onCityItemClick(AdapterView<?> parent, int position) {
        if (cities.get(position).equals("+")) {
            // 새 도시 추가
            showAddAddressDialog();
        } else {
            // 도시 변경
            ArrayList<Location> locations = getLocationFromAddress(cities.get(position));

            if (locations.isEmpty())
                return;

            latitude = locations.get(0).getLatitude();
            longitude = locations.get(0).getLongitude();

            // 현재 날씨 받아오기
            getCurrentWeather(latitude, longitude);

            // 일기 예보 받아오기
            getForecast3Days(latitude, longitude);

            tvLocation.setText(cities.get(position));
        }
    }

    @OnItemLongClick(R.id.lvCity)
    public boolean onCityLongClick(AdapterView<?> parent, int position) {
        Log.e("location", cities.get(position));

        if (position == 0)
            return false;

        showDeleteAddressDialog(position);

        return true;
    }

    void showAddAddressDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText etAddress = new EditText(this);

        alert.setTitle("도시 추가");
        alert.setMessage("주소를 입력하세요\n(예: OO시 OO구 OO동)");

        alert.setView(etAddress);

        alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String address = etAddress.getText().toString();

                ArrayList<Location> locations = getLocationFromAddress(address);

                if (locations.isEmpty() == false) {
                    cities.add(cities.size() - 1, address);
                }
            }
        });


        alert.setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();

        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ArrayList<Location> locations = getLocationFromAddress(charSequence.toString());

                if (locations.isEmpty()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    void showDeleteAddressDialog(final int index) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("도시 삭제");
        alert.setMessage(cities.get(index) + " 를 삭제하시겠습니까?");

        alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (cities.get(0).equals(currentCity)) {
                    ArrayList<Location> locations = getLocationFromAddress(cities.get(0));

                    if (locations.isEmpty())
                        return;

                    latitude = locations.get(0).getLatitude();
                    longitude = locations.get(0).getLongitude();

                    // 현재 날씨 받아오기
                    getCurrentWeather(latitude, longitude);

                    // 일기 예보 받아오기
                    getForecast3Days(latitude, longitude);
                }

                cities.remove(index);

                lvCity.getAdapter().notifyAll();
            }
        });


        alert.setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();
    }
}
