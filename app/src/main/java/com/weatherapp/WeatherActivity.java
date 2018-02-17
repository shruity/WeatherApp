package com.weatherapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.weatherapp.model.Channel;
import com.weatherapp.model.Weather;
import com.weatherapp.weatherInterface.WeatherApi;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherActivity extends AppCompatActivity {
    Context context;
    ProgressDialog progress;
    TextView tvCity, tvDate, tvTemperature, tvConditions, tvSunrise, tvSunset;
    ImageView picture;
    int ACCESS_LOCATION_ATTEMPTS=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retrofit_activity);
        context  = this;

        progress = new ProgressDialog(this,R.style.MyDialogTheme);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false);
        progress.show();

        tvCity          = findViewById(R.id.tvCity);
        tvDate          = findViewById(R.id.tvDate);
        tvTemperature   = findViewById(R.id.tvTemperature);
        tvConditions    = findViewById(R.id.tvConditions);
        tvSunrise       = findViewById(R.id.tvSunrise);
        tvSunset        = findViewById(R.id.tvSunset);
        picture         = findViewById(R.id.weatherIconImageView);

        getWeatherData();
    }
    private void getWeatherData() {
        char unit = 'c';

        String YQL = "select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"bangalore\") and u='" + unit + "'";
        WeatherApi.Factory.getInstance().getWeather(YQL, "json").enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(@NonNull Call<Weather> call, @NonNull Response<Weather> response) {
                try{
                    Channel responseChannel = response.body().getQuery().getResults().getChannel();
                    tvTemperature.setText(responseChannel.getItem().getCondition().getTemp());
                    tvCity.setText(responseChannel.getLocation().getCity());
                    tvDate.setText(responseChannel.getLastBuildDate());
                    tvConditions.setText(responseChannel.getItem().getCondition().getText());
                    tvSunrise.setText(String.format("%s%s", getString(R.string.sunrise), responseChannel.getAstronomy().getSunrise()));
                    tvSunset.setText(String.format("%s%s", getString(R.string.sunset), responseChannel.getAstronomy().getSunset()));

                    int resourceId = getResources().getIdentifier("drawable/icon_" + responseChannel.getItem().getCondition().getCode(), null, getPackageName());
                    @SuppressWarnings("deprecation")
                    Drawable weatherIconDrawable = getResources().getDrawable(resourceId);
                    picture.setImageDrawable(weatherIconDrawable);

                    ArrayList<String> from = new ArrayList<>();
                    from.add("Date");
                    from.add("Day");
                    from.add("High");
                    from.add("Low");
                    from.add("Weather");

                    for (int i = 0; i < responseChannel.getItem().getForecast().size(); i++) {
                        from.add(responseChannel.getItem().getForecast().get(i).getDate());
                        from.add(responseChannel.getItem().getForecast().get(i).getDay());
                        from.add(responseChannel.getItem().getForecast().get(i).getHigh() + "");
                        from.add(responseChannel.getItem().getForecast().get(i).getLow() + "");
                        from.add(responseChannel.getItem().getForecast().get(i).getText());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.retrofit_forecast_item, R.id.cell, from);
                    GridView grid = findViewById(R.id.retrofit_grid);
                    grid.setAdapter(adapter);
                    grid.setNumColumns(5);

                    ACCESS_LOCATION_ATTEMPTS=0;
                    progress.dismiss();

                } catch (Exception e){
                    //we try to get the result seven times. If it fails, we show the error
                    if (ACCESS_LOCATION_ATTEMPTS<7){
                        getWeatherData();
                        ACCESS_LOCATION_ATTEMPTS++;
                    }
                    else{
                        Toast.makeText(context, "no weather data", Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Weather> call, @NonNull Throwable t) {
                Toast.makeText(context, "no weather data", Toast.LENGTH_LONG).show();
                progress.dismiss();
            }
        });

    }
    @Override
    public void onResume(){
        super.onResume();
        getWeatherData();
    }
    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this,R.style.MyDialogTheme)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        moveTaskToBack(true);
                        exitAppMethod();
                        finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    public void exitAppMethod(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finishAffinity();
        startActivity(intent);
    }
}
