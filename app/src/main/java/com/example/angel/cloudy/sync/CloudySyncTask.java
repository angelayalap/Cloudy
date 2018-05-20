package com.example.angel.cloudy.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;

import com.example.angel.cloudy.data.CloudyPreferences;
import com.example.angel.cloudy.data.WeatherContract;
import com.example.angel.cloudy.utilities.NetworkUtils;
import com.example.angel.cloudy.utilities.NotificationUtils;
import com.example.angel.cloudy.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class CloudySyncTask {

    /**Realiza la petición al servidor de JSON e ingresa la información en el conten provider*/
    synchronized public static void syncWeather(Context context) {

        try {
            URL weatherRequestUrl = NetworkUtils.getUrl(context);
            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);
            ContentValues[] weatherValues = OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context, jsonWeatherResponse);

            if (weatherValues != null && weatherValues.length != 0) {
                ContentResolver cloudyContentResolver = context.getContentResolver();
                /* Elimina datos viejos ya que no es necesario mantener datos*/
                cloudyContentResolver.delete(WeatherContract.WeatherEntry.CONTENT_URI, null, null);
                /* Ingresa nuevos datos el content provider*/
                cloudyContentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, weatherValues);

                boolean notificationsEnabled = CloudyPreferences.areNotificationsEnabled(context);

                /*Si se envio notifiación hace 1 día se actualiza dato*/
                long timeSinceLastNotification = CloudyPreferences.getEllapsedTimeSinceLastNotification(context);
                boolean oneDayPassedSinceLastNotification = false;
                if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                    oneDayPassedSinceLastNotification = true;
                }
                if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                    NotificationUtils.notifyUserOfNewWeather(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}