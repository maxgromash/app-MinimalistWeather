package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private TextView textView = null;
    private LocationManager locationManager = null;
    private JSONObject data = null;
    ProgressBar progressBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //Hide action bar
        getSupportActionBar().hide();

        //Init
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        textView = findViewById(R.id.WeatherTextView);
        progressBar = findViewById(R.id.progressBar2);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, 23);
        }

        //Update geo
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private void getWeatherFromLoc(@org.jetbrains.annotations.NotNull Location location)
    {
        //Using API
         String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric&lang=ru",
                 ((int)(location.getLatitude()*1000))/1000.0, ((int)(location.getLongitude()*1000))/1000.0, "b0ff7f79b8ed3d3459551dbf6eedc5b2");

        GetWeatherInfo info = new GetWeatherInfo();
        try {
            //Parse weather and show info
            data = new JSONObject(info.execute(url).get());
            String loc = data.getString("name");
            String temp = data.getJSONObject("main").getString("temp");
            String desk = data.getJSONArray("weather").getJSONObject(0).getString("description");
            desk =  desk.substring(0,1).toUpperCase() + desk.substring(1);
            textView.setText(String.format("%s ℃\n%s\n%s", temp, desk, loc));
            textView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            getWeatherFromLoc(location);
        }
    };

    private class GetWeatherInfo extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //Downloading weather info JSON
            URL url = null;
            HttpURLConnection connection = null;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream stream =  connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(stream);
                BufferedReader bufferedInputStream = new BufferedReader(reader);
                String line = bufferedInputStream.readLine();
                while (line != null)
                {
                    stringBuilder.append(line);
                    line = bufferedInputStream.readLine();
                }
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            finally {
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }
}