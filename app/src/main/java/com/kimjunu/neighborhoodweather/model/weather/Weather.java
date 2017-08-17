
package com.kimjunu.neighborhoodweather.model.weather;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Weather {

    @SerializedName("hourly")
    @Expose
    public List<Hourly> hourly = null;

}
