package com.kimjunu.neighborhoodweather;

import com.kimjunu.neighborhoodweather.model.forecast.ForecastInfo;
import com.kimjunu.neighborhoodweather.model.weather.WeatherInfo;

import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface WeatherService {
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://apis.skplanetx.com/weather/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @Headers("appKey: " + Data.AppKey)
    @GET("current/hourly?")
    Call<WeatherInfo> getCurrentWeather(@Query("version") int version, @Query("lat") String lat,
                                        @Query("lon") String lon);

    @Headers("appKey: " + Data.AppKey)
    @GET("forecast/3days?")
    Call<ForecastInfo> getForecast3Days(@Query("version") int version, @Query("lat") String lat,
                                        @Query("lon") String lon);
}