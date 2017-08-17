
package com.kimjunu.neighborhoodweather.model.forecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Grid {

    @SerializedName("latitude")
    @Expose
    public String latitude;
    @SerializedName("longitude")
    @Expose
    public String longitude;
    @SerializedName("city")
    @Expose
    public String city;
    @SerializedName("county")
    @Expose
    public String county;
    @SerializedName("village")
    @Expose
    public String village;

}
