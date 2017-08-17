
package com.kimjunu.neighborhoodweather.model.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Temperature {

    @SerializedName("tc")
    @Expose
    public String tc;
    @SerializedName("tmax")
    @Expose
    public String tmax;
    @SerializedName("tmin")
    @Expose
    public String tmin;

}
