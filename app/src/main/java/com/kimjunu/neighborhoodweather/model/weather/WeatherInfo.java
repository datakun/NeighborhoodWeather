
package com.kimjunu.neighborhoodweather.model.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WeatherInfo {

    @SerializedName("weather")
    @Expose
    public Weather weather;
    @SerializedName("common")
    @Expose
    public Common common;
    @SerializedName("result")
    @Expose
    public Result result;

}
