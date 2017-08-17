
package com.kimjunu.neighborhoodweather.model.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("code")
    @Expose
    public Integer code;
    @SerializedName("requestUrl")
    @Expose
    public String requestUrl;
    @SerializedName("message")
    @Expose
    public String message;

}
