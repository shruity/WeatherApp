package com.weatherapp;


import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.weatherapp.model.Channel;
import com.weatherapp.model.Forecast;
import com.weatherapp.model.Item;
import com.weatherapp.model.Weather;
import com.weatherapp.weatherInterface.WeatherApi;

import org.junit.Test;
import org.junit.runner.RunWith;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;

@RunWith(AndroidJUnit4.class)
public class WeatherTest {
    @Test
    public void getWeather(){
        char unit = 'c';
        String query = "select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"bangalore\") and u='" + unit + "'";

        assertNotNull("Query YQL null", query);
        WeatherApi.Factory.getInstance().getWeather(query,"json").enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(@NonNull Call<Weather> call, @NonNull Response<Weather> response) {
                assertNotNull("response null",response);
                Channel channel = response.body().getQuery().getResults().getChannel();
                Item item = channel.getItem();
                assertThat(item.getPubDate(), is("Thu, 15 Feb 2018 01:00 PM PDT"));
                Forecast forecast = item.getForecast().get(0);
                assertThat(forecast.getDate(), is("15 Feb 2018"));
                assertThat(forecast.getDay(), is("Thu"));
                assertThat(forecast.getHigh(), is("84"));
                assertThat(forecast.getLow(), is("59"));
                assertThat(forecast.getText(), is("Sunny"));
            }

            @Override
            public void onFailure(@NonNull Call<Weather> call, @NonNull Throwable t) {

            }
        });
    }
}
