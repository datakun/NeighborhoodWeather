
package com.kimjunu.neighborhoodweather.model.forecast;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Weather {

    @SerializedName("forecast3days")
    @Expose
    public List<Forecast3day> forecast3days = null;

}
