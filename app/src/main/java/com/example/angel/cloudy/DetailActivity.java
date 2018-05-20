package com.example.angel.cloudy;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import com.example.angel.cloudy.databinding.ActivityDetailBinding;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.TextView;

import com.example.angel.cloudy.data.WeatherContract;
import com.example.angel.cloudy.utilities.CloudyDateUtils;
import com.example.angel.cloudy.utilities.CloudyWeatherUtils;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String SHARE_HASHTAG = " #CloudyApp";

    private static final int ID_DETAIL_LOADER = 353;
    private String mForecastSummary;
    private Uri mUri;

    private ActivityDetailBinding mDetailBinding;

    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_HUMIDITY = 3;
    public static final int INDEX_WEATHER_PRESSURE = 4;
    public static final int INDEX_WEATHER_WIND_SPEED = 5;
    public static final int INDEX_WEATHER_DEGREES = 6;
    public static final int INDEX_WEATHER_CONDITION_ID = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
        return true;
    }

    @Override // Lanza la actividad de configuracion cuando es seleccionada
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        if (id == R.id.action_share) {
            Intent shareIntent = createShareForecastIntent();
            startActivity(shareIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this).setType("text/plain").setText(mForecastSummary + SHARE_HASHTAG).getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {

        switch (loaderId) {
            case ID_DETAIL_LOADER:
                return new CursorLoader(this, mUri, WEATHER_DETAIL_PROJECTION, null, null, null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        }
        if (!cursorHasValidData) {
            return;
        }

        /****************
         * Icono Clima  *
         ****************/
        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID); // Lee el ID regresado por la API
        int weatherImageId = CloudyWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);
        mDetailBinding.primaryInfo.weatherIcon.setImageResource(weatherImageId);// Asigna icono

        /*****************
         * Fecha de Clima*
         *****************/
        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE); // Lee la fecha consultada en API
        String dateText = CloudyDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);
        mDetailBinding.primaryInfo.date.setText(dateText);

        /***********************
         * Descripcion Clima   *
         ***********************/
        String description = CloudyWeatherUtils.getStringForWeatherCondition(this, weatherId);// Consultada en API
        String descriptionA11y = getString(R.string.a11y_forecast, description);
        mDetailBinding.primaryInfo.weatherDescription.setText(description);
        mDetailBinding.primaryInfo.weatherDescription.setContentDescription(descriptionA11y);
        mDetailBinding.primaryInfo.weatherIcon.setContentDescription(descriptionA11y);

        /***********************
         * Temperatura Maxima  *
         ***********************/
        double highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP); // Lee la temperatura consultada en API
        String highString = CloudyWeatherUtils.formatTemperature(this, highInCelsius);
        String highA11y = getString(R.string.a11y_high_temp, highString);
        mDetailBinding.primaryInfo.highTemperature.setText(highString);
        mDetailBinding.primaryInfo.highTemperature.setContentDescription(highA11y);

        /***********************
         * Temperatura Minima  *
         ***********************/
        double lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP);// Lee la temperatura consultada en API
        String lowString = CloudyWeatherUtils.formatTemperature(this, lowInCelsius);
        String lowA11y = getString(R.string.a11y_low_temp, lowString);
        mDetailBinding.primaryInfo.lowTemperature.setText(lowString);
        mDetailBinding.primaryInfo.lowTemperature.setContentDescription(lowA11y);

        /************
         * Humedad  *
         ************/
        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);// Lee la Humedad consultada en API
        String humidityString = getString(R.string.format_humidity, humidity);
        String humidityA11y = getString(R.string.a11y_humidity, humidityString);
        mDetailBinding.extraDetails.humidity.setText(humidityString);
        mDetailBinding.extraDetails.humidity.setContentDescription(humidityA11y);
        mDetailBinding.extraDetails.humidityLabel.setContentDescription(humidityA11y);

        /**********************
         * Viento y Dirección *
         **********************/
        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = CloudyWeatherUtils.getFormattedWind(this, windSpeed, windDirection);
        String windA11y = getString(R.string.a11y_wind, windString);
        mDetailBinding.extraDetails.windMeasurement.setText(windString);
        mDetailBinding.extraDetails.windMeasurement.setContentDescription(windA11y);
        mDetailBinding.extraDetails.windLabel.setContentDescription(windA11y);

        /************
         * Pressure *
         ************/
        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);
        String pressureString = getString(R.string.format_pressure, pressure);
        String pressureA11y = getString(R.string.a11y_pressure, pressureString);
        mDetailBinding.extraDetails.pressure.setText(pressureString);
        mDetailBinding.extraDetails.pressure.setContentDescription(pressureA11y);
        mDetailBinding.extraDetails.pressureLabel.setContentDescription(pressureA11y);

        mForecastSummary = String.format("%s - %s - %s/%s", dateText, description, highString, lowString);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}