package com.example.safetydrones.Tempo;

import retrofit2.http.GET;
import retrofit2.Call;

public interface TempoService {
    @GET("VisualCrossingWebServices/rest/services/timeline/Portugal%2C%20Porto?unitGroup=metric&key=5X7Y2D4QRQW2W7BT3HCC6CBJ8&contentType=json&include=current,days")
    Call<TempoResponde> getWeather();
}
