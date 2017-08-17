
package com.kimjunu.neighborhoodweather.model.forecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ForecastInfo {

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
