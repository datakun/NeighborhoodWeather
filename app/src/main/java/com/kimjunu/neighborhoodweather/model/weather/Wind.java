
package com.kimjunu.neighborhoodweather.model.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Wind {

    @SerializedName("wdir")
    @Expose
    public String wdir;
    @SerializedName("wspd")
    @Expose
    public String wspd;

}
