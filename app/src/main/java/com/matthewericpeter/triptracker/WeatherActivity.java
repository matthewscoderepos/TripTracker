package com.matthewericpeter.triptracker;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class WeatherActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private CoordinatorLayout coordinatorLayout;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Typeface weatherFont = Typeface.createFromAsset(getAssets(), "weather.ttf");
        TextView weatherIcon = findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);

        Bundle b = getIntent().getExtras();
        String url = b.getString("WeatherURL");
        GetWeather(url);

    }

    private void GetWeather(String url) {

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main_object = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);

                    String temp = String.valueOf(main_object.getDouble("temp"));
                    String city = response.getString("name");
                    int iconID = object.getInt("id");

                    TextView cityField = findViewById(R.id.city_field);
                    TextView updatedField = findViewById(R.id.updated_field);
                    TextView detailsField = findViewById(R.id.details_field);
                    TextView temperatureField = findViewById(R.id.current_temperature_field);

                    setWeatherIcon(iconID);

                    DateFormat df = DateFormat.getTimeInstance();
                    String sRise = df.format(new Date(response.getJSONObject("sys").getLong("sunrise") * 1000));
                    String sSet = df.format(new Date(response.getJSONObject("sys").getLong("sunset") * 1000));

                    temperatureField.setText(String.format("%s°F", temp));
                    detailsField.setText(object.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Humidity: " + main_object.getString("humidity") + "%" +
                            "\n" + "Pressure: " + main_object.getString("pressure") + " hPa" +
                            "\n" + "Sunrise: " + sRise + "\n" + "Sunset: " + sSet);
                    cityField.setText(city);

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE-MM-dd");
                    String formattedDate = sdf.format(calendar.getTime());
                    updatedField.setText(formattedDate);




                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);
    }

    private void setWeatherIcon(int iconID) {
        //ImageView weatherIcon = findViewById(R.id.weather_icon);
        //Picasso.with(this).load("http://openweathermap.org/img/w/" + iconID + ".png").into(weatherIcon);
        String icon = "";
        int ID = iconID / 100;

        if(iconID == 800){
            icon = getString(R.string.weather_sunny);
        }
        else{
            switch(ID){
                case 2 :
                    icon = getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = getString(R.string.weather_drizzle);
                    break;
                case 7 :
                    icon = getString(R.string.weather_foggy);
                    break;
                case 8 :
                    icon = getString(R.string.weather_cloudy);
                    break;
                case 6 :
                    icon = getString(R.string.weather_snowy);
                    break;
                case 5 :
                    icon = getString(R.string.weather_rainy);
                    break;
            }
        }
        TextView weatherIcon = findViewById(R.id.weather_icon);
        weatherIcon.setText(icon);
    }

}
