
package com.kimjunu.neighborhoodweather.model.forecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Forecast3day {

    @SerializedName("grid")
    @Expose
    public Grid grid;
    @SerializedName("timeRelease")
    @Expose
    public String timeRelease;
    @SerializedName("fcst3hour")
    @Expose
    public Fcst3hour fcst3hour;
    @SerializedName("fcst6hour")
    @Expose
    public Fcst6hour fcst6hour;
    @SerializedName("fcstdaily")
    @Expose
    public Fcstdaily fcstdaily;

}
