
package com.kimjunu.neighborhoodweather.model.forecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Fcst3hour {

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
    public Humidity humidity;

}
