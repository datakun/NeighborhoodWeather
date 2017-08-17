
package com.kimjunu.neighborhoodweather.model.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Hourly {

    @SerializedName("grid")
    @Expose
    public Grid grid;
    @SerializedName("wind")
    @Expose
    public Wind wind;
    @SerializedName("precipitation")
    @Expose
    public Precipitation precipitation;
    @SerializedName("sky")
    @Expose
    public Sky sky;
    @SerializedName("temperature")
    @Expose
    public Temperature temperature;
    @SerializedName("humidity")
    @Expose
    public String humidity;
    @SerializedName("lightning")
    @Expose
    public String lightning;
    @SerializedName("timeRelease")
    @Expose
    public String timeRelease;

}
