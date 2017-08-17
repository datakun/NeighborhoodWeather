
package com.kimjunu.neighborhoodweather.model.forecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Temperature_ {

    @SerializedName("tmax1day")
    @Expose
    public String tmax1day;
    @SerializedName("tmax2day")
    @Expose
    public String tmax2day;
    @SerializedName("tmax3day")
    @Expose
    public String tmax3day;
    @SerializedName("tmin1day")
    @Expose
    public String tmin1day;
    @SerializedName("tmin2day")
    @Expose
    public String tmin2day;
    @SerializedName("tmin3day")
    @Expose
    public String tmin3day;

}
