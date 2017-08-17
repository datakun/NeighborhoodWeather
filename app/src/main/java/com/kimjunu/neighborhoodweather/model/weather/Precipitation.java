
package com.kimjunu.neighborhoodweather.model.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Precipitation {

    @SerializedName("sinceOntime")
    @Expose
    public String sinceOntime;
    @SerializedName("type")
    @Expose
    public String type;

}
