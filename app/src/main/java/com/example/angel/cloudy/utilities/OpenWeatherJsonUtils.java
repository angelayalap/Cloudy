package com.example.angel.cloudy.utilities;


import android.content.ContentValues;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import com.example.angel.cloudy.data.CloudyPreferences;
import com.example.angel.cloudy.data.WeatherContract;


public class OpenWeatherJsonUtils {

    /*Informacion de clima. Se asocian variables para los valores usados por API Weatherbit
       Para la API la informacion del pronostico de cada dia es un elemento del arreglo "data" */
    private static final String OWM_LIST = "data";

    /* Para la API OpenWeatherMap todas las temperaturas son hijos del objeto "main" en la version de paga "temp"
     * Para la API Weatherbit que es la que se uso se usan los siguientes valores definidos en las variables*/
    private static final String OWM_TEMPERATURE = "temp";
    private static final String OWM_MAX = "max_temp";
    private static final String OWM_MIN = "min_temp";

    /* Weather information. Each day's forecast info is an element of the "data" array */
    private static final String OWM_PRESSURE = "pres"; // presion
    private static final String OWM_HUMIDITY = "rh"; // humedad
    private static final String OWM_WINDSPEED = "wind_spd"; // velocidad de viento
    private static final String OWM_WIND_DIRECTION = "wind_dir"; // dirección de viento


    private static final String OWM_WEATHER = "weather";
    private static final String OWM_DESCRIPTION = "description";
    private static final String OWM_WEATHER_ICON = "icon";
    private static final String OWM_WEATHER_ID = "code";
    //private static  final String OWM_MESSAGE_CODE = "cod";

    /* Location information */
    private static final String OWM_CITY = "city_name";
    private static final String OWM_COORD = "coord";

    /* Location coordinate */
    private static final String OWM_LATITUDE = "lat";
    private static final String OWM_LONGITUDE = "lon";


    /*Debido a que la API OpenWeatherMap no regresa valores de Temperatura Max y Min en version gratuita,
     * Se opta por usar Weatherbit es posible que se encuentre en codigo referencias OpenWeatherMap*/

    /**
     * Este metodo analiza la respuesta JSON de una web y regresa un arreglo de Strings
     * describiendo el pronostico del clima de varios dias.
     *
     * @param forecastJsonStr Es la respuesta JSON del servidor
     * @return Regresa un Arreglo de Strings describiendo datos de clima
     * @throws JSONException Si ocurro algun error con los datos JSON se dispara excepcion
     *
     * @referencia Se usa Weatherbit API como referencia
     *             https://www.weatherbit.io/api/weather-forecast-16-day
     */
    public static ContentValues[] getWeatherContentValuesFromJson(Context context, String forecastJsonStr)
            throws JSONException {
        /*Se crea objeto JSON*/
        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        /*Se crea el arreglo JSON tiene como parametro la lista que contendra la info de cada dia*/
        JSONArray jsonWeatherArray = forecastJson.getJSONArray(OWM_LIST);
        /*Se obtiene Latitud y Longitud */
        //JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        //JSONObject cityCoord = forecastJson.getJSONObject(OWM_COORD);
        double cityLatitude = forecastJson.getDouble(OWM_LATITUDE);
        double cityLongitude = forecastJson.getDouble(OWM_LONGITUDE);

        CloudyPreferences.setLocationDetails(context, cityLatitude, cityLongitude);
        ContentValues[] weatherContentValues = new ContentValues[jsonWeatherArray.length()];

        long normalizedUtcStartDay = CloudyDateUtils.getNormalizedUtcDateForToday();
        /*Se recorre el arreglo de cada dia obtenido*/
        for (int i = 0; i < jsonWeatherArray.length(); i++) {
            /* Valores que seran recolectados */
            long dateTimeMillis;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            int weatherId;

            /* Se obtiene del objeto JSON el dia */
            JSONObject dayForecast = jsonWeatherArray.getJSONObject(i);
            /*Se obtiene la Datos requeridos*/
            dateTimeMillis = normalizedUtcStartDay + CloudyDateUtils.DAY_IN_MILLIS * i;
            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            //JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            JSONObject weatherObject = dayForecast.getJSONObject(OWM_WEATHER);
            //description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);
            high = dayForecast.getDouble(OWM_MAX);
            low = dayForecast.getDouble(OWM_MIN) ;

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTimeMillis);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            weatherContentValues[i] = weatherValues;
        }

        return weatherContentValues;
    }


}
