package com.kimjunu.neighborhoodweather;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    boolean mIsExiting = false;

    ForecastAdapter mForecastAdapter;

    ProgressDialog mProgressDialog = null;

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
    @BindView(R.id.rvForecast)
    RecyclerView rvForecast;
    @BindView(R.id.lvCity)
    ListView lvCity;

    boolean isExiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        APIService = WeatherService.retrofit.create(WeatherService.class);

        mForecastAdapter = new ForecastAdapter();
        rvForecast.setAdapter(mForecastAdapter);

        if (checkLocationPermission()) {
            initLocationManager();
            getLocationInfo();
        }
    }

    @Override
    public void onBackPressed() {
        if (isExiting == false) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isExiting = false;
                }
            }, 2000);

            Toast.makeText(this, "앱을 종료하려면 '뒤로' 버튼을 한 번 더 누르세요.",
                    Toast.LENGTH_SHORT).show();

            isExiting = true;
        } else {
            finish();
        }
    }

    //    @Override
//    public void onBackPressed() {
//        if (!mIsExiting) {
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mIsExiting = false;
//                }
//            }, 2000);
//
//            Toast.makeText(this, "앱을 종료하려면 '뒤로' 버튼을 한 번 더 누르세요.",
//                    Toast.LENGTH_SHORT).show();
//
//            mIsExiting = true;
//        } else {
//            finish();
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            initLocationManager();
            getLocationInfo();
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

                    String city;
                    String[] temp = address.getAddressLine(0).split(" ");

                    if (temp.length >= 3) {
                        if (temp[0].equals("대한민국"))
                            city = temp[1] + " " + temp[2] + " " + temp[3];
                        else
                            city = temp[0] + " " + temp[1] + " " + temp[2];
                    } else {
                        city = address.getAddressLine(0);
                    }

                    if (currentCity.equals(city))
                        return;

                    if (currentCity.equals("") && cities.contains(currentCity) == false)
                        cities.add(city);

                    currentCity = city;

                    tvLocation.setText(currentCity);

                    ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, R.layout.item_city, cities);
                    lvCity.setAdapter(adapter);

                    // 현재 날씨 받아오기
                    getCurrentWeather(latitude, longitude);

                    // 일기 예보 받아오기
                    getForecast3Days(latitude, longitude);

                    if (mProgressDialog != null && mProgressDialog.isShowing())
                        mProgressDialog.dismiss();

                    adapter.notifyDataSetChanged();
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
                    if (providers.isEmpty() == false) {
                        mProgressDialog = ProgressDialog.show(MainActivity.this, "", "잠시만 기다려주세요.", true);

                        if (checkLocationPermission()) {
                            for (String provider : providers) {
                                locationManager.requestLocationUpdates(provider, 1000, 5, locationListener);
                            }
                        }
                    }
                }
            });
        }
    }

    Address getAddressFromLocation(double latitude, double longitude) {
        Address address = null;

        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.KOREA);
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

    Address getLocationFromAddress(String addressString) {
        Geocoder geocoder = new Geocoder(MainActivity.this);
        Address address = null;

        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 5);

            if (addresses.isEmpty() == false)
                address = addresses.get(0);
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
                        Toast.makeText(MainActivity.this, "No Weather.", Toast.LENGTH_SHORT).show();
                    else {
                        if (weather.hourly.isEmpty())
                            return;

                        Hourly hourly = weather.hourly.get(0);
                        tvSky.setText(hourly.sky.name);
                        String temp = ((int) Double.parseDouble(hourly.temperature.tc)) + "℃";
                        String tempMin = ((int) Double.parseDouble(hourly.temperature.tmin)) + "℃";
                        String tempMax = ((int) Double.parseDouble(hourly.temperature.tmax)) + "℃";
                        tvTemperNow.setText(temp);
                        tvTemperMin.setText(tempMin);
                        tvTemperMax.setText(tempMax);

                        layoutTemperature.setVisibility(View.VISIBLE);

                        View rootView = getWindow().getDecorView().getRootView();

                        if (hourly.sky.code.equals("SKY_O01")) {
                            // 맑음
                            rootView.setBackgroundResource(R.mipmap.bg_sunny);
                        } else if (hourly.sky.code.equals("SKY_O02") || hourly.sky.code.equals("SKY_O03") ||
                                hourly.sky.code.equals("SKY_O07")) {
                            // 흐림
                            rootView.setBackgroundResource(R.mipmap.bg_cloudy);
                        } else if (hourly.sky.code.equals("SKY_O04") || hourly.sky.code.equals("SKY_O06") ||
                                hourly.sky.code.equals("SKY_O08") || hourly.sky.code.equals("SKY_O10")) {
                            // 비
                            rootView.setBackgroundResource(R.mipmap.bg_rainy);
                        } else if (hourly.sky.code.equals("SKY_O05") || hourly.sky.code.equals("SKY_O09")) {
                            // 눈
                            rootView.setBackgroundResource(R.mipmap.bg_snowy);
                        } else {
                            // 낙뢰
                            rootView.setBackgroundResource(R.mipmap.bg_lightening);
                        }
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

                        ArrayList<SimpleForecastInfo> forecastInfos = new ArrayList<>();

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
                                    temp = String.valueOf((int) Double.parseDouble(temp)) + "℃";
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            forecastInfos.add(new SimpleForecastInfo(time, sky, temp));
                        }

                        rvForecast.setHasFixedSize(true);

                        mForecastAdapter.setDataset(forecastInfos);

                        mForecastAdapter.notifyDataSetChanged();
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

    @OnClick(R.id.btnAddCity)
    void showAddAddressDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText etAddress = new EditText(this);
        etAddress.setSingleLine();

        alert.setTitle("지역 추가");
        alert.setMessage("주소를 입력하세요\n(예: OO시 OO구 OO동)");

        alert.setView(etAddress);

        alert.setPositiveButton("추가", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String address = etAddress.getText().toString();

                Address location = getLocationFromAddress(address);

                if (address == null)
                    return;

                String city;
                String[] temp = location.getAddressLine(0).split(" ");

                if (temp.length >= 3) {
                    if (temp[0].equals("대한민국"))
                        city = temp[1] + " " + temp[2] + " " + temp[3];
                    else
                        city = temp[0] + " " + temp[1] + " " + temp[2];
                } else {
                    city = location.getAddressLine(0);
                }

                if (cities.contains(city) == false)
                    cities.add(city);

                ArrayAdapter adapter = (ArrayAdapter) lvCity.getAdapter();
                adapter.notifyDataSetChanged();

                updateCityWeather(cities.size() - 1);
            }
        });
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();
    }

    @OnItemClick(R.id.lvCity)
    public void onCityItemClick(AdapterView<?> parent, int position) {
        updateCityWeather(position);
    }

    public void updateCityWeather(int position) {
        Address location = getLocationFromAddress(cities.get(position));

        if (location == null)
            return;

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        getCurrentWeather(latitude, longitude);

        getForecast3Days(latitude, longitude);

        tvLocation.setText(cities.get(position));
    }

    @OnItemLongClick(R.id.lvCity)
    public boolean onCityLongClick(AdapterView<?> parent, final int position) {
        if (position == 0)
            return true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("지역 삭제");
        builder.setMessage(cities.get(position) + " 을 삭제하시겠습니까?");
        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cities.remove(position);

                ArrayAdapter adapter = (ArrayAdapter) lvCity.getAdapter();
                adapter.notifyDataSetChanged();

                // 현재 위치 날씨로 변경
                updateCityWeather(0);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }
}
