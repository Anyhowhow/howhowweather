package com.anyhowhow.howhowweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("admin_area")
    public String provinceName;
    @SerializedName("parent_city")
    public String cityName;
    @SerializedName("city")
    public String countyName;
    @SerializedName("id")
    public String weatherId;

    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
