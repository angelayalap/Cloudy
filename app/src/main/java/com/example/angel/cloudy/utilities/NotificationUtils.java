package com.example.angel.cloudy.utilities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.example.angel.cloudy.DetailActivity;
import com.example.angel.cloudy.R;
import com.example.angel.cloudy.data.CloudyPreferences;
import com.example.angel.cloudy.data.WeatherContract;

public class NotificationUtils {


    public static final String[] WEATHER_NOTIFICATION_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
    };

    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_MAX_TEMP = 1;
    public static final int INDEX_MIN_TEMP = 2;

    private static final int WEATHER_NOTIFICATION_ID = 3004;

    /**
     * Constructs and displays a notification for the newly updated weather for today.
     * @param context Context used to query our ContentProvider and use various Utility methods
     */
    public static void notifyUserOfNewWeather(Context context) {

        /* Construye URI para mostrar el pronostico de hoy en la notificacion */
        Uri todaysWeatherUri = WeatherContract.WeatherEntry.buildWeatherUriWithDate(CloudyDateUtils.normalizeDate(System.currentTimeMillis()));

        Cursor todayWeatherCursor = context.getContentResolver().query(todaysWeatherUri, WEATHER_NOTIFICATION_PROJECTION,
                null,
                null,
                null);

        if (todayWeatherCursor.moveToFirst()) { // Si el cursor esta vacio no muestra notifiación

            /* Valores regresados por la API */
            int weatherId = todayWeatherCursor.getInt(INDEX_WEATHER_ID);
            double high = todayWeatherCursor.getDouble(INDEX_MAX_TEMP);
            double low = todayWeatherCursor.getDouble(INDEX_MIN_TEMP);

            Resources resources = context.getResources();
            int largeArtResourceId = CloudyWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);

            Bitmap largeIcon = BitmapFactory.decodeResource(resources, largeArtResourceId);
            String notificationTitle = context.getString(R.string.app_name);
            String notificationText = getNotificationText(context, weatherId, high, low);
            int smallArtResourceId = CloudyWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId);

            //Se usa NotificationCompat Builder para construir la notifiación en background
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setColor(ContextCompat.getColor(context,R.color.colorPrimary))
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setAutoCancel(true);

            // Abre laa vista detallada cuando se da click a la notificación
            Intent detailIntentForToday = new Intent(context, DetailActivity.class);
            detailIntentForToday.setData(todaysWeatherUri);

            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
            taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
            PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setContentIntent(resultPendingIntent);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build());

            CloudyPreferences.saveLastNotificationTime(context, System.currentTimeMillis());
        }

        todayWeatherCursor.close();
    }

    /**Construye el resumen del día a notificar     */
    private static String getNotificationText(Context context, int weatherId, double high, double low) {
        //Descripción corta de clima
        String shortDescription = CloudyWeatherUtils.getStringForWeatherCondition(context, weatherId);
        String notificationFormat = context.getString(R.string.format_notification);

        String notificationText = String.format(notificationFormat, shortDescription,
                CloudyWeatherUtils.formatTemperature(context, high),
                CloudyWeatherUtils.formatTemperature(context, low));
        return notificationText;
    }
}
